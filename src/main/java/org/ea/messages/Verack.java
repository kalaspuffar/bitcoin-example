package org.ea.messages;

public class Verack extends Reply {
    public Verack() {
        super.setCommand("verack");
    }

    protected Verack(byte[] data) throws Exception{
        super(data);
        System.out.println("VERSION ACKNOWLEDGE");
    }

    public byte[] getByteData() throws Exception {
        return super.getByteData(new byte[0]);
    }
}
