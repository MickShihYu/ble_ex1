package com.ble_ex1;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import java.util.List;

interface BLEInterface {
    boolean connect(final String address);
    void scanLeDevice();
    List<BluetoothDevice> getBleDevice();
    void close();
}

interface BufferListener {
    void onData(String type, int status, byte[] value);
}
