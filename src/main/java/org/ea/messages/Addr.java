package org.ea.messages;

import org.ea.messages.data.NetAddr;
import org.ea.messages.data.VarInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Addr extends Reply {
    protected Addr(byte[] msg) throws Exception{
        super(msg);
        VarInt numAddr = new VarInt(data);
        List<NetAddr> addresses = new ArrayList<>();
        int current = numAddr.getNumBytes();
        for(int i=0; i<numAddr.getValue(); i++) {
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
