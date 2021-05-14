package com.ble_ex1.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ble_ex1.Main;
import com.ble_ex1.R;

public class LogFragment extends Fragment {

    private TextView tv_logTitle;
    private ListView lv_log;

    private String[] fruit_name = new String[]{"Apple","Banana","Orange","Grape","Strawberry"};

    public LogFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        tv_logTitle = (TextView) view.findViewById(R.id.tv_logTitle);
        lv_log = (ListView) view.findViewById(R.id.lv_log);


        ArrayAdapter adapter = new ArrayAdapter(this.getActivity(), android.R.layout.simple_list_item_1, fruit_name);
        lv_log.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void setAdapter() {

    }
}
