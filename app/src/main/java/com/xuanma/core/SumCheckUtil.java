package com.xuanma.core;

public class SumCheckUtil {
    public static byte[] getSumCheck16(byte[] arr_buff) {
        int rst = 0;
        for (byte val : arr_buff) {
            rst += (0xff&val);
        }
        return intToBytes(rst);
    }

    /**
     * 将int转换成byte数组，低位在前，高位在后
     * 改变高低位顺序只需调换数组序号
     */
    private static byte[] intToBytes(int value) {
        byte[] src = new byte[2];
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }
}
