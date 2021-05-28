package com.ble_ex1.cmd_module;

import android.util.Log;

import com.ble_ex1.Global;
import com.ble_ex1.Tools;
import com.ble_ex1.cmd_module.CmdAPI;
import com.ble_ex1.cmd_module.Command;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class CmdSchedule {

    public static final String TAG = "CmdSchedule";
    private Timer loginTimer = new Timer();
    public CmdSchedule() {
        loginTimer.schedule(new LogInTimer(), 1000, 5 * 1000);
        loginTimer.schedule(new DeviceInfo(), 1000, 10 * 1000);
    }
}

class LogInTimer extends TimerTask {

    private boolean logInStatus = false;
    public void run() {
        try {
            if(!Global.bleConnectStatus()) return;
            if(!logInStatus)
                logInStatus = logIn();
            else
                logInStatus = ping();

            Log.d(CmdSchedule.TAG, "connect: " + Global.bleConnectStatus() + " token: " + (Global.token==null?"null":Global.token));
        } catch (Exception ex) { System.out.println(ex.toString()); }
    }

    public boolean logIn() {
        try {
            Global.writeString("\n");
            Thread.sleep(50);

            Command cmd = Global.syncCommand((Command) new CmdAPI.BaseCommand("login"));
            String challenge = cmd.getStatus() ? cmd.getContent().optString("challenge",null) : null;
            if(challenge!=null) {
                Global.token = getToken(challenge);
                return Global.token.length()>0 ? true : false;
            }
        } catch (Exception ex) { System.out.println(ex.toString()); }
        return false;
    }

    public String getToken(String challenge) {
        String token = "";
        try {
            JSONObject info = new JSONObject();
            info.put("cmd","get_token");
            info.put("password", Tools.Encrypt(challenge));
            Command cmd = Global.syncCommand((Command) new CmdAPI.BaseCommand(info));
            if(cmd.getStatus()) {
                token = (cmd==null?null:cmd.getContent().optString("token",""));
            }
        } catch (Exception ex) { System.out.println(ex.toString()); }
        return token;
    }

    public boolean ping() {
        boolean status = false;
        try {
            JSONObject info = new JSONObject();
            info.put("cmd","ping");
            info.put("token", Global.token);
            Command cmd = Global.syncCommand((Command) new CmdAPI.BaseCommand(info));
            status = cmd.getStatus();
        } catch (Exception ex) { System.out.println(ex.toString()); }
        return status;
    }
}

class DeviceInfo extends TimerTask {
    public void run() {
        try {
            if(!Global.bleConnectStatus()) return;
            if(Global.token==null) return;

            JSONObject info = new JSONObject();
            info.put("cmd","get_lteinfo");
            info.put("token", Global.token);
            Command cmd = Global.syncCommand((Command) new CmdAPI.BaseCommand(info));
        } catch (Exception ex) { System.out.println(ex.toString()); }
    }
}