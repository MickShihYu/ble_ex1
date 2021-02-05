package com.ble_ex1;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;

import java.util.List;
import java.util.UUID;

public class Global {

    public final static UUID UUID_BLE_HC08_TX = UUID.fromString("000ffe1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_BLE_HC08_RX = UUID.fromString("000ffe1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_BLE_HC08_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

    private final static BLEService BleService = new BLEService();
    private final static CMDService CMDService = new CMDService();

    public static boolean initBleAdapter(Activity activity, BluetoothManager mBluetoothManager) {
        BleService.setActivity(activity);
        BleService.setBLEManager(mBluetoothManager);
        return BleService.initBleAdapter();
    }

    public static boolean initBleService(String address) {
        boolean status = Global.BleService.connect(address);
        BleService.readRssi();
        BleService.setReadListener(CMDService.getListener());
        return status;
    }

    public static void setStatusListener(BLEService.BLEListener listener) {
        BleService.setStatusListener(listener);
    }

//    public static void setReadListener(BLEService.BLEListener listener) {
//        BleService.setReadListener(listener);
//    }

    public BLEService.BLEListener readListener = new BLEService.BLEListener() {
        @Override
        public void onData(int status, String value) {

        }
    };


    public static void writeCharacteristic(String value) {
        BleService.writeCharacteristic(value);
    }

    public static List<BluetoothDevice> getBleDevice() {
        return BleService.getBleDevice();
    }

    public static void scanLeDevice() {
        BleService.scanLeDevice();
    }

}
