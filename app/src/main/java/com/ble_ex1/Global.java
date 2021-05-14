package com.ble_ex1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import com.ble_ex1.ble_module.BleInterface;
import com.ble_ex1.ble_module.BleTunnel;
import com.ble_ex1.ble_module.BluetoothTunnel;
import com.ble_ex1.cmd_module.CmdListener;
import com.ble_ex1.cmd_module.CmdObserver;
import com.ble_ex1.cmd_module.CmdSchedule;
import com.ble_ex1.cmd_module.CmdService;
import com.ble_ex1.cmd_module.Command;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Global {

    private final static String TAG = "Global";

    public static final UUID UUID_BLE_HC08_TX = UUID.fromString("000ffe1-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_BLE_HC08_RX = UUID.fromString("000ffe1-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_BLE_HC08_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

    public static final String BLE_STATUS = "status";
    public static final String BLE_CHARACTERISTIC = "characteristic";
    public static final String BLE_TIME_OUT = "timeout";
    public static final String BLE_EXECUTE = "execute";
    public static final String BLE_CONNECTED = "connected";
    public static final String BLE_DISCONNECTED = "disconnected";
    public static final String CMD_TIME_OUT = "timeout";

    public static String token = "";

    private static BleInterface bleService = null;
    private static CmdService cmdService = new CmdService();
    private static CmdSchedule cmdSchedule = new CmdSchedule();

    private static final long SCAN_PERIOD = 3000;
    private static Dialog dialog = null;

    public static boolean initBleAdapter(Activity activity, BluetoothAdapter bluetoothAdapter, boolean isBleModule) {
        if(bleService!=null)
            bleService.close();

        bleService = isBleModule?new BleTunnel(activity, bluetoothAdapter):new BluetoothTunnel(activity, bluetoothAdapter);
        return bleService!=null?true:false;
    }

    public static boolean connectBluetooth(String address) {
        bleService.setReadListener(cmdService.getBleListener());
        return bleService.connect(address);
    }

    public static boolean bleConnectStatus() {
        return bleService.getConnectStatus();
    }

    public static void writeString(String value) {
         bleService.writeString(value);
    }

    public static void registerCmdService(CmdObserver observer) {
        cmdService.register(observer);
    }



    public static void unregisterCmdService(CmdObserver observer) {
        cmdService.unregister(observer);
    }

    public static void sendCommand(Command command) {
        bleService.writeCharacteristic(command.toString());
        Log.d(TAG, "write: " + command.toString());
    }

    public static Command syncCommand(Command command) {
        bleService.writeCharacteristic(command.toString());
        return cmdService.syncCommand(command);
    }

    public static void scanLeDevice() {
        bleService.scanLeDevice();
    }

    public static List<BluetoothDevice> getBleDevice() {
        return bleService.getBleDevice();
    }

    public static void searchDevice(final Activity activity) {
        showRoundProcessDialog(activity, R.layout.loading_process_dialog_anim);
        Global.scanLeDevice();
        Timer mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent deviceListIntent = new Intent(activity.getApplicationContext(), Device.class);
                activity.startActivity(deviceListIntent);
                closeDialog();
            }
        }, 3 * 1000);
    }

    public static void showRoundProcessDialog(Context mContext, int layout) {
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

    public static void closeDialog() {
        dialog.dismiss();
    }

}
