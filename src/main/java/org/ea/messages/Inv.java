package org.ea.messages;

public class Inv extends Reply {
    private byte[] nonce;

    protected Inv(byte[] data) throws Exception{
        super(data);
        nonce = this.data;
    }

    public byte[] getNonce() {
        return nonce;
    }
}
