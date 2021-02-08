package com.ble_ex1;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.util.Log;

import java.util.List;
import java.util.UUID;

public class Global {

    public final static UUID UUID_BLE_HC08_TX = UUID.fromString("000ffe1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_BLE_HC08_RX = UUID.fromString("000ffe1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_BLE_HC08_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

    private final static BLEService BleService = new BLEService();
    private final static CMDService CMDService = new CMDService();

    private final static String TAG = "CMDService";

    private static StringBuffer buffer = new StringBuffer();

    public static boolean initBleAdapter(Activity activity, BluetoothManager mBluetoothManager) {
        BleService.setActivity(activity);
        BleService.setBLEManager(mBluetoothManager);
        return BleService.initBleAdapter();
    }

    public static boolean initBleService(String address) {
        boolean status = Global.BleService.connect(address);
        BleService.readRssi();
        BleService.setReadListener(Bufferlistener);
        return status;
    }

    public static void setStatusListener(BLEService.BLEListener listener) {
        BleService.setStatusListener(listener);
    }

    public static void writeCharacteristic(String value) {
        BleService.writeCharacteristic(value);
    }

    public static List<BluetoothDevice> getBleDevice() {
        return BleService.getBleDevice();
    }

    public static void scanLeDevice() {
        BleService.scanLeDevice();
    }

    public static BLEService.BufferListener Bufferlistener = new BLEService.BufferListener() {
        public void onData(int status, byte[] byteArray) {
            String str = new String(byteArray);
            int _start = str.lastIndexOf("$");
            int _end = str.indexOf("#");

            if (_start != -1 && _end != -1) {
                str = str.substring(_start + 1, _end);
                Log.d(TAG, "str: " + str);
            } else if (_start != -1) {
                buffer = new StringBuffer();
                buffer.append(str.substring(_start + 1, str.length()));
            } else if (_end != -1) {
                buffer.append(str.substring(0, _end));
                String temp = buffer.toString();
                Log.d(TAG, "str: " + temp);
                buffer = new StringBuffer();
            } else {
                buffer.append(str);
            }
        }
    };

}
