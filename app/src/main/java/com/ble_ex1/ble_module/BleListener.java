package com.ble_ex1.ble_module;
public interface BleListener {
    void onData(String type, int status, byte[] value);
}
