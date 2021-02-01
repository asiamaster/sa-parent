package com.sa.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class ByteArrayUtils {


    public static short[] bytes2short(byte[] bytes) {
        if(bytes == null){
            return null;
        }
        short[] shorts = new short[bytes.length/2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }


    public static byte[] short2bytes(Short shorts) {
        if(shorts == null){
            return null;
        }
        byte[] bytes = new byte[2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts);
        return bytes;
    }


    public static short[] bytes2short2(byte[] bytes) {
        if(bytes == null){
            return null;
        }
        short[] shorts = new short[bytes.length/2];
        ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }


    public static byte[] short2bytes2(Short shorts) {
        if(shorts == null){
            return null;
        }
        byte[] bytes = new byte[2];
        ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asShortBuffer().put(shorts);
        return bytes;
    }


    public static byte[] int2byte(int res) {
        byte[] targets = new byte[4];
        targets[0] = (byte) (res & 0xff);
        targets[1] = (byte) ((res >> 8) & 0xff);
        targets[2] = (byte) ((res >> 16) & 0xff);
        targets[3] = (byte) (res >>> 24);
        return targets;
    }


    public static int byte2int(byte[] res) {

        int targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00)
                | ((res[2] << 24) >>> 8) | (res[3] << 24);
        return targets;
    }


    public static byte[] int2byte2(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }


    public static int byte2int2(byte[] bytes) {
        int value=0;
        for(int i = 0; i < 4; i++) {
            int shift= (3-i) * 8;
            value +=(bytes[i] & 0xFF) << shift;
        }
        return value;
    }

    public static String toHexString(byte[] ba) {
        StringBuilder sbuf = new StringBuilder();
        for (byte b : ba) {
            String s = Integer.toHexString((int) (b & 0xff));
            if (s.length() == 1) {
                sbuf.append('0');
            }
            sbuf.append(s);
        }
        return sbuf.toString();
    }

    public static byte[] hexString2bytearray(String s) {
        int len = s.length();
        byte[] ba = new byte[len / 2];

        for (int i = 0; i < ba.length; i++) {
            int j = i * 2;
            int t = Integer.parseInt(s.substring(j, j + 2), 16);
            byte b = (byte) (t & 0xFF);
            ba[i] = b;
        }
        return ba;
    }

    public static byte[] toPrimitives(Byte[] objBytes)
    {
        byte[] bytes = new byte[objBytes.length];
        for(int i = 0; i < objBytes.length; i++) {
            bytes[i] = objBytes[i];
        }
        return bytes;
    }


    public static Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];
        int i = 0;
        for (byte b : bytesPrim) {

            bytes[i++] = b;
        }
        return bytes;
    }
}
