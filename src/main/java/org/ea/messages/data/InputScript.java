package org.ea.messages.data;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class InputScript {
    private byte[] previousOutput;
    private int previousIndex;
    private VarInt script_len;
    private Script script;
    private int sequence;

    public byte[] setData(byte[] data) {
        previousOutput = Arrays.copyOfRange(data, 0, 32);
        previousIndex = ByteBuffer.wrap(Arrays.copyOfRange(data, 32, 36)).getInt();

        script_len = new VarInt(Arrays.copyOfRange(data, 36, 46));
        int scriptOffset = 36 + script_len.getNumBytes();
        int scriptEnd = (int)(scriptOffset + script_len.getValue());

        script = new Script(Arrays.copyOfRange(data, scriptOffset, scriptEnd));
        sequence = ByteBuffer.wrap(Arrays.copyOfRange(data, scriptEnd, scriptEnd+4)).getInt();

        return Arrays.copyOfRange(data, scriptEnd+4, data.length);
    }
}
