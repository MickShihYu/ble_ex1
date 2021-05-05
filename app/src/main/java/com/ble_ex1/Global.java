package com.ble_ex1;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;

import com.ble_ex1.cmd_module.CmdListener;
import com.ble_ex1.cmd_module.CmdObserver;
import com.ble_ex1.cmd_module.Command;

import java.util.List;
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


    public static final String CMD_TIME_OUT = "timeout";

    private static BleInterface bleService = null;
    private final static CmdService cmdService = new CmdService();
    private static Activity global_activity = null;

    public static boolean initBleAdapter(Activity activity, BluetoothAdapter bluetoothAdapter, boolean isBleModule) {
        global_activity = activity;
        if(bleService!=null)
            bleService.close();

        bleService = isBleModule?new BleTunnel(activity, bluetoothAdapter):new BluetoothTunnel(activity, bluetoothAdapter);
        return bleService!=null?true:false;
    }

    public static boolean connectBluetooth(String address) {
        bleService.setReadListener(cmdService.getBleListener());
        return bleService.connect(address);
    }

    public static void registerCmdService(CmdObserver observer) {
        cmdService.register(observer);
    }

    public static void sendCommand(Command command) {
        bleService.writeCharacteristic(command.toString());
        Log.d(TAG, "write: " + command.toString());
    }

    public static void sendCommand(Command command, CmdListener listener) {
        bleService.writeCharacteristic(command.toString());
        cmdService.sendCommand(command, listener);
        Log.d(TAG, "async write: " + command.toString());
    }

    public static void scanLeDevice() {
        bleService.scanLeDevice();
    }

    public static List<BluetoothDevice> getBleDevice() {
        return bleService.getBleDevice();
    }

    private static void sendBroadcastStatus(int status) {
        Intent intent = new Intent();
        intent.setAction(Global.BLE_STATUS);
        intent.putExtra(Global.BLE_STATUS, status);
        global_activity.sendBroadcast(intent);
    }
}
