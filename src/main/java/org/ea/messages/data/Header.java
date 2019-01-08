package org.ea.messages.data;

import org.ea.main.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class Header {

    private int version;
    private byte[] prevBlock;
    private byte[] merkleRoot;
    private int timestamp;
    private int bits;
    private int nonce;
    private VarInt txnCount;

    public Header(byte[] data) {
        this.version = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 4))
                .order(LITTLE_ENDIAN).getInt();
        this.prevBlock = Utils.reverse(Arrays.copyOfRange(data, 4, 36));
        this.merkleRoot = Utils.reverse(Arrays.copyOfRange(data, 36, 68));
        this.timestamp = ByteBuffer.wrap(Arrays.copyOfRange(data, 68, 72))
                .order(LITTLE_ENDIAN).getInt();
        this.bits = ByteBuffer.wrap(Arrays.copyOfRange(data, 72, 76))
                .order(LITTLE_ENDIAN).getInt();
        this.nonce = ByteBuffer.wrap(Arrays.copyOfRange(data, 76, 80))
                .order(LITTLE_ENDIAN).getInt();
        this.txnCount = new VarInt(Arrays.copyOfRange(data, 80, data.length));
    }

    public void print() {
        byte[] data = new byte[0];
        data = Utils.combine(data, Utils.getIntToBytes(this.version));
        data = Utils.combine(data, Utils.reverse(this.prevBlock));
        data = Utils.combine(data, Utils.reverse(this.merkleRoot));
        data = Utils.combine(data, Utils.getIntToBytes(this.timestamp));
        data = Utils.combine(data, Utils.getIntToBytes(this.bits));
        data = Utils.combine(data, Utils.getIntToBytes(this.nonce));

        try {
            data = Utils.dhash(data);
            data = Utils.reverse(data);
            System.out.println(Utils.byte2hex(data));
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] rPrev = Utils.reverse(prevBlock);

        //System.out.println(Utils.byte2hex(rPrev));
    }
}
