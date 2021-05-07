package com.ble_ex1.cmd_module;

public interface CmdListener {
    void onData(String status, Command cmd);
}
