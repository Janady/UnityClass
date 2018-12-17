package com.xuanma.module;

import com.xuanma.core.BytesUtil;
import com.xuanma.core.CRC16Util;
import com.xuanma.log.Log;
import com.xuanma.serial.SerialHandler;
import com.xuanma.serial.listener.SerialReceiveListener;

import org.simple.eventbus.EventBus;

import java.io.IOException;

public class LockingPlate implements SerialReceiveListener {
    private SerialHandler serial;
    LockingPlateListener _listener;
    private static LockingPlate sDefaultPayment;
    private byte[] header = {(byte)0xfa, (byte)0xce};
    private byte[] tailer = {(byte)0xaf, (byte)0xec};
    private byte[] headerRcv = {(byte)0xfb, (byte)0xcb};
    private byte[] tailerRcv = {(byte)0xbf, (byte)0xbc};
    private LockingPlate() {
        serial = new SerialHandler("/dev/ttyS5", 115200, this);
        serial.active();
    }
    protected void finalize() {
        serial.inActive();
    }
    public static LockingPlate getDefault() {
        if (sDefaultPayment == null) {
            synchronized (Payment.class) {
                if (sDefaultPayment == null) {
                    sDefaultPayment = new LockingPlate();
                }
            }
        }
        return sDefaultPayment;
    }

    private static final int LEN = 10;
    @Override
    public void reveive(byte[] val) {
        Log.i(this, "reveive" + BytesUtil.BytesToHex(val));
        for (int i=0; i<=val.length-LEN; i++) {
            if (BytesUtil.compareBytes(val, i, headerRcv)
                    && BytesUtil.compareBytes(val, i + LEN - tailerRcv.length, tailerRcv)) {

                byte[] dest = new byte[LEN];
                System.arraycopy(val, i, dest, 0, LEN);
                i+=LEN;
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
        return packet((byte)0x01, cmd, (byte)0x00);
    }
    private byte[] packet(byte cmd, byte value) {
        return packet((byte)0x01, cmd, value);
    }
    private byte[] packet(byte addr, byte cmd, byte value) {
        byte[] raw = new byte[header.length + 3];
        int i = 0;
        BytesUtil.appendBytes(raw, 0, header);
        i += header.length;
        raw[i++] = cmd;
        raw[i++] = addr;
        raw[i++] = value;
        byte[] crc = CRC16Util.appendCrc16(raw);

        return BytesUtil.appendBytes(crc, tailer);
    }
    private byte[] packetOfPoll(byte id) {
        return packet(CMD.POLL.getVal(), id);
    }
    private byte[] packetOfUnlock(byte id) {
        return packet(CMD.UNLOCK.getVal(), id);
    }
    private byte[] packetOfCheck() {
        return packet(CMD.CHECK.getVal());
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
        byte cmd = buf[2];
        byte addr = buf[3];
        byte index = buf[4];
        byte value = buf[5];
        switch (cmd) {
            case (byte)0xA2:
                handlerUnlockRev(index, value);
                break;
        }
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
