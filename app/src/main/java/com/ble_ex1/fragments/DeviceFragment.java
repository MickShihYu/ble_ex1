package com.ble_ex1.fragments;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.ble_ex1.Global;
import com.ble_ex1.R;
import java.util.ArrayList;
import java.util.List;

public class DeviceFragment extends Fragment{

    public static final String TAG = DeviceFragment.class.getSimpleName();
    private Activity activity = null;
    private DeviceAdapter deviceAdapter = null;
    private ListView listView;
    private List<BleDevice> devices = new ArrayList<BleDevice>();
    public DeviceFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();

        Global.scanLeDevice(bleScanCallback);

        View view = inflater.inflate(R.layout.fragment_device, container, false);
        initUI(view);
        return view;
    }

    private void initUI(View view) {
        deviceAdapter = new DeviceAdapter(activity, devices);
        listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(deviceAdapter);
    }

    private BluetoothAdapter.LeScanCallback bleScanCallback = new BluetoothAdapter.LeScanCallback() {
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            //if (device != null) {
            if (device != null && device.getName()!=null && device.getName().length()>0) {
                //Log.d(TAG, "" + device.getAddress() + " , " + device.getName());
                boolean exit = false;
                for(BleDevice bleDevice: devices) {
                    if(bleDevice.address.equals(device.getAddress())) {
                        bleDevice.rssi = rssi;
                        exit = true;
                    }
                }

                if(!exit) { devices.add(new BleDevice(device.getName(), device.getAddress(), rssi)); }
                deviceAdapter.updateResults(devices);
            }
        }
    };
}

class BleDevice {
    public String name = "";
    public String address = "";
    public boolean connectStatus = false;
    public int rssi = 0;
    public BleDevice(String name, String address, int rssi) {
        this.name = name;
        this.address = address;
        this.rssi = rssi;
        this.connectStatus = connectStatus;
    }
}

class ViewHolder {
    TextView tv_name;
    TextView tv_address;
    TextView tv_rssi;
    Button btn_connect;
}

class DeviceAdapter extends BaseAdapter {
    private List<BleDevice> devices = null;
    private LayoutInflater inflater = null;
    private Context context = null;
    public DeviceAdapter(final Context context, final List<BleDevice> devices) {
        this.context = context;
        this.devices = devices;
        inflater = LayoutInflater.from(context);
    }

    public void updateResults(List<BleDevice> devices) {
        this.devices = devices;
        notifyDataSetChanged();
    }
    public List<BleDevice> getData() {
        return devices;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.ble_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_device_name);
            viewHolder.tv_address = (TextView) convertView.findViewById(R.id.tv_device_address);
            viewHolder.tv_rssi = (TextView) convertView.findViewById(R.id.tv_rssi);
            viewHolder.btn_connect = (Button) convertView.findViewById(R.id.btn_ble_connect);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final BleDevice bleDevice = devices.get(position);

        viewHolder.btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(DeviceFragment.TAG, "onItemClick !!!");

                if(!bleDevice.connectStatus) Global.connectBluetooth(bleDevice.address);
                else closeConnectDialog();
            }
        });

        if(bleDevice != null) {
            viewHolder.tv_name.setText(bleDevice.name);
            viewHolder.tv_address.setText(bleDevice.address);
            viewHolder.tv_rssi.setText("" + bleDevice.rssi + "db");
            BluetoothDevice connectDevice = Global.getConnectDevice();
            boolean isCurrentDevice = connectDevice != null && connectDevice.getAddress().equals(bleDevice.address) ? true: false;
            bleDevice.connectStatus = isCurrentDevice && Global.bleConnectStatus();
            viewHolder.btn_connect.setText(!bleDevice.connectStatus ? "Disconnect" : "Connected");
        }
        return convertView;
    }

    public void closeConnectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you want close ble connected?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Global.closeBluetooth();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


}