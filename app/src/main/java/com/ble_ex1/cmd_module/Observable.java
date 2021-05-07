package com.ble_ex1.cmd_module;

public interface Observable {
    void register(Observer reader);
    void unregister(Observer reader);
    void inform(String status, Command cmd);
}
