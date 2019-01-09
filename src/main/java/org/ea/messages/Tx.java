package org.ea.messages;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Tx extends Reply {
    protected Tx(byte[] msg) throws Exception{
        super(msg);

        int version = ByteBuffer.wrap(
                Arrays.copyOfRange(data, 0, 4)
        ).order(ByteOrder.LITTLE_ENDIAN).getInt();

        System.out.println("TX " + version);
    }
}
