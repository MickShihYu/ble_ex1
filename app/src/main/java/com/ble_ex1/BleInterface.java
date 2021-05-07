package com.ble_ex1;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import java.util.List;

interface BleInterface {
    boolean connect(final String address);
    List<BluetoothDevice> getBleDevice();
    void scanLeDevice();
    void setReadListener(BleListener listener);
    void writeCharacteristic(String value);
    boolean getConnectStatus();
    void close();
}

interface BleListener {
    void onData(String type, int status, byte[] value);
}
