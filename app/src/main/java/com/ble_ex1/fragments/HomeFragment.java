package com.ble_ex1.fragments;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.ble_ex1.Device;
import com.ble_ex1.Global;
import com.ble_ex1.R;
import com.ble_ex1.cmd_module.CmdAPI;
import com.ble_ex1.cmd_module.CmdListener;
import com.ble_ex1.cmd_module.CmdObserver;
import com.ble_ex1.cmd_module.Command;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {

    private static final String TAG = DashboardFragment.class.getSimpleName();

    private TextView text_rx = null;
    private EditText edit_rx = null;
    private Button btn_tx = null, btn_write;
    private ImageView img_status_led = null;

    private Activity activity = null;


    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initUI(view);
        return view;
    }


    public void initUI(View view) {
        img_status_led = (ImageView) view.findViewById(R.id.img_status_led);
        text_rx = (TextView)view.findViewById(R.id.text_rx);
        edit_rx = (EditText)view.findViewById(R.id.edit_tx);

        btn_tx = (Button)view.findViewById(R.id.btn_tx);
        btn_tx.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    String str = edit_rx.getText().toString();
                    JSONObject obj = new JSONObject();
                    obj.put("cmd", "login");

                    Command command = CmdAPI.CreateCommand(obj.toString());
                    //Global.sendCommand(command);
                    command = Global.syncCommand(command);

                    Log.d(TAG, "sync rev: " + command.toString());

                } catch (Exception ex) {}
            }
        });

        btn_write = (Button)view.findViewById(R.id.btn_write);
        btn_write.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    String cmd = edit_rx.getText().toString();
                    int number = Integer.parseInt(cmd);
                    if(number>0) {
                        String str = "";
                        for(int i=0;i<number;i++) str+="A";
                        //Global.writeBLE(CmdAPI.CreateCommand(new JSONObject().put("number", str).toString()));
                    }
                } catch (Exception ex) {Log.d(TAG, ex.toString());}
            }
        });
    }

}
