package com.ble_ex1;

import android.util.Log;

import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Tools {

    private final static String AESKey = "billion35753268";

    public static String Encrypt(String src) throws Exception {
        try {
            byte[] temp = AESKey.getBytes("utf-8");
            byte[] raw = new byte[16];
            System.arraycopy(temp, 0, raw, 0, temp.length);

            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            String hexStr = byte2Hex(cipher.doFinal(string2Hex(src)));

            return hexStr.substring(0, hexStr.length()/2);
        } catch (Exception ex) { System.out.println(ex.toString()); }
        return null;
    }

    public static byte[] string2Hex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i+=2) {
            bytes[i==0?0:(i/2)] = (byte) (Integer.parseInt(hex.substring(i, i + 2), 16) & 0xff);
        }
        return bytes;
    }

    public static String byte2Hex(byte[] bytes) {
        String result = "";
        for (int i=0 ; i<bytes.length ; i++)
            result += Integer.toString( ( bytes[i] & 0xff ) + 0x100, 16).substring( 1 );
        return result.toUpperCase();
    }
}
