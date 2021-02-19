package com.ble_ex1;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class CMD_API {

    public static Command CreateCommand(String data) {
        Command command = null;
        try {
            JSONObject info  = new JSONObject(data);
            if(info!=null) {
                switch (info.optString("command", "")) {
                    case "firmware":
                        command = (Command) new Firmware(info);
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
        private String name = "";
        private long system_time = 0;

        public BaseCommand(JSONObject info) {
            this.info = info;
            this.name = info.optString("command", "");
            this.system_time = new Date().getTime();
        }

        public String getName() {
            return name;
        }

        public long getTime() {
            return system_time;
        }

        public String toString() {
            return info.toString();
        }

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
}
