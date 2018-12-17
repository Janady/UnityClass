package com.xuanma.serial;

import android.util.Log;

import com.xuanma.core.BytesUtil;
import com.xuanma.serial.listener.SerialReceiveListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SerialHandler {
    SerialReceiveListener _listener;
    SerialPort sp;
    private File _path;
    private int _boudrate = 115200;
    private boolean isActive = false;
    public SerialHandler(String pathStr, int boudrate) {
        this._boudrate = boudrate;
        this._path = new File(pathStr);
    }
    public SerialHandler(String pathStr, int boudrate, SerialReceiveListener ls) {
        this._boudrate = boudrate;
        this._path = new File(pathStr);
        this._listener = ls;
    }
    public boolean active() {
        try {
            sp = new SerialPort(_path, _boudrate, 0);
            // observer.startWatching();
            isActive = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isActive){
                        OnSerialReceived();
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public void inActive() {
        isActive = false;
        sp.close();
        sp = null;
    }

    public void transfer(byte[] value) throws IOException {
        FileOutputStream mOutputStream=(FileOutputStream) sp.getOutputStream();
        mOutputStream.write(value);
        mOutputStream.flush();
    }

    private void OnSerialReceived() {
        try {
            FileInputStream fis =(FileInputStream) sp.getInputStream();
            if (fis == null) return;

            int available = fis.available();
            if (available <= 0) return;
            byte[] buffer = new byte[available];
            int size = fis.read(buffer, 0, available);
            if (size > 0 && _listener != null) {
                _listener.reveive(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
