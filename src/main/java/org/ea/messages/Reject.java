package org.ea.messages;

import org.ea.main.Utils;

import java.util.Arrays;

public class Reject extends Reply {
    private String message;
    private byte ccode;
    private String reason;
    private byte[] extraData;

    /*
1+	message	var_str	type of message rejected
1	ccode	char	code relating to rejected message
1+	reason	var_str	text version of reason for rejection
0+	data	char	Optional extra data provided by some errors. Currently, all errors which provide this field fill it with the TXID or block header hash of the object being rejected, so the field is 32 bytes.
     */

    protected Reject(byte[] msg) throws Exception {
        super(msg);

        int msgLen = Utils.indexOf(data, (byte)0x00);

        if(msgLen == -1) {
            message = new String(data);
            return;
        } else {
            message = new String(Arrays.copyOfRange(data, 0, msgLen));
        }

        ccode = this.data[msgLen];
        int reasonLen = Utils.indexOf(Arrays.copyOfRange(data, msgLen + 1, data.length), (byte)0x00);
        reason = new String(Arrays.copyOfRange(data, msgLen + 1, msgLen + 1 + reasonLen));
        extraData = Arrays.copyOfRange(data, msgLen + 1 + reasonLen, data.length);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ccode);
        sb.append(" - ");
        sb.append(message);
        sb.append("\n");
        sb.append(reason);
        sb.append("\n");
        if(extraData != null) {
            Utils.printArray("extraData", extraData);
        }

        return sb.toString();
    }
}
