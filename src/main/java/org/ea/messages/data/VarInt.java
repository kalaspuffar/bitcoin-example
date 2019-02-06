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
            bytes = Utils.getLongToBytes(data);
        } else if(data > Short.MAX_VALUE) {
            bytes = Utils.getIntToBytes((int)data);
        } else if(data > Byte.MAX_VALUE) {
            bytes = Utils.getShortToBytes((short)data);
        } else {
            bytes = new byte[] {(byte)data};
        }
        value = data;
    }
    public VarInt(byte[] data) {
        if((data[0] & 0xFF) == 0xFF) {
            bytes = Arrays.copyOfRange(data, 1, 9);
            value = ByteBuffer.wrap(bytes)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .getLong();
        } else if((data[0] & 0xFF) == 0xFE) {
            numBytes = 5;
            bytes = Arrays.copyOfRange(data, 1, 5);
            value = ByteBuffer.wrap(bytes)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .getInt();
        } else if((data[0] & 0xFF) == 0xFD) {
            bytes = Arrays.copyOfRange(data, 1, 3);
            value = ByteBuffer.wrap(bytes)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .getShort();
        } else {
            bytes = Arrays.copyOfRange(data, 0, 1);
            value = data[0] & 0xFF;
        }
    }

    public long getValue() {
        return value;
    }

    public int getNumBytes() {
        return bytes.length;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
