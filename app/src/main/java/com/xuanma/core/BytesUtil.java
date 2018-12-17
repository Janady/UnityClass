package com.xuanma.core;

public class BytesUtil {
    /**
     * 获取两个字节数组的组合byte数组
     *
     * @param head 字节数组, 头部
     * @param tail 字节数组，尾部
     * @return 字节数组
     */
    public static byte[] appendBytes(final byte[] head, final byte[] tail) {
        byte[] dest = new byte[head.length + tail.length];
        System.arraycopy(head, 0, dest, 0, head.length);
        System.arraycopy(tail, 0, dest, head.length, tail.length);
        return dest;
    }
    public static void appendBytes(byte[] src, int index, final byte[] buf) {
        if (src.length < index + buf.length) return;
        System.arraycopy(buf, 0, src, index, buf.length);
    }
    public static boolean compareBytes(byte[] src, int index, final byte[] calc) {
        if (src.length < index + calc.length) return false;
        for (int i=0; i<calc.length; i++) {
            if (calc[i] != src[index+i]) return false;
        }
        return true;
    }

    private static char[] hexmap = {'0', '1', '2', '3', '4','5','6','7','8','9','A','B','C','D','E','F'};

    public static String BytesToHex(final byte[] buffer) {
        return BytesToHex(buffer, buffer.length);
    }
    public static String BytesToHex(final byte[] buffer, final int size) {
        StringBuffer strBuffer = new StringBuffer();
        for (int i = 0; i < size; i++) {
            strBuffer.append(hexByteToChar(buffer[i]));
        }
        return strBuffer.toString();
    }
    private static String hexByteToChar(byte b) {
        return "" + hexmap[(b>>4)&0x0f] + hexmap[b&0x0f];
    }
}
