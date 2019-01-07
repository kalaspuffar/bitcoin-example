package org.ea.messages;

import org.ea.main.Utils;
import org.ea.messages.data.VarInt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetData extends Reply {
    private long hashCount;
    private List<String> hashes = new ArrayList<>();

    public GetData(int network) {
        super.setCommand("getdata");
        super.setNetwork(network);
    }

    protected GetData(byte[] msg) throws Exception{
        super(msg);

        VarInt varInt = new VarInt(Arrays.copyOfRange(data, 4, data.length));
        hashCount = varInt.getValue();
        int currentBlockPlace = varInt.getNumBytes();
        for(int i=0; i<hashCount; i++) {
            hashes.add(
                Utils.byte2hex(Arrays.copyOfRange(data, currentBlockPlace, currentBlockPlace+32))
            );
            currentBlockPlace += 32;
        }
    }

    public byte[] getByteData() throws Exception {
        byte[] res = new byte[0];
        byte[] hash_stop = new byte[32];
        Arrays.fill(hash_stop, (byte)0);
        if(hashes.size() == 0) {
            res = Utils.combine(res, (byte) 1);
            res = Utils.combine(res, hash_stop);
        } else {
            res = Utils.combine(res, (byte) hashes.size());
            for(String hash : hashes) {
                res = Utils.combine(res, Utils.hex2Byte(hash));
            }
        }

        res = Utils.combine(res, hash_stop);
        Utils.printArray("getHeadersMsg", res);

        byte[] header = super.getByteData(res);

        return Utils.combine(header, res);
    }

    public void addHash(String hash) {
        hashes.add(hash);
    }
}
