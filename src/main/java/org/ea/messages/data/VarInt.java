package org.ea.messages.data;

import org.ea.main.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class VarInt {
    private long value;
    private int numBytes;
    private byte[] bytes;

    public VarInt(long data) {
        if(data > Integer.MAX_VALUE) {
            numBytes = 9;
            bytes = Utils.combine(new byte[] {(byte)0xFF}, Utils.getLongToBytes(data));
        } else if(data > Short.MAX_VALUE) {
            numBytes = 5;
            bytes = Utils.combine(new byte[] {(byte)0xFE}, Utils.getIntToBytes((int)data));
        } else if(data > Byte.MAX_VALUE) {
            numBytes = 3;
            bytes = Utils.combine(new byte[] {(byte)0xFD}, Utils.getShortToBytes((short)data));
        } else {
            numBytes = 1;
            bytes = new byte[] {(byte)data};
        }
        value = data;
    }
    public VarInt(byte[] data) {
        if((data[0] & 0xFF) == 0xFF) {
            numBytes = 9;
            bytes = Arrays.copyOfRange(data, 1, 9);
        } else if((data[0] & 0xFF) == 0xFE) {
            numBytes = 5;
            bytes = Arrays.copyOfRange(data, 1, 5);
            byte[] padding = new byte[4];
            Arrays.fill(padding, (byte) 0);
            bytes = Utils.combine(bytes, padding);
        } else if((data[0] & 0xFF) == 0xFD) {
            numBytes = 3;
            bytes = Arrays.copyOfRange(data, 1, 3);
            byte[] padding = new byte[6];
            Arrays.fill(padding, (byte) 0);
            bytes = Utils.combine(bytes, padding);
        } else {
            numBytes = 1;
            bytes = Arrays.copyOfRange(data, 0, 1);
            byte[] padding = new byte[7];
            Arrays.fill(padding, (byte) 0);
            bytes = Utils.combine(bytes, padding);
        }

        value = ByteBuffer.wrap(bytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getLong();
    }

    public long getValue() {
        return value;
    }

    public int getNumBytes() {
        return numBytes;
    }

    public byte[] getBytes() {
        byte[] extraByte = new byte[0];
        switch (numBytes) {
            case 9:
                extraByte = new byte[] {(byte)0xFF};
                break;
            case 5:
                extraByte = new byte[] {(byte)0xFE};
                break;
            case 3:
                extraByte = new byte[] {(byte)0xFD};
                break;
            case 1:
                return Arrays.copyOfRange(bytes, 0, 1);
            default:
        }

        return Utils.combine(
            extraByte,
            Arrays.copyOfRange(bytes, 0, numBytes - 1)
        );
    }
}
