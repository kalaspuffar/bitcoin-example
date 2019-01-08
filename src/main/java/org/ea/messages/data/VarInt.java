package org.ea.messages.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class VarInt {
    private long value;
    private int numBytes;

    public VarInt(byte[] data) {
        if((data[0] & 0xFF) == 0xFF) {
            numBytes = 9;
            value = ByteBuffer.wrap(
                    Arrays.copyOfRange(data, 1, 9)
            ).order(ByteOrder.LITTLE_ENDIAN)
                    .getLong();
        } else if((data[0] & 0xFF) == 0xFE) {
            numBytes = 5;
            value = ByteBuffer.wrap(
                    Arrays.copyOfRange(data, 1, 5)
            ).order(ByteOrder.LITTLE_ENDIAN)
                    .getInt();
        } else if((data[0] & 0xFF) == 0xFD) {
            numBytes = 3;
            value = ByteBuffer.wrap(
                    Arrays.copyOfRange(data, 1, 3)
            ).order(ByteOrder.LITTLE_ENDIAN)
                    .getShort();
        } else {
            numBytes = 1;
            value = data[0] & 0xFF;
        }
    }

    public long getValue() {
        return value;
    }

    public int getNumBytes() {
        return numBytes;
    }
}
