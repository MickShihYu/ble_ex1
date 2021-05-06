package com.ble_ex1.cmd_module;
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
                        command = (Command) new Firmware(info);
                        break;
                    case "login":
                        command = (Command) new LogIn(info);
                        break;
                    default:
                        command = (Command) new BaseCommand(info);
                }
            }
        } catch (Exception ex) { System.out.println(ex.toString()); }
        return command;
    }

    public static class BaseCommand implements Command {
        private JSONObject info = null;
        private String cmd = "";
        private long system_time = 0;

        public BaseCommand(JSONObject info) {
            this.info = info;
            this.cmd = info.optString("cmd", "");
            this.system_time = System.currentTimeMillis();
        }

        public String getCmd() {
            return cmd;
        }

        public long getTime() {
            return system_time;
        }

        public String toString() {
            return info.toString();
        }

        public JSONObject getInfo() { return info; }

        public JSONObject execute() throws JSONException {
            return info;
        }
    }

    public static class Firmware extends BaseCommand{
        public Firmware(JSONObject info) {
            super(info);
        }

        public JSONObject execute() throws  JSONException{
            return new JSONObject().put("firmware", true);
        }
    }

    public static class LogIn extends BaseCommand{
        public LogIn(JSONObject info) {
            super(info);
        }

        public JSONObject execute() throws  JSONException{
            JSONObject info = getInfo();
            String challenge = info.optString("challenge");

            return new JSONObject().put("firmware", true);
        }
    }
}
