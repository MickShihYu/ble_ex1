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

    public interface BLEListener {
        void onData(int status, String value);
    }

    public interface BufferListener {
        void onData(int status, byte[] value);
    }

    private final static String TAG = "BLEService";

    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothGatt mBluetoothGatt = null;
    private BufferListener readListener = null;
    private BLEListener statusListener = null;

    private BluetoothGattCharacteristic characteristicTx = null,  characteristicRx = null;

    private List<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>();

    private Activity activity = null;

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if(statusListener!=null) statusListener.onData(newState, "");

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    mBluetoothGatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    break;
            }
        }

        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.w(TAG, "onReadRemoteRssi received: " + status);
        };

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
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
                    //String rxStr = new String(characteristic.getValue());
                    //Log.d(TAG, "Read: " + rxStr + " len: " + rxStr.length());
                    readListener.onData(status, characteristic.getValue());
                }
            }
        } catch (Exception ex) { System.out.println(ex.toString()); }
    }

    private void getGattService() {
        BluetoothGattService gattService = mBluetoothGatt.getService(Global.UUID_BLE_HC08_SERVICE);
        if (gattService == null) return;

        characteristicTx = gattService.getCharacteristic(Global.UUID_BLE_HC08_TX);
        characteristicRx = gattService.getCharacteristic(Global.UUID_BLE_HC08_RX);

        mBluetoothGatt.setCharacteristicNotification(characteristicRx,true);
        mBluetoothGatt.readCharacteristic(characteristicRx);
    }

    public void setStatusListener(BLEListener listener) {
        this.statusListener = listener;
    }
    public void setReadListener(BufferListener listener) {
        this.readListener = listener;
    }

    public boolean initBleAdapter(Activity activity, BluetoothManager mBluetoothManager) {
        this.activity = activity;
        this.mBluetoothManager = mBluetoothManager;
        return initBleAdapter();
    }

    public boolean initBleAdapter() {
        mBluetoothAdapter = (mBluetoothManager==null?null:mBluetoothManager.getAdapter());
        if (mBluetoothAdapter == null) return false;
        return true;
    }

    public boolean initBleService(String address) {
        boolean status = connect(address);
        readRssi();
        return status;
    }

    public boolean connect(final String address) {
        try {
            if (mBluetoothAdapter == null || address == null) return false;
            else close();

            if (mBluetoothGatt != null) {
                if (mBluetoothGatt.connect()) return true;
                else return false;
            }

            final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            if (device == null) return false;

            mBluetoothGatt = device.connectGatt(activity, false, mGattCallback);
            return true;

        } catch (Exception ex) { System.out.println(ex.toString()); }
        return false;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) return;
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (mBluetoothGatt == null) return;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) return;
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void readRssi() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) return;
        mBluetoothGatt.readRemoteRssi();
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
        if (mBluetoothAdapter == null || mBluetoothGatt == null) return;
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) return;
        //mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        //BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString());
        //descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        //mBluetoothGatt.writeDescriptor(descriptor);
    }

    public List<BluetoothDevice> getBleDevice() {
        return mDevices;
    }

    public void scanLeDevice() {
        new Thread() {
            public void run() {
                try {
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                    Thread.sleep(2000);
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                } catch (Exception ex) {}
            }
        }.start();
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            if (device != null && device.getName()!=null && device.getName().length()>0) {
                if (mDevices.indexOf(device) == -1)
                    mDevices.add(device);
            }
        }
    };

}