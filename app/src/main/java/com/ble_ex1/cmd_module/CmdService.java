package com.ble_ex1.cmd_module;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.ble_ex1.Global;
import com.ble_ex1.ble_module.BleListener;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class CmdService implements Observable {

    private static final String TAG = "CmdService";
    private static final double CMD_RECEIVE_TIMEOUT = 1 * 1000;
    private ArrayList<Observer> observerList = new ArrayList<Observer>();
    private ArrayList<Command> cmdList = new ArrayList<Command>();
    private HashMap<String, Command> cmdTypeList = new HashMap<String, Command>();
    private StringBuffer buffer = new StringBuffer();
    private Command lastCommand = null;
    private boolean bleConnectStatus = false;

    public BleListener bleListener = new BleListener() {
        public void onData(String type, int status, byte[] byteArray) {
            try {
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
                    Log.d(TAG, "status: " + status);
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

            if(str.equals("AT")) {
                Global.writeString("OK");
            } else if(index == -1) {
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
            Command cmd = CmdAPI.CreateCommand(object.toString());
            if(cmd != null) {
                addCommandHistory(cmd);
                lastCommand = cmd;
                inform(Global.BLE_CHARACTERISTIC, cmd);
                addCommandTypeList(cmd);
            }
        } catch (Exception ex) { System.out.println(ex.toString()); }
    }

    public Command syncCommand(Command cmd) {

        try {
            Log.d(TAG, "----------------------------------------");
            Log.d(TAG, "async write: " + cmd.toString());

            addCommandHistory(cmd);

            long startTime = System.currentTimeMillis();
            while(System.currentTimeMillis()-startTime < CMD_RECEIVE_TIMEOUT) {
                if(lastCommand == null) continue;
                if(isNewCommand(cmd, lastCommand)) {
                    Log.d(TAG, "async rev: " + lastCommand.toString());
                    return lastCommand;
                }
                Thread.sleep(1);
            }
        } catch (Exception ex) { System.out.println(ex.toString()); }
        Log.d(TAG, "async rev: null");
        return null;
    }

    public ArrayList<Command> getCommandHistory() {
        return cmdList;
    }

    public void addCommandTypeList(Command cmd) {
        cmdTypeList.put(cmd.getCmd(), cmd);
    }

    public Command getCommandTypeList(String type) {
        return cmdTypeList.get(type);
    }

    public void addCommandHistory(Command cmd) {
        cmdList.add(cmd);
    }

    public void clearCommandHistory() {
        cmdList.clear();
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
    public void inform(String status, Object obj) {
        for(Observer observer : observerList){
            observer.update(status, obj);
        }
    }
}
