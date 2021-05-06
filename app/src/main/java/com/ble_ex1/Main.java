package com.ble_ex1;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import com.ble_ex1.cmd_module.CmdAPI;
import com.ble_ex1.cmd_module.CmdListener;
import com.ble_ex1.cmd_module.CmdObserver;
import com.ble_ex1.cmd_module.Command;
import org.json.JSONObject;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Activity {
    private static final String TAG = Main.class.getSimpleName();
    private static final long SCAN_PERIOD = 3000;

    private TextView text_rx = null;
    private EditText edit_rx = null;
    private Button btn_tx = null, btn_connect = null, btn_write;
    private ImageView img_status_led = null;
    private Dialog dialog = null;

    private String deviceName = null, deviceAddress = null;
    private boolean isBLEModule = true;
    private CmdObserver cmdObserver = null;

    private BluetoothAdapter bluetoothAdapter = null;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            switch (action){
                case Global.BLE_STATUS:
                    int status = intent.getIntExtra(Global.BLE_STATUS, 0);
                    //Log.d(TAG, "Receive action: " + action + " status: " + status);
                    setLEDStatus(status);
                    break;
            }
        }

    };

    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Global.BLE_STATUS);
        filter.addAction(Global.BLE_EXECUTE);
        registerReceiver(broadcastReceiver,filter);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);

        registerPermissions();
        initBluetoothAdapter();
        initUI();
        initCmdService();
    }

    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    protected void onStop() {
        super.onStop();
        if(cmdObserver!=null) {
            Global.unregisterCmdService(cmdObserver);
            cmdObserver = null;
        }
    }

    public void initBluetoothAdapter() {
        if(isBLEModule) {
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = (bluetoothManager==null?null:bluetoothManager.getAdapter());
        }
        else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        if(!Global.initBleAdapter(this, bluetoothAdapter, isBLEModule)) {
            Toast.makeText(this, "Ble not supported.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = getIntent();
        deviceAddress = intent.getStringExtra(Device.EXTRA_DEVICE_ADDRESS);
        deviceName = intent.getStringExtra(Device.EXTRA_DEVICE_NAME);

        if(deviceAddress!=null && deviceAddress.length()>0 && deviceName!=null && deviceName.length()>0)
        {
            Global.connectBluetooth(deviceAddress);
        }
    }

    public void initCmdService() {
        if(cmdObserver == null) {
            cmdObserver = new CmdObserver("main", new CmdListener() {
                @Override
                public void onData(Command cmd) {
                    Log.d(TAG, "Main rev: " + cmd.toString());
                }
            });
            Global.registerCmdService(cmdObserver);
        }
    }

    public void initUI() {
        img_status_led = (ImageView) findViewById(R.id.img_status_led);
        text_rx = (TextView)findViewById(R.id.text_rx);
        edit_rx = (EditText)findViewById(R.id.edit_tx);

        btn_tx = (Button)findViewById(R.id.btn_tx);
        btn_tx.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    String str = edit_rx.getText().toString();
                    JSONObject obj = new JSONObject();
                    obj.put("cmd", "login");

                    Command command = CmdAPI.CreateCommand(obj.toString());
                    //Global.sendCommand(command);
                    command = Global.syncCommand(command);

                    Log.d(TAG, "sync rev: " + command.toString());

                } catch (Exception ex) {}
            }
        });

        btn_connect = (Button)findViewById(R.id.btn_connect);
        btn_connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                searchDevice();
            }
        });

        btn_write = (Button)findViewById(R.id.btn_write);
        btn_write.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    String cmd = edit_rx.getText().toString();
                    int number = Integer.parseInt(cmd);
                    if(number>0) {
                        String str = "";
                        for(int i=0;i<number;i++) str+="A";
                        //Global.writeBLE(CmdAPI.CreateCommand(new JSONObject().put("number", str).toString()));
                    }
                } catch (Exception ex) {Log.d(TAG, ex.toString());}
            }
        });
    }

    private void setLEDStatus(int status) {
        runOnUiThread(new Runnable() {
            public void run() {
                switch (status) {
                    case BluetoothProfile.STATE_CONNECTED:
                        img_status_led.setImageResource(R.drawable.circle_connect);
                        break;
                    default:
                        img_status_led.setImageResource(R.drawable.circle_close);
                }
            }
        });
    }

    public void searchDevice() {
        showRoundProcessDialog(Main.this, R.layout.loading_process_dialog_anim);

        Global.scanLeDevice();

        Timer mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent deviceListIntent = new Intent(getApplicationContext(), Device.class);
                startActivity(deviceListIntent);
                closeDialog();
            }
        }, SCAN_PERIOD);
    }

    public void showRoundProcessDialog(Context mContext, int layout) {
        DialogInterface.OnKeyListener keyListener = new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_SEARCH) {
                    return true;
                }
                return false;
            }
        };

        dialog = new AlertDialog.Builder(mContext).create();
        dialog.setOnKeyListener(keyListener);
        dialog.show();
        dialog.setContentView(layout);
    }

    public void closeDialog() {
        dialog.dismiss();
    }

    public void registerPermissions() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_PRIVILEGED,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Ble not supported.", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_devices, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        isBLEModule = (id==R.id.menu_ble_2_0?false:true);

        initBluetoothAdapter();

        return super.onOptionsItemSelected(item);
    }
}
