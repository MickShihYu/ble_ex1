package com.ble_ex1;
import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.StringDef;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;
import com.ble_ex1.cmd_module.CmdListener;
import com.ble_ex1.cmd_module.CmdObserver;
import com.ble_ex1.fragments.DashboardFragment;
import com.ble_ex1.fragments.DeviceFragment;
import com.ble_ex1.fragments.LogFragment;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Main extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = Main.class.getSimpleName();
    private boolean isBLEModule = true;

    private Activity activity = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private CmdObserver cmdObserver = null;

    //private Button btn_connect = null;
    private FragmentManager mFragmentManager;
    private DashboardFragment mDashboardFragment;
    private DeviceFragment mDeviceFragment;
    private LogFragment mLogFragment;

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            DASHBOARD, DEVICE, LOG, DETAIL
    })
    public @interface FragmentType {}

    public static final String DASHBOARD = "DASHBOARD";
    public static final String DEVICE = "DEVICE";
    public static final String LOG = "Log";
    public static final String DETAIL = "DETAIL";
    public static final String DETAIL_MESSAGE = "DetailMessage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        activity = this;
        initUI();

        ((BottomNavigationView) findViewById(R.id.navigation)).setOnNavigationItemSelectedListener(this);
        ((BottomNavigationView) findViewById(R.id.navigation)).setSelectedItemId(R.id.navigation_log);

        registerPermissions();
        initBluetoothAdapter();
        initCmdService();

        //setBleConnectStatus(Global.bleConnectStatus() ? Global.BLE_CONNECTED : Global.BLE_DISCONNECTED);
    }

    public void onStop() {
        super.onStop();
        if(cmdObserver!=null) {
            Global.unregisterCmdService(cmdObserver);
            cmdObserver = null;
        }
    }

    public void initUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("BLE");
        toolbar.setSubtitle("");

        mFragmentManager = getFragmentManager();
//        btn_connect = (Button)findViewById(R.id.btn_ble_connect);
//        btn_connect.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
////                Global.searchDevice(activity);
//            }
//        });
    }

    public void initBluetoothAdapter() {
        if(isBLEModule) {
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = (bluetoothManager==null?null:bluetoothManager.getAdapter());
        }
        else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        if(!Global.initBleAdapter(this, bluetoothAdapter)) {
            Toast.makeText(this, "Ble not supported.", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void initCmdService() {
        if(cmdObserver == null) {
            cmdObserver = new CmdObserver("main", new CmdListener() {
                @Override
                public void onData(String status, Object obj) {
                    if(status.equals(Global.BLE_CHARACTERISTIC)) {
                        //setBleConnectStatus(Global.BLE_CONNECTED);
                    } else {
                        //setBleConnectStatus(status);
                    }
                }
            });
            Global.registerCmdService(cmdObserver);
        }
    }

//    private void setBleConnectStatus(final String status) {
//        activity.runOnUiThread(new Runnable() {
//            public void run() {
//                switch (status) {
//                    case Global.BLE_CONNECTED:
//                        btn_connect.setText("Connected");
//                        break;
//                    default:
//                        btn_connect.setText("Disconnected");
//                }
//            }
//        });
//    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        if (mFragmentManager.findFragmentByTag(DETAIL) != null) {
            mFragmentManager.popBackStack();
        }

        switch (item.getItemId()) {

            case R.id.navigation_dashboard:

                if (mDashboardFragment == null) mDashboardFragment = new DashboardFragment();
                if (mDeviceFragment != null) fragmentTransaction.hide(mDeviceFragment);
                if (mLogFragment != null) fragmentTransaction.hide(mLogFragment);
                if (mDashboardFragment.isAdded()) {
                    fragmentTransaction.show(mDashboardFragment);
                } else {
                    fragmentTransaction.add(R.id.container_main, mDashboardFragment, DASHBOARD);
                }
                fragmentTransaction.detach(mDashboardFragment);
                fragmentTransaction.attach(mDashboardFragment);
                fragmentTransaction.commit();
                return true;

            case R.id.navigation_Device:
                if (mDeviceFragment == null) mDeviceFragment = new DeviceFragment();
                if (mDashboardFragment != null) fragmentTransaction.hide(mDashboardFragment);
                if (mLogFragment != null) fragmentTransaction.hide(mLogFragment);
                if (mDeviceFragment.isAdded()) {
                    fragmentTransaction.show(mDeviceFragment);
                } else {
                    fragmentTransaction.add(R.id.container_main, mDeviceFragment, DEVICE);
                }
                fragmentTransaction.detach(mDeviceFragment);
                fragmentTransaction.attach(mDeviceFragment);
                fragmentTransaction.commit();
                return true;

            case R.id.navigation_log:

                if (mLogFragment == null) mLogFragment = new LogFragment();
                if (mDeviceFragment != null) fragmentTransaction.hide(mDeviceFragment);
                if (mDashboardFragment != null) fragmentTransaction.hide(mDashboardFragment);
                if (mLogFragment.isAdded()) {
                    fragmentTransaction.show(mLogFragment);
                } else {
                    fragmentTransaction.add(R.id.container_main, mLogFragment, LOG);
                }

                fragmentTransaction.detach(mLogFragment);
                fragmentTransaction.attach(mLogFragment);
                fragmentTransaction.commit();
                return true;
        }
        return false;
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
