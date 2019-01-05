package org.ea.main;

import org.ea.messages.data.VarLen;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.Arrays;

public class Utils {
    public static void printArray(String name, byte[] sendMessage) {
        System.out.println("BEGIN -----------" + name + "-----------");
        for(int i=1; i<sendMessage.length + 1; i++) {
            System.out.print(getByteStr(sendMessage[i-1]) + " ");
            if(i % 20 == 0) {
                System.out.println();
            }
        }
        System.out.println();
        System.out.println("END -----------" + name + "-----------");
    }

    public static String getByteStr(byte b) {
        String s = Integer.toHexString(b & 0XFF);
        s = s.toUpperCase();
        if(s.length() == 1) {
            s = "0" + s;
        }
        return s;
    }

    public static byte[] firstBytes(byte[] bytes, int len) {
        byte[] res = new byte[len];
        for(int i=0; i<len; i++) res[i] = bytes[i];
        return res;
    }

    public static byte[] dhash(byte[] message) throws Exception {
        return sha256(sha256(message));
    }

    public static byte[] sha256(byte[] message) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(message);
    }

    public static byte[] hashValue(byte[] message) throws Exception {
        return firstBytes(dhash(message), 4);
    }

    public static byte[] combine(byte[] a, byte b) {
        return combine(a, new byte[] {b});
    }

    public static byte[] combine(byte[] a, byte[] b) {
        byte[] keys = new byte[a.length + b.length];
        System.arraycopy(a, 0, keys, 0, a.length);
        System.arraycopy(b, 0, keys, a.length, b.length);
        return keys;
    }

    public static byte[] reverse(byte[] data) {
        for(int i = 0; i < data.length / 2; i++) {
            byte temp = data[i];
            data[i] = data[data.length - i - 1];
            data[data.length - i - 1] = temp;
        }
        return data;
    }

    public static byte[] getShortToBytes(short input) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(input).array();
    }

    public static byte[] getIntToBytes(int input) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(input).array();
    }

    public static byte[] getLongToBytes(long input) {
        return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(input).array();
    }

    public static byte[] getCommand(String cmd) {
        byte[] res = new byte[12];
        Arrays.fill(res, (byte)0);
        System.arraycopy(cmd.getBytes(), 0, res, 0, cmd.length());
        return res;
    }

    public static byte[] hex2Byte(String str) {
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < bytes.length; i++)
        {
            bytes[i] = (byte) Integer.parseInt(str.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp;
        for (int n = 0; n < b.length; n++)
        {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
            if (n < b.length - 1) {
                hs = hs + "";
            }
        }
        return hs;
    }

    public static void main(String[] args) {

        String hash = "A1 21 3B D4 75 4A 66 06 44 4B 97 B5 E8 C4 6E 9B 78 32 77 3F \n" +
                "F4 34 BD 5F 87 AC 45 BC 00 00 00 00 D1 E7 02 69 86 A9 CD 24 \n" +
                "7B 5B 85 A3 F3 0E CB AB B6 D6 18 40 D0 AB B8 1F 90 5C 41 1D \n" +
                "5F C1 45 E8 31 E8 49 4D FF FF 00 1D 00 41 38 F9 00 01 00 00 \n" +
                "00 ";

        byte[] msgByte = hex2Byte(
                hash
                    .replaceAll("[\n ]", "")
        );
        try {
            System.out.println(byte2hex(dhash(msgByte)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
