package org.ea.messages;

public class Ping extends Reply {
    private byte[] nonce;

    protected Ping(byte[] data) throws Exception{
        super(data);
        nonce = this.data;
        System.out.println("PING");
    }

    public byte[] getPongData() throws Exception {
        super.setCommand("pong");
        return super.getByteData(nonce);
    }
}
