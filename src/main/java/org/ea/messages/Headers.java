package org.ea.messages;

import org.ea.main.Utils;
import org.ea.messages.data.VarLen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Headers extends Reply {
    private long numHeaders;

    private List<byte[]> headers = new ArrayList<>();

    protected Headers(byte[] msg) throws Exception{
        super(msg);
        VarLen count = new VarLen(data);
        numHeaders = count.getValue();

        int currentBlockPlace = count.getNumBytes() + 4;
        for(int i=0; i<numHeaders; i++) {
            headers.add(
                Arrays.copyOfRange(data, currentBlockPlace, currentBlockPlace+81)
            );
            currentBlockPlace += 81;
        }
    }
}
