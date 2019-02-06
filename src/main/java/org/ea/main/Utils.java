package org.ea.main;

import org.ea.messages.data.Header;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.*;

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
        byte[] newData = new byte[data.length];
        for(int i = 0; i < data.length / 2; i++) {
            newData[i] = data[data.length - i - 1];
            newData[data.length - i - 1] = data[i];
        }
        return newData;
    }

    public static String reverse(String data) {
        return Utils.byte2hex(Utils.reverse(Utils.hex2Byte(data)));
    }

    public static byte[] getShortToBytes(short input) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(input).array();
    }

    public static byte[] getIntToBytes(int input) {
        return getIntToBytes(input, false);
    }

    public static byte[] getIntToBytes(int input, boolean bigEndian) {
        if(bigEndian) {
            return ByteBuffer.allocate(4).putInt(input).array();
        } else {
            return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(input).array();
        }
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

        String hash = "43 49 7F D7 F8 26 95 71 08 F4 A3 0F D9 CE C3 AE BA 79 97 20 \n" +
                "84 E9 0E AD 01 EA 33 09 00 00 00 00 BA C8 B0 FA 92 7C 0A C8 \n" +
                "23 42 87 E3 3C 5F 74 D3 8D 35 48 20 E2 47 56 AD 70 9D 70 38 \n" +
                "FC 5F 31 F0 20 E7 49 4D FF FF 00 1D 03 E4 B6 72 00 01 00 00";

        String bla = "F8 26 95 71 08 F4 A3 0F D9 CE C3 AE BA 79 97 20 84 E9 0E AD 01 EA 33 09 00 00 00 00 BA C8 B0 FA";
        System.out.println(bla      .replaceAll("[\n ]", ""));


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

    public static int indexOf(byte[] haystack, byte needle) {
        int index = 0;
        for(byte b : haystack) {
            if(b == needle) return index;
            index++;
        }
        return -1;
    }

    public static void handleHeights(File headersFile, List<Header> list) throws Exception {
        if(list.size() == 0) return;
        String nextBlock = "43497FD7F826957108F4A30FD9CEC3AEBA79972084E90EAD01EA330900000000".toLowerCase();
        long currentHeight = 0;

        if(headersFile.exists()) {
            RandomAccessFile raf = new RandomAccessFile(headersFile, "r");

            if (headersFile != null && headersFile.length() > 0) {
                currentHeight = headersFile.length() / 80;
                raf.seek((currentHeight - 1) * 80);
                byte[] headerBytes = new byte[80];
                raf.read(headerBytes);
                Header startHeader = new Header(headerBytes);
                nextBlock = startHeader.getId();
            }
        }

        List<Header> newHeaders = new ArrayList<>();
        for (Header header : list) {
            if (nextBlock.equalsIgnoreCase(header.getPrevBlock())) {
                nextBlock = header.getId();
                newHeaders.add(header);
            }
        }

        if(newHeaders.size() == 0 && list.size() > 0) {
            File newHeaderFile = new File(headersFile.getParentFile(), "header.new");
            FileInputStream fis = new FileInputStream(headersFile);
            byte[] buffer = new byte[8000];

            FileOutputStream fos = new FileOutputStream(newHeaderFile);

            int numRead;
            int foundValues = 0;
            reading:
            while((numRead = fis.read(buffer)) != -1) {
                for(int i = 0; i < numRead / 80; i++) {
                    byte[] headerBytes = Arrays.copyOfRange(buffer, i * 80, (i+1) * 80);
                    Header startHeader = new Header(headerBytes);
                    if(list.get(0).getPrevBlock().equals(startHeader.getId())) {
                        foundValues = i;
                        break reading;
                    }
                }
                fos.write(buffer);
            }

            for(int i = 0; i < foundValues / 80; i++) {
                byte[] headerBytes = Arrays.copyOfRange(buffer, i * 80, (i+1) * 80);
                fos.write(headerBytes);
            }

            for (Header header : list) {
                if (nextBlock.equalsIgnoreCase(header.getPrevBlock())) {
                    fos.write(header.getHeaderData());
                    nextBlock = header.getId();
                }
            }

            fos.flush();
            fos.close();

            newHeaderFile.renameTo(headersFile);

        } else {
            FileOutputStream fos = new FileOutputStream(headersFile, true);
            for (Header header : newHeaders) {
                fos.write(header.getHeaderData());
            }
            fos.flush();
            fos.close();
        }

        System.out.println("Have " + (headersFile.length() / 80) + " headers");
    }



    public static List<Header> blockLocator(File headersFile) throws Exception {
        if(!headersFile.exists()) {
            List<Header> justOne = new ArrayList<>();
            byte[] rev = Utils.hex2Byte("43497FD7F826957108F4A30FD9CEC3AEBA79972084E90EAD01EA330900000000");
            Header genesisIV = new Header(1, rev);
            justOne.add(genesisIV);
            return justOne;
        }
        RandomAccessFile raf = new RandomAccessFile(headersFile, "r");
        List<Header> indexes = new ArrayList<>();
        int step = 1;

        long topHeight = headersFile.length() / 80;

        byte[] header = new byte[80];

        // Start at the top of the chain and work backwards.
        for (long index = topHeight - 1; index > 0; index -= step) {
            // Push top 10 indexes first, then back off exponentially.
            if (indexes.size() >= 10)
                step *= 2;

            raf.seek(index * 80);
            raf.read(header);
            indexes.add(new Header(header));
        }

        byte[] rev = Utils.hex2Byte("43497FD7F826957108F4A30FD9CEC3AEBA79972084E90EAD01EA330900000000");
        Header genesisIV = new Header(1, rev);
        indexes.add(genesisIV);

        return indexes;
    }

    public static File getDataPath() {
        return new File("data");
    }

    public static boolean findFileName(String id) {
        File blockDir = new File(getDataPath(), "blocks");
        if(!blockDir.exists()) return false;
        File dir = new File(blockDir, id.substring(0, 4));
        if(!dir.exists()) return false;

        File file = new File(dir, id);
        return file.exists();
    }

    public static File findFile(String id) {
        File blockDir = new File(getDataPath(), "blocks");
        if(!blockDir.exists()) return null;
        File dir = new File(blockDir, id.substring(0, 4));
        if(!dir.exists()) return null;

        return new File(dir, id);
    }

    public static String getId(byte[] bytes) throws Exception {
        if(bytes.length != 80) return null;
        return byte2hex(dhash(bytes));
    }
}
