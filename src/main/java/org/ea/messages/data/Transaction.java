package org.ea.messages.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Transaction {
    private int version;
    private boolean witnessed;
    private VarInt numInput;
    private List<InputScript> inScript = new ArrayList<>();
    private VarInt numOutput;
    private List<OutputScript> outScript = new ArrayList<>();
    private List<Witness> witnesses = new ArrayList<>();
    private int lockTime;

    public byte[] setData(byte[] data) {
        version = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 4)).getInt();
        int scriptOffset = 4;
        if(data[5] == 0x00 && data[6] == 0x01) {
            witnessed = true;
            scriptOffset = 6;
        }
        numInput = new VarInt(Arrays.copyOfRange(data, scriptOffset, data.length));

        scriptOffset += numInput.getNumBytes();
        data = Arrays.copyOfRange(data, scriptOffset, data.length);
        for(int i=0; i<numInput.getValue(); i++) {
            InputScript is = new InputScript();
            data = is.setData(data);
            inScript.add(is);
        }

        numOutput = new VarInt(Arrays.copyOfRange(data, 0, data.length));
        data = Arrays.copyOfRange(data, numOutput.getNumBytes(), data.length);
        for(int i=0; i<numOutput.getValue(); i++) {
            OutputScript is = new OutputScript();
            data = is.setData(data);
            outScript.add(is);
        }

        if(witnessed) {
            System.out.println("DARN!");
        }

        lockTime = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 4)).getInt();

        return Arrays.copyOfRange(data, 4, data.length);
    }
}
