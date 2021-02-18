package com.ble_ex1;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Activity {
    private static final String TAG = Main.class.getSimpleName();
    private static final long SCAN_PERIOD = 3000;

    private TextView text_rx = null;
    private EditText edit_rx = null;
    private Button btn_tx = null, btn_connect = null, btn_write;
    private Dialog mDialog = null;
    private String mDeviceName = null, mDeviceAddress = null;

    protected  void onPause() {
        super.onPause();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);

        registerPermissions();

        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if(!Global.BleService.initBleAdapter(this, mBluetoothManager)) {
            Toast.makeText(this, "Ble not supported.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(Device.EXTRA_DEVICE_ADDRESS);
        mDeviceName = intent.getStringExtra(Device.EXTRA_DEVICE_NAME);

        if(mDeviceAddress!=null && mDeviceAddress.length()>0
            && mDeviceName!=null && mDeviceName.length()>0)
        {
            Global.BleService.initBleService(mDeviceAddress);
            Global.BleService.setStatusListener(statusListener);
            //Global.setReadListener(readListener);
        }

        initUI();
    }

    public BLEService.BLEListener statusListener = new BLEService.BLEListener() {
        @Override
        public void onData(int status, String value) {
        }
    };

//    public BLEService.BLEListener readListener = new BLEService.BLEListener() {
//        @Override
//        public void onData(int status, String value) {
//            runOnUiThread(new Runnable() {
//                public void run() {
//                    text_rx.append(value);
//                    final int scrollAmount = text_rx.getLayout().getLineTop(text_rx.getLineCount()) - text_rx.getHeight();
//                    if (scrollAmount > 0) text_rx.scrollTo(0, scrollAmount);
//                    else text_rx.scrollTo(0, 0);
//                }
//            });
//        }
//    };

    public void initUI() {
        text_rx = (TextView)findViewById(R.id.text_rx);
        edit_rx = (EditText)findViewById(R.id.edit_tx);

        btn_write = (Button)findViewById(R.id.btn_write);
        btn_write.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    int number = Integer.parseInt(edit_rx.getText().toString());
                    if(number>0) {
                        String str = "";
                        for(int i=0;i<number;i++) str+="A";
                        Global.BleService.writeCharacteristic(new JSONObject().put("number", str).toString());
                    }
                } catch (Exception ex) {}
            }
        });

        btn_tx = (Button)findViewById(R.id.btn_tx);
        btn_tx.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Global.BleService.writeCharacteristic(edit_rx.getText().toString());
            }
        });

        btn_connect = (Button)findViewById(R.id.btn_connect);
        btn_connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                searchDevice();
            }
        });
    }

    public void searchDevice() {
        showRoundProcessDialog(Main.this, R.layout.loading_process_dialog_anim);

        Global.BleService.scanLeDevice();

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
                if (keyCode == KeyEvent.KEYCODE_HOME
                        || keyCode == KeyEvent.KEYCODE_SEARCH) {
                    return true;
                }
                return false;
            }
        };

        mDialog = new AlertDialog.Builder(mContext).create();
        mDialog.setOnKeyListener(keyListener);
        mDialog.show();
        mDialog.setContentView(layout);
    }

    public void closeDialog() {
        mDialog.dismiss();
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
}
