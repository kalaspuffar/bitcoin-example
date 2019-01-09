package org.ea.messages;

import org.ea.main.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class SendCmpCt extends Reply {
    private boolean fAnnounceUsingCMPCTBLOCK = false;
    private long nCMPCTBLOCKVersion = 0;

    protected SendCmpCt(byte[] msg) throws Exception{
        super(msg);

        fAnnounceUsingCMPCTBLOCK = (data[0] & 0xFF) == 1;
        nCMPCTBLOCKVersion = ByteBuffer.wrap(
                Arrays.copyOfRange(data, 1, 9)
        ).order(ByteOrder.LITTLE_ENDIAN)
                .getLong();

        System.out.println("SendCmpCt");
    }
}
