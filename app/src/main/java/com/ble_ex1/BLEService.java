package com.ble_ex1;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

class BLEService {

    public interface BufferListener {
        void onData(String type, int status, byte[] value);
    }

    private final static String TAG = "BLEService";
    private BluetoothManager bluetoothManager = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothGatt bluetoothGatt = null;
    private BufferListener readListener = null;
    private BluetoothGattCharacteristic characteristicTx = null,  characteristicRx = null;
    private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    private Activity activity = null;

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if(readListener!=null) readListener.onData(Global.BLE_STATUS, newState, null);

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    bluetoothGatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    break;
            }
        }

        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.w(TAG, "Rssi status: " + status);
        };

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.w(TAG, "Discovered status: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) getGattService();
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            characteristicRead(characteristic, status);
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            characteristicRead(characteristic, BluetoothGatt.GATT_SUCCESS);
        }
    };

    private void characteristicRead(BluetoothGattCharacteristic characteristic, int status) {
        try {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (readListener != null) {
                    readListener.onData(Global.BLE_CHARACTERISTIC, status, characteristic.getValue());
                }
            }
        } catch (Exception ex) { System.out.println(ex.toString()); }
    }

    private void getGattService() {
        BluetoothGattService gattService = bluetoothGatt.getService(Global.UUID_BLE_HC08_SERVICE);
        if (gattService == null) return;

        characteristicTx = gattService.getCharacteristic(Global.UUID_BLE_HC08_TX);
        characteristicRx = gattService.getCharacteristic(Global.UUID_BLE_HC08_RX);

        bluetoothGatt.setCharacteristicNotification(characteristicRx,true);
        bluetoothGatt.readCharacteristic(characteristicRx);
    }

    public void setReadListener(BufferListener listener) {
        this.readListener = listener;
    }

    public boolean initBLEAdapter(Activity activity, BluetoothManager bluetoothManager) {
        this.activity = activity;
        this.bluetoothManager = bluetoothManager;
        return initBLEAdapter();
    }

    public boolean initBLEAdapter() {
        bluetoothAdapter = (bluetoothManager==null?null:bluetoothManager.getAdapter());
        if (bluetoothAdapter == null) return false;
        return true;
    }

    public boolean connect(final String address) {
        try {
            if (bluetoothAdapter == null || address == null) return false;
            else close();

            if (bluetoothGatt != null) {
                if (bluetoothGatt.connect()) return true;
                else return false;
            }

            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            if (device == null) return false;

            bluetoothGatt = device.connectGatt(activity, false, gattCallback);
            return true;

        } catch (Exception ex) { System.out.println(ex.toString()); }
        return false;
    }

    public void disconnect() {
        if (bluetoothAdapter == null || bluetoothGatt == null) return;
        bluetoothGatt.disconnect();
    }

    public void close() {
        if (bluetoothGatt == null) return;
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothAdapter == null || bluetoothGatt == null) return;
        bluetoothGatt.readCharacteristic(characteristic);
    }

    public void readRssi() {
        if (bluetoothAdapter == null || bluetoothGatt == null) return;
        bluetoothGatt.readRemoteRssi();
    }

    public void writeCharacteristic(String value) {
        try {
            byte b = 0x00;
            byte[] tmp = value.getBytes();
            byte[] tx = new byte[tmp.length + 1];
            tx[0] = b;
            for(int i = 1; i < tmp.length + 1; i++) tx[i] = tmp[i - 1];
            characteristicTx.setValue(tx);
            writeCharacteristic(characteristicTx);
        } catch (Exception ex) { System.out.println(ex.toString()); }
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothAdapter == null || bluetoothGatt == null) return;
        bluetoothGatt.writeCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (bluetoothAdapter == null || bluetoothGatt == null) return;
        //bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        //BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString());
        //descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        //bluetoothGatt.writeDescriptor(descriptor);
    }

    public List<BluetoothDevice> getBleDevice() {
        return devices;
    }

    public void scanLeDevice() {
        new Thread() {
            public void run() {
                try {
                    bluetoothAdapter.startLeScan(bleScanCallback);
                    Thread.sleep(2000);
                    bluetoothAdapter.stopLeScan(bleScanCallback);
                } catch (Exception ex) {}
            }
        }.start();
    }

    private BluetoothAdapter.LeScanCallback bleScanCallback = new BluetoothAdapter.LeScanCallback() {
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            if (device != null && device.getName()!=null && device.getName().length()>0) {
                if (devices.indexOf(device) == -1)
                    devices.add(device);
            }
        }
    };

}