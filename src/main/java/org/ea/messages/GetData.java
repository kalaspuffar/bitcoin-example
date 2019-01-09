package org.ea.messages;

import org.ea.main.Utils;
import org.ea.messages.data.InvVector;
import org.ea.messages.data.VarInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetData extends Reply {
    private long hashCount;
    private List<InvVector> inventoryVectors = new ArrayList<>();

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
            inventoryVectors.add(
                new InvVector(
                        Arrays.copyOfRange(data, currentBlockPlace, currentBlockPlace+36)
                )
            );
            currentBlockPlace += 36;
        }
    }

    public byte[] getByteData() throws Exception {
        byte[] res = new byte[0];
        res = Utils.combine(res, (byte) inventoryVectors.size());
        for(InvVector invVector : inventoryVectors) {
            res = Utils.combine(res, invVector.getByteData());
        }

        byte[] header = super.getByteData(res);

        return Utils.combine(header, res);
    }

    public void addVector(InvVector hash) {
        inventoryVectors.add(hash);
    }
}
