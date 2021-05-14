package com.ble_ex1.cmd_module;

public class CmdObserver implements Observer {

    private String name = null;
    private Observable observable = null;
    private CmdListener listener = null;

    public CmdObserver(String name, CmdListener listener){
        this.name = name;
        this.listener = listener;
    }

    @Override
    public void subscribe(Observable observable) {
        this.observable = observable;
        this.observable.register(this);
    }

    @Override
    public void unsubscribe() {
        observable.unregister(this);
    }

    @Override
    public void update(String status, Object obj) {
        if(listener!=null) listener.onData(status, obj);
    }
}
