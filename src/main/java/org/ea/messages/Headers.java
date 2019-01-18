package org.ea.messages;

import org.ea.messages.data.Header;
import org.ea.messages.data.VarInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Headers extends Reply {
    private long numHeaders;

    private List<Header> headers = new ArrayList<>();

    protected Headers(byte[] msg) throws Exception{
        super(msg);
        System.out.println("HEADERS");

        VarInt count = new VarInt(data);
        numHeaders = count.getValue();

        System.out.println("Got some headers " + numHeaders);

        int currentBlockPlace = count.getNumBytes();
        for(int i=0; i<numHeaders; i++) {
            headers.add(
                new Header(Arrays.copyOfRange(data, currentBlockPlace, currentBlockPlace+81))
            );
            currentBlockPlace += 81;
        }

        //for(Header h : headers) h.print();
    }

    public List<Header> getHeaders() {
        return headers;
    }
}

/*
43 49 7F D7
F8 26 95 71 08 F4 A3 0F D9 CE C3 AE BA 79 97 20 84 E9 0E AD 01 EA 33 09 00 00 00 00 BA C8 B0 FA
92 7C 0A C8 23 42 87 E3 3C 5F 74 D3 8D 35 48 20 E2 47 56 AD 70 9D 70 38 FC 5F 31 F0 20 E7 49 4D
FF FF 00 1D
03 E4 B6 72
00 01 00 00
00
 */