package com.ble_ex1.cmd_module;
import com.ble_ex1.Global;
import com.ble_ex1.Tools;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Date;

public class CmdAPI {
    public static Command CreateCommand(String data) {
        Command command = null;
        try {
            JSONObject info  = new JSONObject(data);
            if(info!=null) {
                switch (info.optString("cmd", "")) {
                    case "firmware":
                        break;
                    case "login":
                    default:
                        command = (Command) new BaseCommand(info);
                }
            }
        } catch (Exception ex) { System.out.println(ex.toString()); }
        return command;
    }

    public static class BaseCommand implements Command {
        private String cmd = "";
        private JSONObject info = null;
        private long system_time = System.currentTimeMillis();

        public BaseCommand(String cmd) {
            this.cmd = cmd;
            try {
                info = new JSONObject();
                info.put("cmd", cmd);
            } catch (Exception ex) { System.out.println(ex.toString()); }
        }

        public BaseCommand(JSONObject info) {
            this.info = info;
            this.cmd = info.optString("cmd", "");
        }
        public String getCmd() {
            return cmd;
        }
        public String toString() { return info.toString(); }
        public long getTime() {
            return system_time;
        }
        public boolean getStatus() { return info!=null?info.optString("status","").equals("200 OK"):false; }
        public JSONObject getContent() { return info==null?new JSONObject():info; }
    }
}
