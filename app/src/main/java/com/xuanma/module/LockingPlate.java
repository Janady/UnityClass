package com.xuanma.module;

import com.unity3d.player.UnityPlayer;
import com.xuanma.core.BytesUtil;
import com.xuanma.core.CRC16Util;
import com.xuanma.log.Log;
import com.xuanma.serial.SerialHandler;
import com.xuanma.serial.listener.SerialReceiveListener;

import java.io.IOException;

public class LockingPlate implements SerialReceiveListener {
    private SerialHandler serial;
    LockingPlateListener _listener;
    private static LockingPlate sDefaultPayment;
    private byte header = (byte)0xEA;
    private byte tailer = (byte)0xFA;
    private byte headerRcv = (byte)0xAE;
    private byte tailerRcv = (byte)0xAF;
    private LockingPlate() {
        serial = new SerialHandler("/dev/ttyS2", 115200, this);
        serial.active();
    }
    protected void finalize() {
        serial.inActive();
    }
    public static LockingPlate getDefault() {
        if (sDefaultPayment == null) {
            synchronized (LockingPlate.class) {
                if (sDefaultPayment == null) {
                    sDefaultPayment = new LockingPlate();
                }
            }
        }
        return sDefaultPayment;
    }

    @Override
    public void reveive(byte[] val) {
        Log.i(this, "reveive: " + BytesUtil.BytesToHex(val));

        for (int i=0; i<=val.length; i++) {
            int len = 0xff & val[1+i];
            if (headerRcv == val[i]
                    && tailerRcv == val[i + len - 1]) {
                byte[] dest = new byte[len];
                System.arraycopy(val, i, dest, 0, len);
                i += len;
                parse(dest);
            }
        }
    }
    private void transfer(byte[] val) {
        try {
            Log.i(this, BytesUtil.BytesToHex(val));
            serial.transfer(val);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private byte[] packet(byte cmd) {
        return packet((byte)0x00, cmd);
    }
    private byte[] packet(byte func, byte cmd) {
        return packet(func, cmd, (byte)0x00);
    }
    private byte[] packet(byte func, byte cmd, byte val) {
        byte len = 6;
        byte[] raw = new byte[len];
        raw[0] = header;
        raw[len - 1] = tailer;
        raw[1] = len;
        raw[2] = func;
        raw[3] = cmd;
        raw[4] = val;

        return raw;
    }
    private byte[] packet(byte func, byte cmd, byte[] value) {
        byte len = (byte)(0xff & (5 + value.length));
        byte[] raw = new byte[len];
        raw[0] = header;
        raw[len - 1] = tailer;
        raw[1] = len;
        raw[2] = func;
        raw[3] = cmd;
        System.arraycopy(value, 0, raw, 4, value.length);

        return raw;
    }
    private byte[] packetOfPoll(byte id) {
        return packet(CMD.POLL.getVal(), id);
    }
    private byte[] packetOfUnlock(byte id) {
        return packet(CMD.UNLOCK.getVal(), id);
    }
    private byte[] packetOfCheck() {
        return packet((byte)0x00, (byte)0x01, (byte)0x01);
    }
    public void check() {
        byte[] pac = packetOfCheck();
        transfer(pac);
    }
    public void unlock(int id) {
        byte[] pac = packetOfUnlock((byte)(id & 0xff));
        transfer(pac);
    }
    public void poll(int id) {
        byte[] pac = packetOfPoll((byte)(id & 0xff));
        transfer(pac);
    }

    private void parse(final byte[] buf) {
        UnityPlayer.UnitySendMessage("InternalMsgManager", "SerialMsg", BytesUtil.BytesToHex(buf));
    }
    private void handlerUnlockRev(byte index, byte val) {
        Log.i(this,"handlerUnlockRev: " + index + "-" + val);
        LockingPlateListener.LockingStatus ls;
        switch (val) {
            case (byte)0x01:
                ls = LockingPlateListener.LockingStatus.SUCCESS;
                break;
            case (byte)0x02:
                ls = LockingPlateListener.LockingStatus.FAIL;
                break;
            case (byte)0x03:
                ls = LockingPlateListener.LockingStatus.NoAddr;
                break;
            case (byte)0x04:
                ls = LockingPlateListener.LockingStatus.USELESS;
                break;
                default:
                    return;
        }
        if (_listener != null) {
            _listener.OnCallback(index, ls);
        }
    }
    public void setOnRcvListener(LockingPlateListener listener) {
        _listener = listener;
    }
    private enum CMD {
        POLL((byte)0xa1), UNLOCK((byte)0xa2), CHECK((byte)0xa0);
        private byte val;
        private CMD(byte val) {
            this.val = val;
        }

        public byte getVal() {
            return val;
        }
        @Override
        public String toString() {
            return "CMD: " + this.val;
        }
    }
}
