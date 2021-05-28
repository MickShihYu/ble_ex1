package com.ble_ex1;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.ble_ex1.ble_module.BleTunnel;
import com.ble_ex1.cmd_module.CmdObserver;
import com.ble_ex1.cmd_module.CmdSchedule;
import com.ble_ex1.cmd_module.CmdService;
import com.ble_ex1.cmd_module.Command;

import java.util.ArrayList;
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

    private static BleTunnel bleTunnel = null;
    private static CmdService cmdService = new CmdService();
    private static CmdSchedule cmdSchedule = new CmdSchedule();

    private static final long SCAN_PERIOD = 3000;
    private static Dialog dialog = null;

    public static boolean initBleAdapter(Activity activity, BluetoothAdapter bluetoothAdapter) {
        if(bleTunnel!=null)
            bleTunnel.close();

        bleTunnel = new BleTunnel(activity, bluetoothAdapter);
        return bleTunnel!=null?true:false;
    }

    public static boolean connectBluetooth(String address) {
        bleTunnel.setReadListener(cmdService.getBleListener());
        return bleTunnel.connect(address);
    }

    public static void closeBluetooth() {
        bleTunnel.close();
    }

    public static BluetoothDevice getConnectDevice() {
        return bleTunnel.getConnectDevice();
    }

    public static boolean bleConnectStatus() {
        return bleTunnel.getConnectStatus();
    }

    public static void writeString(String value) {
         bleTunnel.writeString(value);
    }

    public static void registerCmdService(CmdObserver observer) {
        cmdService.register(observer);
    }

    public static ArrayList<Command> getCommandHistory() {
        return cmdService.getCommandHistory();
    }

    public static void clearCommandHistory() {
        cmdService.clearCommandHistory();
    }

    public static Command getCommandTypeList(String type) {
        return cmdService.getCommandTypeList(type);
    }

    public static void unregisterCmdService(CmdObserver observer) {
        cmdService.unregister(observer);
    }

    public static void sendCommand(Command command) {
        bleTunnel.writeCharacteristic(command.toString());
        Log.d(TAG, "write: " + command.toString());
    }

    public static Command syncCommand(Command command) {
        bleTunnel.writeCharacteristic(command.toString());
        return cmdService.syncCommand(command);
    }

    public static void scanLeDevice(BluetoothAdapter.LeScanCallback bleScanCallback) {
        bleTunnel.scanLeDevice(bleScanCallback);
    }

    public static void stopDevice(BluetoothAdapter.LeScanCallback bleScanCallback) {
        bleTunnel.stopLeDevice(bleScanCallback);
    }

//    public static void searchDevice(final Activity activity) {
//        showRoundProcessDialog(activity, R.layout.loading_process_dialog_anim);
//        Global.scanLeDevice();
//        Timer mTimer = new Timer();
//        mTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                Intent deviceListIntent = new Intent(activity.getApplicationContext(), Device.class);
//                activity.startActivity(deviceListIntent);
//                closeDialog();
//            }
//        }, 3 * 1000);
//    }
//
//    public static void showRoundProcessDialog(Context mContext, int layout) {
//        DialogInterface.OnKeyListener keyListener = new DialogInterface.OnKeyListener() {
//            public boolean onKey(DialogInterface dialog, int keyCode,
//                                 KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_SEARCH) {
//                    return true;
//                }
//                return false;
//            }
//        };
//        dialog = new AlertDialog.Builder(mContext).create();
//        dialog.setOnKeyListener(keyListener);
//        dialog.show();
//        dialog.setContentView(layout);
//    }
//
//    public static void closeDialog() {
//        dialog.dismiss();
//    }
}
