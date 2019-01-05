package org.ea.messages;

import org.ea.main.Utils;
import org.ea.messages.data.VarLen;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetHeaders extends Reply {
    private int version;
    private long hashCount;
    private List<String> hashes = new ArrayList<>();

    protected GetHeaders(byte[] msg) throws Exception{
        super(msg);
        version = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 4))
            .order(ByteOrder.LITTLE_ENDIAN)
            .getInt();
        VarLen varLen = new VarLen(Arrays.copyOfRange(data, 4, data.length));
        hashCount = varLen.getValue();
        int currentBlockPlace = varLen.getNumBytes() + 4;
        for(int i=0; i<hashCount; i++) {
            hashes.add(
                Utils.byte2hex(Arrays.copyOfRange(data, currentBlockPlace, currentBlockPlace+32))
            );
            currentBlockPlace += 32;
        }

        System.out.println("Got a request for " + hashes.size() + " num of hashes");
    }
}
