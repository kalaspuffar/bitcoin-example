package org.ea.messages;

import org.ea.main.Utils;
import org.ea.messages.data.NetAddr;
import org.ea.messages.data.VarInt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Version extends Reply {
    private int version;
    private long network_service;
    private long timestamp;

    private NetAddr recipient;
    private NetAddr sender;

    private byte[] nodeId;
    private String subversion;

    private int lastBlock;

    public Version() {
        super.setCommand("version");
        this.network_service = 1;
        this.recipient = new NetAddr();
        this.sender = new NetAddr();
        this.subversion = "";
    }

    /*
    7F 11 01 00
    0D 04 00 00 00 00 00 00
    CE DA 2F 5C 00 00 00 00
    00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF 57 F1 7F 96 C2 84
    0D 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
    A2 21 B4 67 48 74 A4 A5
    10 2F 53 61 74 6F 73 68 69 3A 30 2E 31 37 2E 30 2F
    BD 23 16 00
    01
     */

    public Version(byte[] msg) throws Exception{
        super(msg);

        this.version = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 4))
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
        this.network_service = ByteBuffer.wrap(Arrays
                    .copyOfRange(data, 4, 12)
                )
                .order(ByteOrder.LITTLE_ENDIAN)
                .getLong();
        this.timestamp = ByteBuffer.wrap(
                Arrays
                    .copyOfRange(data, 12, 20)
                )
                .order(ByteOrder.LITTLE_ENDIAN)
                .getLong();

        this.recipient = new NetAddr(
                Arrays.copyOfRange(data, 20, 46)
        );
        this.sender = new NetAddr(
                Arrays.copyOfRange(data, 46, 72)
        );
        this.nodeId = Arrays.copyOfRange(data, 72, 80);

        VarInt len = new VarInt(Arrays.copyOfRange(data, 80, data.length));
        subversion = new String(
                Arrays.copyOfRange(data, 80 + len.getNumBytes(), 80 + len.getNumBytes() + (int)len.getValue())
        );

        int endOfSubVersion = 80 + len.getNumBytes() + (int)len.getValue();

        lastBlock = ByteBuffer.wrap(
                Arrays.copyOfRange(data, endOfSubVersion, endOfSubVersion + 4)
        ).order(ByteOrder.LITTLE_ENDIAN)
        .getInt();

        System.out.println("VERSION " + version + " - " + subversion + " last " + lastBlock);
    }

    public byte[] getByteData() throws Exception {
        byte[] res = new byte[0];
        res = Utils.combine(res, Utils.getIntToBytes(this.version));
        res = Utils.combine(res, Utils.getLongToBytes(this.network_service));
        res = Utils.combine(res, Utils.getLongToBytes(this.timestamp));
        res = Utils.combine(res, recipient.getNetworkAddress());
        res = Utils.combine(res, sender.getNetworkAddress());
        res = Utils.combine(res, this.nodeId);
        res = Utils.combine(res, (byte)subversion.length());
        if(subversion.length() > 0) {
            res = Utils.combine(res, Arrays.copyOfRange(subversion.getBytes(), 0, subversion.length()));
        }
        res = Utils.combine(res, Utils.getIntToBytes(this.lastBlock));

        byte[] header = super.getByteData(res);

        return Utils.combine(header, res);
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getNetworkService() {
        return network_service;
    }

    public void setNetworkService(long network_service) {
        this.network_service = network_service;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public NetAddr getRecipient() {
        return recipient;
    }

    public void setRecipient(NetAddr recipient) {
        this.recipient = recipient;
    }

    public NetAddr getSender() {
        return sender;
    }

    public void setSender(NetAddr sender) {
        this.sender = sender;
    }

    public byte[] getNodeId() {
        return nodeId;
    }

    public void setNodeId(byte[] nodeId) {
        this.nodeId = nodeId;
    }

    public String getSubversion() {
        return subversion;
    }

    public void setSubversion(String subversion) {
        this.subversion = subversion;
    }

    public int getLastBlock() {
        return lastBlock;
    }

    public void setLastBlock(int lastBlock) {
        this.lastBlock = lastBlock;
    }
}
