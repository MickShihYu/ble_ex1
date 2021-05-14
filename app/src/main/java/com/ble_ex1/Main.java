package com.ble_ex1;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ble_ex1.cmd_module.CmdListener;
import com.ble_ex1.cmd_module.CmdObserver;
import com.ble_ex1.fragments.DashboardFragment;
import com.ble_ex1.fragments.DetailFragment;
import com.ble_ex1.fragments.HomeFragment;
import com.ble_ex1.fragments.LogFragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Main extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = Main.class.getSimpleName();
    private String deviceName = null, deviceAddress = null;
    private boolean isBLEModule = true;
    private Button btn_connect = null;

    private Activity activity = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private CmdObserver cmdObserver = null;

    private FragmentManager mFragmentManager;
    private HomeFragment mHomeFragment;
    private DashboardFragment mDashboardFragment;
    private LogFragment mLogFragment;

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            HOME, DASHBOARD, LOG, DETAIL
    })
    public @interface FragmentType {}

    public static final String HOME = "HOME";
    public static final String DASHBOARD = "DASHBOARD";
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
        ((BottomNavigationView) findViewById(R.id.navigation)).setSelectedItemId(R.id.navigation_home);

        registerPermissions();
        initBluetoothAdapter();
        initCmdService();

        setBleConnectStatus(Global.bleConnectStatus() ? Global.BLE_CONNECTED : Global.BLE_DISCONNECTED);
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
        //toolbar.setLogo(R.mipmap.ic_launcher);
        //toolbar.inflateMenu(R.menu.navigation);

        mFragmentManager = getFragmentManager();

        btn_connect = (Button)findViewById(R.id.btn_ble_connect);
        btn_connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Global.searchDevice(activity);
            }
        });
    }

    public void initCmdService() {
        if(cmdObserver == null) {
            cmdObserver = new CmdObserver("main", new CmdListener() {
                @Override
                public void onData(String status, Object obj) {
                    if(status.equals(Global.BLE_CHARACTERISTIC)) {
                    } else {
                        setBleConnectStatus(status);
                    }
                }
            });
            Global.registerCmdService(cmdObserver);
        }
    }

    private void setBleConnectStatus(final String status) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                switch (status) {
                    case Global.BLE_CONNECTED:
                        //img_status_led.setImageResource(R.drawable.circle_connect);
                        btn_connect.setText("Connected");
                        break;
                    default:
                        btn_connect.setText("Disconnected");
                        //img_status_led.setImageResource(R.drawable.circle_close);
                }
            }
        });
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        if (mFragmentManager.findFragmentByTag(DETAIL) != null) {
            mFragmentManager.popBackStack();
        }

        switch (item.getItemId()) {
            case R.id.navigation_home:

                if (mHomeFragment == null) mHomeFragment = new HomeFragment();
                if (mDashboardFragment != null) fragmentTransaction.hide(mDashboardFragment);
                if (mLogFragment != null) fragmentTransaction.hide(mLogFragment);
                if (mHomeFragment.isAdded()) {
                    fragmentTransaction.show(mHomeFragment);
                } else {
                    fragmentTransaction.add(R.id.container_main, mHomeFragment, HOME);
                }
                fragmentTransaction.commit();

                return true;
            case R.id.navigation_dashboard:

                if (mDashboardFragment == null) mDashboardFragment = new DashboardFragment();
                if (mHomeFragment != null) fragmentTransaction.hide(mHomeFragment);
                if (mLogFragment != null) fragmentTransaction.hide(mLogFragment);
                if (mDashboardFragment.isAdded()) {
                    fragmentTransaction.show(mDashboardFragment);
                } else {
                    fragmentTransaction.add(R.id.container_main, mDashboardFragment, DASHBOARD);
                }
                fragmentTransaction.commit();

                return true;
            case R.id.navigation_log:

                if (mLogFragment == null) mLogFragment = new LogFragment();
                if (mHomeFragment != null) fragmentTransaction.hide(mHomeFragment);
                if (mDashboardFragment != null) fragmentTransaction.hide(mDashboardFragment);
                if (mLogFragment.isAdded()) {
                    fragmentTransaction.show(mLogFragment);
                } else {
                    fragmentTransaction.add(R.id.container_main, mLogFragment, LOG);
                }
                fragmentTransaction.commit();

                return true;
        }
        return false;
    }

    public void onOpenDetail(String message) {

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        if (mHomeFragment != null && !mHomeFragment.isHidden()) {
            fragmentTransaction.hide(mHomeFragment);
            fragmentTransaction.addToBackStack(HOME);
        }
        if (mDashboardFragment != null && !mDashboardFragment.isHidden()) {
            fragmentTransaction.hide(mDashboardFragment).addToBackStack(DASHBOARD);
        }
        if (mLogFragment != null && !mLogFragment.isHidden()) {
            fragmentTransaction.hide(mLogFragment).addToBackStack(LOG);
        }

        DetailFragment fragment = new DetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DETAIL_MESSAGE, message);
        fragment.setArguments(bundle);
        fragmentTransaction.add(R.id.container_main, fragment, DETAIL).commit();
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
