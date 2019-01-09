package org.ea.messages;

import org.ea.main.Utils;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class Reply {
    private int network;
    private String command;
    private int length;
    private byte[] hash;
    protected byte[] data;

    public static Reply build(byte[] data) throws Exception {
        String command = new String(Arrays.copyOfRange(data, 4, 16)).trim();

        switch (command) {
            case "addr":
                return new Addr(data);
            case "block":
                return new Block(data);
            case "feefilter":
                return new FeeFilter(data);
            case "headers":
                return new Headers(data);
            case "getheaders":
                return new GetHeaders(data);
            case "inv":
                return new Inv(data);
            case "version":
                return new Version(data);
            case "verack":
                return new Verack(data);
            case "ping":
                return new Ping(data);
            case "sendheaders":
                return new SendHeaders(data);
            case "sendcmpct":
                return new SendCmpCt(data);
            case "tx":
                return new Tx(data);
            case "reject":
                return new Reject(data);
            default:
                System.out.println("Unknown: " + command);
                return new Reply(data);
        }
    }

    public Reply() {
        this.network = 1;
    }

    protected Reply(byte[] msg) throws Exception {
        this.network = ByteBuffer.wrap(Arrays.copyOfRange(msg, 0, 4)).getInt();
        this.command = new String(Arrays.copyOfRange(msg, 4, 16));
        this.length = ByteBuffer.wrap(Arrays.copyOfRange(msg, 16, 20)).order(LITTLE_ENDIAN).getInt();
        this.hash = Arrays.copyOfRange(msg, 20, 24);
        this.data = Arrays.copyOfRange(msg, 24, 24 + this.length);

        byte[] checkHash = Utils.hashValue(this.data);
        if(Arrays.compare(checkHash, hash) != 0) {
            throw new Exception("Hashes don't match.");
        }
    }

    public void setCommand(String cmd) {
        this.command = cmd;
    }

    public void setNetwork(int network) {
        this.network = network;
    }

    protected byte[] getByteData(byte[] res) throws Exception {
        byte[] checksum = Utils.hashValue(res);
        byte[] header = new byte[0];
        header = Utils.combine(header, Utils.getIntToBytes(network));
        header = Utils.combine(header, Utils.getCommand(this.command));
        header = Utils.combine(header, Utils.getIntToBytes(res.length));
        header = Utils.combine(header, checksum);

        return header;
    }
}
