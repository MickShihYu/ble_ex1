package com.ble_ex1.cmd_module;

public interface Observer {
    void subscribe(Observable cmdService);
    void unsubscribe();
    void update(String status, Object obj);
}
