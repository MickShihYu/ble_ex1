package com.ble_ex1;

import android.util.Log;

import com.ble_ex1.cmd_module.CmdAPI;
import com.ble_ex1.cmd_module.Command;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class CmdSchedule {

    private static final String TAG = "CmdSchedule";

    private Timer loginTimer = new Timer();
    private String token = null;

    public CmdSchedule() {
        initLoginTimer();
    }

    public void initLoginTimer() {
        try {
            loginTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.d(TAG, "connect: " + Global.bleConnectStatus() + " token: " + (token==null?"null":token));
                    if(!Global.bleConnectStatus()) return;
                    if(token == null || !ping()) token = logIn();

                }
            }, 3000, 10000);
        } catch (Exception ex) { System.out.println(ex.toString()); }
    }

    public String logIn() {
        try {
            Command cmd = Global.syncCommand((Command) new CmdAPI.BaseCommand("login"));
            String challenge = cmd.getStatus() ? cmd.getContent().optString("challenge",null) : null;
            if(challenge!=null) {
                return getToken(challenge);
            }
        } catch (Exception ex) { System.out.println(ex.toString()); }
        return null;
    }

    public String getToken(String challenge) {
        String token = null;
        try {
                JSONObject info = new JSONObject();
                info.put("cmd","get_token");
                info.put("password", challenge);
                Command cmd = Global.syncCommand((Command) new CmdAPI.BaseCommand(info));
                if(cmd.getStatus()) {
                    token = (cmd==null?null:cmd.getContent().optString("token",null));
                }

        } catch (Exception ex) { System.out.println(ex.toString()); }
        return token;
    }

    public boolean ping() {
        boolean status = false;
        try {
            JSONObject info = new JSONObject();
            info.put("cmd","ping");
            info.put("token",token);
            Command cmd = Global.syncCommand((Command) new CmdAPI.BaseCommand(info));
            status = cmd.getStatus();
        } catch (Exception ex) { System.out.println(ex.toString()); }
        return status;
    }
}