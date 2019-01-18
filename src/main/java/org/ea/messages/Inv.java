package org.ea.messages;

import org.ea.messages.data.InvVector;
import org.ea.messages.data.VarInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Inv extends Reply {
    private VarInt numInventory;
    private List<InvVector> inventoryVectors = new ArrayList<>();

    /*
1+	count	var_int	Number of inventory entries
36x?	inventory	inv_vect[]	Inventory vectors
     */

    protected Inv(byte[] msg) throws Exception{
        super(msg);

        numInventory = new VarInt(data);

        int currentIndex = numInventory.getNumBytes();
        for(int i=0; i<numInventory.getValue(); i++) {
            inventoryVectors.add(new InvVector(
                    Arrays.copyOfRange(data, currentIndex, currentIndex+36)
            ));
            currentIndex += 36;
        }
/*
        for(InvVector iv : inventoryVectors) {
            iv.print();
        }
*/
    }

    public List<InvVector> getInvVectors() {
        return inventoryVectors;
    }
}
