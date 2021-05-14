package com.ble_ex1.fragments;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
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
import com.ble_ex1.Main;
import com.ble_ex1.R;
import com.ble_ex1.Tools;
import com.ble_ex1.cmd_module.CmdListener;
import com.ble_ex1.cmd_module.CmdObserver;
import com.ble_ex1.cmd_module.Command;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogFragment extends Fragment {
    private static final String TAG = Main.class.getSimpleName();
    private Activity activity = null;
    private ListView lv_log = null;
    private LogAdapter mAdapter = null;
    private CmdObserver cmdObserver = null;
    private Button btn_clear = null;

    public LogFragment() {
        //initCmdService();
    }

    public void initCmdService() {
        if(cmdObserver == null) {
            cmdObserver = new CmdObserver("main", new CmdListener() {
                @Override
                public void onData(String status, Object obj) {
                    if(status.equals(Global.BLE_CHARACTERISTIC)) {
                        reloadData((Command) obj);
                    }
                }
            });
            Global.registerCmdService(cmdObserver);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        activity = this.getActivity();

        lv_log = (ListView) view.findViewById(R.id.lv_log);
        btn_clear = (Button) view.findViewById(R.id.btn_clear);
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.clearCommandHistory();
                setAdapter();
            }
        });

        initCmdService();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setAdapter();
    }

    private void setAdapter() {
        try {
            List<Command> cmdList = Global.getCommandHistory();
            List<Command> list = new ArrayList<>();
            list.addAll(cmdList);
            mAdapter = new LogAdapter(activity, list);
            lv_log.setAdapter(mAdapter);
        } catch (Exception ex) { Log.e(TAG, ex.toString()); }
    }

    private void reloadData(final Command data){
        activity.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    List<Command> cmdList = Global.getCommandHistory();
                    mAdapter.getData().clear();
                    mAdapter.getData().addAll(cmdList);
                    mAdapter.notifyDataSetChanged();
                } catch (Exception ex) { Log.e(TAG, ex.toString()); }
            }
        });
    }
}

class LogAdapter extends BaseAdapter {
    private List<Command> data = null;
    private final Context mContext;
    public LogAdapter(final Context context, final List<Command> mData) {
        this.data = mData;
        this.mContext = context;
    }

    public List<Command> getData() {
        return data;
    }

    @Override
    public int getCount() {
        return data.size();
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
        TextView textView = new TextView(mContext);
        Command cmd = data.get(position);
        if(cmd != null) {
            boolean status = cmd.getStatus();
            String str = Tools.getDate(cmd.getTime()) + "\r\n" + (status ? "rev : " : "snd : ") + "" + cmd.toString();
            textView.setText(str);
        }
        return textView;
    }
}