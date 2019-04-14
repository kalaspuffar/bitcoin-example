package org.ea.messages.data;

import jdk.jshell.execution.Util;
import org.ea.main.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

    private byte[] dataIn;

    public byte[] setData(byte[] data) throws IllegalArgumentException {
        dataIn = data;
        version = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 4))
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .getInt();
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

//        System.out.println("In: "+ inScript.size() + " Out: " + outScript.size());

        lockTime = ByteBuffer
                .wrap(Arrays.copyOfRange(data, 0, 4))
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();

        return Arrays.copyOfRange(data, 4, data.length);
    }

    public byte[] getDataIn() {
        return dataIn;
    }

    public byte[] getData() {
        byte[] data = new byte[0];
        data = Utils.combine(data, Utils.getIntToBytes(version, false));
        if(witnessed) {
            data = Utils.combine(data, new byte[] {0x00, 0x01});
        }
        data = Utils.combine(data, numInput.getBytes());
        for(InputScript is : inScript) {
            data = Utils.combine(data, is.getData());
        }

        data = Utils.combine(data, numOutput.getBytes());
        for(OutputScript os : outScript) {
            data = Utils.combine(data, os.getData());
        }

        if(witnessed) {
            System.out.println("DARN");
        }
        data = Utils.combine(data, Utils.getIntToBytes(lockTime));
        return data;
    }
}
