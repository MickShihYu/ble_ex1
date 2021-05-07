package com.ble_ex1;

import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.util.Log;

import com.ble_ex1.cmd_module.CmdAPI;
import com.ble_ex1.cmd_module.CmdListener;
import com.ble_ex1.cmd_module.Command;
import com.ble_ex1.cmd_module.Observable;
import com.ble_ex1.cmd_module.Observer;

import org.json.JSONObject;

import java.util.ArrayList;

public class CmdService implements Observable {

    private static final String TAG = "CmdService";
    private ArrayList<Observer> observerList = new ArrayList<Observer>();
    private StringBuffer buffer = new StringBuffer();
    private Command lastCommand = null;
    private boolean bleConnectStatus = false;

    public BleListener bleListener = new BleListener() {
        public void onData(String type, int status, byte[] byteArray) {
            try {
                Log.d(TAG, "type: " + type + " status: " + status + " Rev: " + (byteArray==null?"null":new String(byteArray)));

                if(type.equals(Global.BLE_STATUS)) {
                    switch (status) {
                        case BluetoothProfile.STATE_CONNECTED:
                            inform(Global.BLE_CONNECTED, null);
                            bleConnectStatus = true;
                            break;
                        default:
                            inform(Global.BLE_DISCONNECTED, null);
                            bleConnectStatus = false;
                    }
                } else {
                    addBuffer(byteArray);
                }
            } catch (Exception ex) { System.out.println(ex.toString()); }
        }
    };

    public boolean getBleConnectStatus() {
        return bleConnectStatus;
    }

    private void addBuffer(byte[] byteArray) {
        try {
            if(byteArray==null) return;
            String str = new String(byteArray);
            int size = str.length() - 1;
            int index = str.indexOf(0x0a);

            if(index == -1) {
                buffer.append(str);
            } else {
                if(index < size) {
                    buffer.append(str.substring(0, index));
                    informCommand(buffer.toString());
                    buffer.append(str.substring(index, size));
                    buffer = new StringBuffer();
                } else {
                    buffer.append(str.substring(0, size));
                    informCommand(buffer.toString());
                    buffer = new StringBuffer();
                }
            }

        } catch (Exception ex) { System.out.println(ex.toString()); }
    }

    public BleListener getBleListener() { return bleListener; }

    public void informCommand(String data) {
        try {
            JSONObject object = new JSONObject(data);
            lastCommand = CmdAPI.CreateCommand(object.toString());
            inform(Global.BLE_CHARACTERISTIC, lastCommand);
        } catch (Exception ex) { System.out.println(ex.toString()); }
    }

    public Command syncCommand(Command cmd) {

        try {
            long startTime = System.currentTimeMillis();
            while(System.currentTimeMillis()-startTime < 3*1000) {
                if(lastCommand == null) continue;
                if(isNewCommand(cmd, lastCommand)) {
                    return lastCommand;
                }
                Thread.sleep(10);
            }
        } catch (Exception ex) { System.out.println(ex.toString()); }
        return null;
    }

    public boolean isNewCommand(Command curCmd, Command disCmd) {
        return curCmd.getCmd().equals(disCmd.getCmd()) && curCmd.getTime() < disCmd.getTime() ? true : false;
    }

    @Override
    public void register(Observer observer) {
        observerList.add(observer);
    }

    @Override
    public void unregister(Observer observer) {
        observerList.remove(observer);
    }

    @Override
    public void inform(String status, Command cmd) {
        for(Observer observer : observerList){
            observer.update(status, cmd);
        }
    }
}
