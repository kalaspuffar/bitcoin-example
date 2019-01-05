package org.ea.messages;

import org.ea.messages.Reply;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FeeFilter extends Reply {
    private long feeFilterVal;
    protected FeeFilter(byte[] msg) throws Exception{
        super(msg);
        feeFilterVal = ByteBuffer.wrap(data)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getLong();

        System.out.println("Current FeeFilter: " + feeFilterVal);
    }
}
