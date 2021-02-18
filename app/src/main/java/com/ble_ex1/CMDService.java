package com.ble_ex1;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

public class CMDService implements CMDSubject{

    private String subjectState = null;
    private List<CMDObserver> observers = null;

    public CMDService() {
        observers = new ArrayList<>();
    }

    @Override
    public void attach(CMDObserver observer) {
        observers.add(observer);
    }

    @Override
    public void detach(CMDObserver observer) {
        observers.remove(observer);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void replyObservers() {
        observers.forEach(CMDObserver::reply);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void executeObservers() {
        observers.forEach(CMDObserver::execute);
    }



}
