package com.smartlife.smartcart.classes;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class DeviceIO {
    public static boolean write(String fileName, String buffer) {
        try {
            FileOutputStream fout = new FileOutputStream(fileName);
            fout.write(buffer.getBytes());
            fout.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String read(String fileName) {
        String res = "";
        try {
            FileInputStream fin = new FileInputStream(fileName);
            byte[] buffer = new byte[fin.available()];
            int length = fin.read(buffer);
            String res2 = new String(buffer);
            try {
                res = String.copyValueOf(res2.toCharArray(), 0, length);
                fin.close();
            } catch (Exception e) {
                e = e;
                res = res2;
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            return res;
        }
        return res;
    }
}
