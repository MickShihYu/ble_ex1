package com.ble_ex1;

import org.json.JSONException;
import org.json.JSONObject;

interface Command {
    String getName();
    String toString();
    JSONObject execute() throws JSONException;
    long getTime();
}

interface CommandListener {
    void onData(String status,  JSONObject value);
}