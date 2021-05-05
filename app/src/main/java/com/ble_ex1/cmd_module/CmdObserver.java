package com.ble_ex1.cmd_module;

public class CmdObserver implements Observer {

    private String name = null;
    private Observable cmdService = null;
    private CmdListener listener = null;

    public CmdObserver(String name){
        this.name = name;
    }

    public CmdObserver(String name, CmdListener listener){
        this.name = name;
        this.listener = listener;
    }

    public void setListener(CmdListener listener) { this.listener = listener; }

    @Override
    public void subscribe(Observable cmdService) {
        cmdService.register(this);
    }

    @Override
    public void unsubscribe() {
        cmdService.unregister(this);
    }

    @Override
    public void update(Command cmd) {
        if(listener!=null) listener.onData(cmd);
    }
}
