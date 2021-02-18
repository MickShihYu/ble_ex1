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
                    default:
                        command = (Command) new BaseCommand(info);
                }
            }
        } catch (Exception ex) { System.out.println(ex.toString()); }
        return command;
    }

    static class BaseCommand implements Command {
        private JSONObject info = null;
        private Date system_time;

        public BaseCommand(JSONObject info) {
            this.info = info;
            this.system_time = new Date();
        }

        @Override
        public String getName() {
            return info.optString("command");
        }

        @Override
        public boolean reply(String name) {
            return false;
        }

        @Override
        public JSONObject execute() throws JSONException {
            return info.put("end_time", new Date().getTime());
        }
    }

    static class Firmware implements Command {

        public JSONObject info = null;

        public Firmware(JSONObject info) {
            this.info = info;
        }

        @Override
        public String getName() {
            return info.optString("command");
        }

        @Override
        public boolean reply(String name) {
            return false;
        }

        @Override
        public JSONObject execute() {
            return new JSONObject();
        }
    }
}
