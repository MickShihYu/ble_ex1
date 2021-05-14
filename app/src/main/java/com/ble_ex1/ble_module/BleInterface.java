package com.ble_ex1.ble_module;
import android.bluetooth.BluetoothDevice;
import com.ble_ex1.ble_module.BleListener;
import java.util.List;

public interface BleInterface {
    boolean connect(final String address);
    List<BluetoothDevice> getBleDevice();
    void scanLeDevice();
    void setReadListener(BleListener listener);
    void writeCharacteristic(String value);
    void writeString(String value);
    boolean getConnectStatus();
    void close();
}