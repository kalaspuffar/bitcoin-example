package org.ea.messages.data;

import org.ea.main.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class InputScript {
    private byte[] previousOutput;
    private int previousIndex;
    private VarInt script_len;
    private Script script;
    private int sequence;

    public byte[] setData(byte[] data) {
        previousOutput = Arrays.copyOfRange(data, 0, 32);
        previousIndex = ByteBuffer
                .wrap(Arrays.copyOfRange(data, 32, 36))
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();

        script_len = new VarInt(Arrays.copyOfRange(data, 36, data.length));
        int scriptOffset = 36 + script_len.getNumBytes();
        int scriptEnd = (int)(scriptOffset + script_len.getValue());

        script = new Script(Arrays.copyOfRange(data, scriptOffset, scriptEnd));
        sequence = ByteBuffer
                    .wrap(Arrays.copyOfRange(data, scriptEnd, scriptEnd+4))
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .getInt();

        return Arrays.copyOfRange(data, scriptEnd+4, data.length);
    }

    public byte[] getData() {
        byte[] data = new byte[0];
        data = Utils.combine(data, previousOutput);
        data = Utils.combine(data, Utils.getIntToBytes(previousIndex));
        data = Utils.combine(data, script_len.getBytes());
        data = Utils.combine(data, script.getData());
        data = Utils.combine(data, Utils.getIntToBytes(sequence));
        return data;
    }
}
