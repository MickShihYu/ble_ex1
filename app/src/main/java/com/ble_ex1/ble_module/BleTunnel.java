package com.ble_ex1.ble_module;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;
import com.ble_ex1.Global;

public class BleTunnel {

    private final static String TAG = "BleTunnel";

    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothGatt bluetoothGatt = null;
    private BluetoothDevice device = null;
    private BleListener readListener = null;
    private BluetoothGattCharacteristic characteristicTx = null,  characteristicRx = null;

    private Activity activity = null;

    private boolean connectStatus = false;
    private boolean autoConnect = false;


    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            Log.d(TAG, "Connect status: " + status + " new status: " + newState);
            if(readListener!=null) readListener.onData(Global.BLE_STATUS, newState, null);

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    bluetoothGatt.discoverServices();
                    connectStatus = true;
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                default:
                    connectStatus = false;
                    bluetoothGatt.close();
            }
        }

        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG, "rssi status: " + status + " value: " + rssi);
        };

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "discovered status: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                getGattService();

//                TimerTask task = new TimerTask()
//                {
//                    public void run()
//                    {
//                        bluetoothGatt.readRemoteRssi();
//                    }
//                };
//                Timer rssiTimer = new Timer();
//                rssiTimer.schedule(task, 1000, 1000);
            }
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "read status: " + status);
            //characteristicRead(characteristic, status);
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
                    //String str = new String(characteristic.getValue());
                    //Log.d(TAG, "rev: " + str);
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

    public boolean getConnectStatus() {
        return connectStatus;
    }
    public void setReadListener(BleListener listener) {
        this.readListener = listener;
    }

    public BleTunnel(Activity activity, BluetoothAdapter bluetoothAdapter) {
        this.activity = activity;
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public boolean connect(final String address) {
        try {
            if (bluetoothAdapter == null || address == null) return false;
            else close();

            device = bluetoothAdapter.getRemoteDevice(address);
            if (device == null) return false;

            bluetoothGatt = device.connectGatt(activity, autoConnect, gattCallback);
            return true;

        } catch (Exception ex) { System.out.println(ex.toString()); }
        return false;
    }

    public void close() {
        if (bluetoothGatt == null) return;

        bluetoothGatt.disconnect();
        bluetoothGatt.close();
        bluetoothGatt = null;
        connectStatus = false;
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
            value =  value + "\n";

            int strSize = value.length(), loopSize = 20;
            int loop = strSize<=loopSize?1:(strSize/loopSize)+1;
            int lastCount = strSize%loopSize;

            for(int i=0;i<loop;i++) {
                int start = i*20;
                int end = i==loop-1?(loopSize*i)+lastCount:loopSize*i+20;

                String temp = value.substring(start, end);
                characteristicTx.setValue(temp.getBytes());
                writeCharacteristic(characteristicTx);
                Thread.sleep(3);
            }
        } catch (Exception ex) { System.out.println(ex.toString()); }
    }

    public void writeString(String value) {
        try {
            characteristicTx.setValue(value.getBytes());
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

    public BluetoothDevice getConnectDevice() {
        return device;
    }

    public void scanLeDevice(BluetoothAdapter.LeScanCallback bleScanCallback) {
        try {
            bluetoothAdapter.startLeScan(bleScanCallback);
        } catch (Exception ex) {}
    }

    public void stopLeDevice(BluetoothAdapter.LeScanCallback bleScanCallback) {
        try {
            bluetoothAdapter.stopLeScan(bleScanCallback);
        } catch (Exception ex) {}
    }
}