package org.ea.messages;

public class Reject extends Reply {
    private String message;

    protected Reject(byte[] data) throws Exception {
        super(data);
        message = new String(this.data);
    }

    public String getMessage() {
        return message;
    }
}
