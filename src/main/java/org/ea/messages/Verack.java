package org.ea.messages;

public class Verack extends Reply {
    public Verack(int network) {
        super.setCommand("verack");
        super.setNetwork(network);
    }

    protected Verack(byte[] data) throws Exception{
        super(data);
        System.out.println("VERSION ACKNOWLEDGE");
    }

    public byte[] getByteData() throws Exception {
        return super.getByteData(new byte[0]);
    }
}
