package org.ea.messages.data;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class OutputScript {
    private long value;
    private VarInt pk_script_len;
    private byte[] pk_script;

    public byte[] setData(byte[] data) {
        value = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 8)).getLong();
        System.out.println(value);
        pk_script_len = new VarInt(Arrays.copyOfRange(data, 8, 12));
        int scriptOffset = pk_script_len.getNumBytes() + 8;
        pk_script = Arrays.copyOfRange(data, scriptOffset, scriptOffset + (int)pk_script_len.getValue());
        return Arrays.copyOfRange(data, scriptOffset + (int)pk_script_len.getValue(), data.length);
    }
}
