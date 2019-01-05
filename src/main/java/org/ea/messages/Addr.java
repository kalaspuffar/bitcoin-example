package org.ea.messages;

import org.ea.messages.data.NetAddr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Addr extends Reply {
    protected Addr(byte[] msg) throws Exception{
        super(msg);
        int numAddr = data[0];
        List<NetAddr> addresses = new ArrayList<>();
        int current = 1;
        for(int i=0; i<numAddr; i++) {
            addresses.add(new NetAddr(
                Arrays.copyOfRange(data, current, current + 30)
            ));
            current += 30;
        }

        for(NetAddr addr : addresses) {
            System.out.println(addr.toString());
        }
    }
}
