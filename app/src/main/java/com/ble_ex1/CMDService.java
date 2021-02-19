package com.ble_ex1;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CMDService {
    private static final String TAG = "CMDService";

    private List<Command> revCommands = null;
    private static final int SearchCountMax = 3;

    public CMDService() {
        revCommands = new ArrayList<>();
    }

    public void receive(Command command) {
        revCommands.add(command);
        if(revCommands.size()>50) revCommands.remove(0);
    }

    public void write(Command command, CommandListener listener) {
        if(command == null || listener == null) return;
        try {
            int serachCount = 0;
            while(serachCount++<SearchCountMax) {
                for(int i=revCommands.size()-1;i>=0;i--) {
                    if(command.getName().equals(revCommands.get(i).getName()) && command.getTime()>revCommands.get(i).getTime()) {
                        listener.onData(Global.BLE_EXECUTE, revCommands.get(i).execute());
                        revCommands.remove(i);
                        return;
                    }
                }
                Thread.sleep(1000);
            }
        } catch (Exception ex) { System.out.println(ex.toString()); }

        listener.onData(Global.BLE_TIME_OUT, null);
    }

}
