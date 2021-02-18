package com.ble_ex1;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Global {

    private final static String TAG = "CMDService";

    public final static UUID UUID_BLE_HC08_TX = UUID.fromString("000ffe1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_BLE_HC08_RX = UUID.fromString("000ffe1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_BLE_HC08_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

    public final static BLEService BleService = new BLEService();
    public final static CMDService CMDService = new CMDService();

    private static StringBuffer buffer = new StringBuffer();
    //private static ArrayList<JSONObject> queue = new ArrayList<JSONObject>();

    public static BLEService.BufferListener Bufferlistener = new BLEService.BufferListener() {
        public void onData(int status, byte[] byteArray) {

            String str = new String(byteArray);
            int start = str.lastIndexOf("$");
            int end = str.indexOf("#");

            if (start != -1 && end != -1) {
                str = str.substring(start + 1, end);
                Log.d(TAG, "str: " + str);

            } else if (start != -1) {

                buffer = new StringBuffer();
                buffer.append(str.substring(start + 1, str.length()));

            } else if (end != -1) {
                buffer.append(str.substring(0, end));

                String temp = buffer.toString();
                Log.d(TAG, "str: " + temp);
                buffer = new StringBuffer();

            } else {
                buffer.append(str);
            }
        }
    };

}
