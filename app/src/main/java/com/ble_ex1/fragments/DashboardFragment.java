package com.ble_ex1.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ble_ex1.Main;
import com.ble_ex1.R;

public class DashboardFragment extends Fragment {

    private TextView mTextTitle;

    public DashboardFragment() {
        // Requires empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        mTextTitle = (TextView) root.findViewById(R.id.text_title_dashboard);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextTitle.setText("Now I see u!");
        mTextTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Main) getActivity()).onOpenDetail("Open from Notifications!");
            }
        });
    }
}
