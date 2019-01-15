package org.ea.messages;

import org.ea.main.Utils;
import org.ea.messages.data.VarInt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetHeaders extends Reply {
    private int version = 70015;
    private long hashCount;
    private List<String> hashes = new ArrayList<>();

    public GetHeaders(int network) {
        super.setCommand("getheaders");
        super.setNetwork(network);
    }

    protected GetHeaders(byte[] msg) throws Exception{
        super(msg);

        version = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 4))
            .order(ByteOrder.LITTLE_ENDIAN)
            .getInt();
        VarInt varInt = new VarInt(Arrays.copyOfRange(data, 4, data.length));
        hashCount = varInt.getValue();
        int currentBlockPlace = varInt.getNumBytes() + 4;
        for(int i=0; i<hashCount; i++) {
            hashes.add(
                Utils.byte2hex(Arrays.copyOfRange(data, currentBlockPlace, currentBlockPlace+32))
            );
            currentBlockPlace += 32;
        }

        System.out.println(hashes.get(0));

        System.out.println("Got a request for " + hashes.size() + " num of hashes");
    }

    public byte[] getByteData() throws Exception {
        byte[] res = new byte[0];
        res = Utils.combine(res, Utils.getIntToBytes(this.version));

        byte[] hash_stop = new byte[32];
        Arrays.fill(hash_stop, (byte)0);
        res = Utils.combine(res, (byte) hashes.size());
        for(String hash : hashes) {
            res = Utils.combine(res, Utils.hex2Byte(hash));
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
