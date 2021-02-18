package com.ble_ex1;

import org.json.JSONException;
import org.json.JSONObject;

//interface CMDSubject {
//    void attach(Command command);
//    void detach(Command command);
//    void replyCommands();
//    void executeCommands();
//}

interface Command {
    String getName();
    boolean reply(String name);
    JSONObject execute() throws JSONException;
}

interface CommandListener {
    void onData(String value);
}