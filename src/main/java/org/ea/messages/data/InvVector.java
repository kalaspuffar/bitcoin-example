package org.ea.messages.data;

import org.ea.main.Utils;
import org.ea.messages.Inv;
import org.json.simple.JSONObject;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;

public class InvVector implements Comparable<InvVector> {

    /*
4	type	uint32_t	Identifies the object type linked to this inventory
32	hash	char[32]	Hash of the object
     */

    private int id;
    private byte[] hash;
    private byte[] revHash;

    public InvVector(JSONObject jsonObject) {
        setId(((Long)jsonObject.get("id")).intValue());
        setHash((String)jsonObject.get("hash"));
    }

    public InvVector(byte[] data) {
        this.id = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 4))
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
        this.hash = Arrays.copyOfRange(data, 4, 36);
        this.revHash = Utils.reverse(this.hash);
    }

    public InvVector(int i, String s) {
        setId(i);
        setHash(s);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setHash(String hash) {
        this.hash = Utils.hex2Byte(hash);
        this.revHash = Utils.reverse(this.hash);
    }

    public void print() {
        System.out.println(id + " - " + Utils.byte2hex(revHash));
    }

    public byte[] getByteData() {
        byte[] res = new byte[0];
        res = Utils.combine(res, Utils.getIntToBytes(id));
        res = Utils.combine(res, hash);
        return res;
    }

    public int getId() {
        return id;
    }

    public String getHash() {
        return Utils.byte2hex(hash);
    }

    public JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("hash", getHash());
        obj.put("id", getId());
        return obj;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvVector invVector = (InvVector) o;
        return getId() == invVector.getId() &&
                getHash().equalsIgnoreCase(invVector.getHash());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getId());
        result = 31 * result + getHash().hashCode();
        return result;
    }

    @Override
    public int compareTo(InvVector o) {
        for(int i = 0; i < 32; i++) {
            if((this.revHash[i] & 0xFF) > (o.revHash[i] & 0xFF)) {
                return 1;
            } else if((this.revHash[i] & 0xFF) < (o.revHash[i] & 0xFF)) {
                return -1;
            }
        }
        return 0;
/*
        System.out.println(Utils.byte2hex(o.hash));

        BigInteger other = new BigInteger(1,
            Utils.reverse(o.hash)
        );
        BigInteger curr = new BigInteger(1,
            Utils.reverse(this.hash)
        );

        if(curr.compareTo(other) > 0) {
            System.err.println(curr.toString() + " > " + other.toString());
        } else if(curr.compareTo(other) < 0) {
            System.err.println(curr.toString() + " < " + other.toString());
        } else {
            System.err.println(curr.toString() + " == " + other.toString());
        }

        return curr.compareTo(other);
*/
    }
}