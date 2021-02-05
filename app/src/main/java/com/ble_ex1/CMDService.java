package com.ble_ex1;
import android.util.Log;
public class CMDService {

    private final static String TAG = "CMDService";
    private StringBuffer buffer = new StringBuffer();
    
    public BLEService.BufferListener listener = new BLEService.BufferListener() {
        public void onData(int status, byte[] byteArray) {
            String str = new String(byteArray);
            int _start = str.lastIndexOf("$");
            int _end = str.indexOf("#");

            if (_start != -1 && _end != -1) {
                str = str.substring(_start + 1, _end);
                Log.d(TAG, "str: " + str);

            } else if (_start != -1) {
                buffer = new StringBuffer();
                buffer.append(str.substring(_start + 1, str.length()));
            } else if (_end != -1) {
                buffer.append(str.substring(0, _end));
                String temp = buffer.toString();
                Log.d(TAG, "str: " + temp);
                buffer = new StringBuffer();
            } else {
                buffer.append(str);
            }
        }
    };

    public BLEService.BufferListener getListener() {
        return listener;
    }
}
