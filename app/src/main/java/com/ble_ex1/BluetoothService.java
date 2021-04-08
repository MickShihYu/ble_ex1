package com.ble_ex1;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BluetoothService implements BLEInterface{

    private final static String TAG = "BluetoothService";

    private static final UUID BLUETOOTH_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Activity activity = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket socket;
    private BluetoothDevice device;
    private BufferListener readListener = null;

    private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

    public BluetoothService(Activity activity, BluetoothAdapter bluetoothAdapter) {
        this.activity = activity;
        this.bluetoothAdapter = bluetoothAdapter;

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);

        if(bluetoothAdapter!=null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            activity.registerReceiver(receiver, filter);
            bluetoothAdapter.startDiscovery();
        }
    }

    @Override
    public boolean connect(String address) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        device = bluetoothAdapter.getRemoteDevice(address);

        Log.d(TAG, "connect : "+ address);

        new Thread(){
            public void run() {

                socketRun();
            }
        }.start();

        return false;
    }

    @Override
    public void close() {
        if(socket != null) {
            try {
                socket.close();
            } catch (Exception ignored) {}
            socket = null;
        }
    }

    @Override
    public void scanLeDevice() {
        new Thread() {
            public void run() {
                try {
                    Log.d(TAG, "Start scan device ...");
                    Thread.sleep(10 * 1000);
                } catch (Exception ex) {}
            }
        }.start();
    }

    public void socketRun() { // connect & read
        try {
            socket = device.createRfcommSocketToServiceRecord(BLUETOOTH_SPP);
            socket.connect();

        } catch (Exception e) {
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            socket = null;
            return;
        }

        try {
            byte[] buffer = new byte[1024];

            while (true) {
                int len = socket.getInputStream().read(buffer);
                byte[] data = Arrays.copyOf(buffer, len);

                if(data!=null && data.length>0) {
                    Log.d(TAG, "rev: " + new String(data));
                    if(readListener != null) readListener.onData(Global.BLE_CHARACTERISTIC, 1, data);
                }
            }
        } catch (Exception e) {
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            socket = null;
        }
    }

    public List<BluetoothDevice> getBleDevice() {
        return devices;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                devices.add(device);
                Log.d(TAG, "Receiver: " + device.getAddress() + " , " + device.getName());
            }
        }
    };
}
