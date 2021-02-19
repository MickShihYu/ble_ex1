package com.ble_ex1;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.util.Log;
import java.util.List;
import java.util.UUID;

public class Global {

    private final static String TAG = "Global";

    public static final UUID UUID_BLE_HC08_TX = UUID.fromString("000ffe1-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_BLE_HC08_RX = UUID.fromString("000ffe1-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_BLE_HC08_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

    public final static String BLE_STATUS = "status";
    public final static String BLE_CHARACTERISTIC = "characteristic";
    public static final String BLE_TIME_OUT = "time_out";
    public static final String BLE_EXECUTE = "execute";

    private final static BLEService BLEService = new BLEService();
    private final static CMDService CMDService = new CMDService();
    private static Activity global_activity = null;

    private static StringBuffer buffer = new StringBuffer();

    public static boolean initBLEAdapter(Activity activity, BluetoothManager mBluetoothManager) {
        global_activity = activity;
       return BLEService.initBLEAdapter(activity, mBluetoothManager);
    }

    public static boolean connectBLE(String address) {
        BLEService.setReadListener(Bufferlistener);
        return BLEService.connect(address);
    }

    public static void writeBLE(Command command, CommandListener listener) {

        Log.d(TAG, "write: " + command.toString());

        BLEService.writeCharacteristic(command.toString());
        CMDService.write(command, listener);
    }

    public static void scanLeDevice() {
        BLEService.scanLeDevice();
    }

    public static List<BluetoothDevice> getBleDevice() {
        return BLEService.getBleDevice();
    }

    private static BLEService.BufferListener Bufferlistener = new BLEService.BufferListener() {
        public void onData(String type, int status, byte[] byteArray) {
            try {
                Log.d(TAG, "type: " + type + " status: " + status + " Rev: " + (byteArray==null?"null":new String(byteArray)));

                switch(type) {
                    case BLE_STATUS:
                        sendBroadcastStatus(status);
                        break;
                    case BLE_CHARACTERISTIC:
                        addBuffer(byteArray);
                        break;
                }
            } catch (Exception ex) { System.out.println(ex.toString()); }
        }
    };


    private static void sendBroadcastStatus(int status) {
        Intent intent = new Intent();
        intent.setAction(Global.BLE_STATUS);
        intent.putExtra(Global.BLE_STATUS, status);
        global_activity.sendBroadcast(intent);
    }

    private static void addBuffer(byte[] byteArray) {
        try {

            if(byteArray==null) return;

            String str = new String(byteArray);
            int start = str.lastIndexOf("$");
            int end = str.indexOf("#");

            if (start != -1 && end != -1) {
                str = str.substring(start + 1, end);
                CMDService.receive(CMD_API.CreateCommand(str));
            } else if (start != -1) {
                buffer = new StringBuffer();
                buffer.append(str.substring(start + 1));
            } else if (end != -1) {
                buffer.append(str.substring(0, end));
                CMDService.receive(CMD_API.CreateCommand(buffer.toString()));
                buffer = new StringBuffer();
            } else {
                buffer.append(str);
            }

        } catch (Exception ex) { System.out.println(ex.toString()); }
    }
}
