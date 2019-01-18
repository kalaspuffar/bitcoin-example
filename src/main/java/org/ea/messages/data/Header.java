package org.ea.messages.data;

import org.ea.main.Utils;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class Header implements Comparable<Header> {

    private byte[] id;
    private long height = -1;
    private int version;
    private byte[] prevBlock;
    private byte[] merkleRoot;
    private int timestamp;
    private int bits;
    private int nonce;
    private VarInt txnCount;

    public Header(JSONObject jsonObject) {
        setId((String)jsonObject.get("id"));
        setHeight(((Long)jsonObject.get("height")).intValue());
        setVersion(((Long)jsonObject.get("version")).intValue());
        setPrevBlock((String)jsonObject.get("prevBlock"));
        setMerkleRoot((String)jsonObject.get("merkleRoot"));
        setTimestamp(((Long)jsonObject.get("timestamp")).intValue());
        setBits(((Long)jsonObject.get("bits")).intValue());
        setNonce(((Long)jsonObject.get("nonce")).intValue());
        setTxnCount(((Long)jsonObject.get("txnCount")));
    }

    public Header(byte[] data) {
        this.version = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 4))
                .order(LITTLE_ENDIAN).getInt();
        this.prevBlock = Arrays.copyOfRange(data, 4, 36);
        this.merkleRoot = Arrays.copyOfRange(data, 36, 68);
        this.timestamp = ByteBuffer.wrap(Arrays.copyOfRange(data, 68, 72))
                .order(LITTLE_ENDIAN).getInt();
        this.bits = ByteBuffer.wrap(Arrays.copyOfRange(data, 72, 76))
                .order(LITTLE_ENDIAN).getInt();
        this.nonce = ByteBuffer.wrap(Arrays.copyOfRange(data, 76, 80))
                .order(LITTLE_ENDIAN).getInt();
        this.txnCount = new VarInt(Arrays.copyOfRange(data, 80, data.length));

        byte[] hashData = new byte[0];
        hashData = Utils.combine(hashData, Utils.getIntToBytes(this.version));
        hashData = Utils.combine(hashData, this.prevBlock);
        hashData = Utils.combine(hashData, this.merkleRoot);
        hashData = Utils.combine(hashData, Utils.getIntToBytes(this.timestamp));
        hashData = Utils.combine(hashData, Utils.getIntToBytes(this.bits));
        hashData = Utils.combine(hashData, Utils.getIntToBytes(this.nonce));
        try {
            id = Utils.dhash(hashData);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void print() {
        System.out.println(this.version + " - " + Utils.byte2hex(id) + " - " + Utils.byte2hex(prevBlock));
    }

    public String getId() {
        return Utils.byte2hex(id);
    }

    public void setId(String prevBlock) {
        this.id = Utils.hex2Byte(prevBlock);
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getPrevBlock() {
        return Utils.byte2hex(prevBlock);
    }

    public void setPrevBlock(String prevBlock) {
        this.prevBlock = Utils.hex2Byte(prevBlock);
    }

    public String getMerkleRoot() {
        return Utils.byte2hex(merkleRoot);
    }

    public void setMerkleRoot(String merkleRoot) {
        this.merkleRoot = Utils.hex2Byte(merkleRoot);
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getBits() {
        return bits;
    }

    public void setBits(int bits) {
        this.bits = bits;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public long getTxnCount() {
        return txnCount.getValue();
    }

    public void setTxnCount(long count) {
        this.txnCount = new VarInt(count);
    }

    public JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("id", getId());
        obj.put("height", getHeight());
        obj.put("version", getVersion());
        obj.put("prevBlock", getPrevBlock());
        obj.put("merkleRoot", getMerkleRoot());
        obj.put("timestamp", getTimestamp());
        obj.put("bits", getBits());
        obj.put("nonce", getNonce());
        obj.put("txnCount", getTxnCount());
        return obj;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Header invVector = (Header) o;
        return getId().equalsIgnoreCase(invVector.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public int compareTo(Header o) {
        return Long.compare(this.height, o.height);
    }
}
