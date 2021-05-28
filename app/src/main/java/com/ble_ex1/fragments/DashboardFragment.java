package com.ble_ex1.fragments;

import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ble_ex1.Global;
import com.ble_ex1.R;
import com.ble_ex1.cmd_module.Command;
import org.json.JSONObject;
import java.util.Iterator;

public class DashboardFragment extends Fragment {

    private final static String TAG = "DashboardFragment";
    private TextView tv_info = null, tv_bleInfo = null;

    public DashboardFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        tv_info = (TextView) root.findViewById(R.id.tv_info);
        tv_bleInfo = (TextView) root.findViewById(R.id.tv_bleInfo);
        displayBleInfo();
        displayDeviceInfo();
        return root;
    }

    public void displayBleInfo() {
        try {
            String str = "";
            if(Global.bleConnectStatus()) {
                BluetoothDevice device = Global.getConnectDevice();
                str += "ble name: " + device.getName() + "\r\n";
                str += "ble address: " + device.getAddress() + "\r\n";
                str += "ble rssi: " + "" + "\r\n";
            }

            tv_bleInfo.setText(str);
        } catch (Exception ex) { Log.e(TAG, ex.toString()); }
    }

    public void displayDeviceInfo() {
        try {
            Command cmd = Global.getCommandTypeList("get_lteinfo");
            if(cmd != null) {
                JSONObject info = cmd.getContent();
                String str = "get_lteinfo : \r\n";
                for(int i = 0; i<info.names().length(); i++){
                    String key = info.names().getString(i);
                    str += key + " : " + info.optString(key, "") + "\r\n";
                }
                tv_info.setText(str);
            }
        } catch (Exception ex) { Log.e(TAG, ex.toString()); }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
