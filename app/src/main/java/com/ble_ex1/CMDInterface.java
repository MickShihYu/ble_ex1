package com.ble_ex1;

interface CMDSubject {
    void attach(CMDObserver observer);
    void detach(CMDObserver observer);
    void replyObservers();
    void executeObservers();
}

interface CMDObserver {
    boolean reply();
    boolean execute();
}