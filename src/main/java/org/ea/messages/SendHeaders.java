package org.ea.messages;

public class SendHeaders extends Reply {
    public SendHeaders(int network) {
        super.setCommand("sendheaders");
        super.setNetwork(network);
    }

    protected SendHeaders(byte[] msg) throws Exception{
        super(msg);

        System.out.println("SendHeaders");
    }

    public byte[] getByteData() throws Exception {
        return super.getByteData(new byte[0]);
    }
}
