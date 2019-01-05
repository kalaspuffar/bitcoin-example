package org.ea.messages.data;

import org.ea.main.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class NetAddr {
    private int time;
    private long network_service;
    private byte[] ipv6;
    private byte[] ipv4;
    private short port;

    public NetAddr() {
        ipv6 = new byte[12];
        ipv4 = new byte[4];
        Arrays.fill(ipv6, (byte)0);
        ipv6[10] = (byte)0xFF;
        ipv6[11] = (byte)0xFF;
        Arrays.fill(ipv4, (byte)0);
        this.network_service = 1;
    }

    public NetAddr(byte[] data) {
        if(data.length < 27) {
            this.network_service = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 8))
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .getLong();
            this.ipv6 = Arrays.copyOfRange(data, 8, 20);
            this.ipv4 = Arrays.copyOfRange(data, 20, 24);
            this.port = ByteBuffer.wrap(Arrays.copyOfRange(data, 24, 26))
                    .getShort();
        } else {
            this.time = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 4))
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .getInt();
            this.network_service = ByteBuffer.wrap(Arrays.copyOfRange(data, 4, 12))
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .getLong();
            this.ipv6 = Arrays.copyOfRange(data, 12, 24);
            this.ipv4 = Arrays.copyOfRange(data, 24, 28);
            this.port = ByteBuffer.wrap(Arrays.copyOfRange(data, 28, 30))
                    .getShort();
        }
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public long getNetworkService() {
        return network_service;
    }

    public void setNetworkService(long network_service) {
        this.network_service = network_service;
    }

    public byte[] getIpv6() {
        return ipv6;
    }

    public void setIpv6(byte[] ipv6) {
        this.ipv6 = ipv6;
    }

    public byte[] getIpv4() {
        return ipv4;
    }

    public void setIpv4(byte[] ipv4) {
        this.ipv4 = ipv4;
    }

    public short getPort() {
        return port;
    }

    public void setPort(short port) {
        this.port = port;
    }

    public byte[] getNetworkAddress() {
        byte[] res = new byte[0];
        res = Utils.combine(res, Utils.getLongToBytes(this.network_service));
        res = Utils.combine(res, this.ipv6);
        res = Utils.combine(res, this.ipv4);
        res = Utils.combine(res, Utils.reverse(Utils.getShortToBytes(port)));
        return res;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.ipv4[0] & 0xFF);
        sb.append(".");
        sb.append(this.ipv4[1] & 0xFF);
        sb.append(".");
        sb.append(this.ipv4[2] & 0xFF);
        sb.append(".");
        sb.append(this.ipv4[3] & 0xFF);
        sb.append(":");
        sb.append(this.port);
        return sb.toString();
    }
}
