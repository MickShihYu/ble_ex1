package com.ble_ex1.cmd_module;

import org.json.JSONException;
import org.json.JSONObject;

public interface Command {
    String getCmd();
    String toString();
    long getTime();
    boolean getStatus();
    JSONObject getContent();
}
