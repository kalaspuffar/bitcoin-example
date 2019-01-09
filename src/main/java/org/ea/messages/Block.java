package org.ea.messages;

import org.ea.main.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Block extends Reply {
    protected Block(byte[] msg) throws Exception{
        super(msg);

        int version = ByteBuffer.wrap(
                Arrays.copyOfRange(data, 0, 4)
        ).order(ByteOrder.LITTLE_ENDIAN).getInt();

        System.out.println("BLOCK " + version);
        Utils.printArray("data", data);
    }
}
