import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;

public class Reply {
    private int network;
    private String command;
    private int length;
    private byte[] hash;
    private byte[] data;

    public Reply(byte[] data) {
        this.network = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 4)).getInt();
        this.command = new String(Arrays.copyOfRange(data, 4, 16));
        this.length = ByteBuffer.wrap(Arrays.copyOfRange(data, 16, 20)).getInt();
        this.hash = Arrays.copyOfRange(data, 20, 24);
        this.data = Arrays.copyOfRange(data, 24, 24 + this.length);

        System.out.println(new String(this.data));

        try {
            byte[] checkHash = firstBytes(sha256(sha256(data)), 4);
            System.out.println(Arrays.toString(checkHash));
            System.out.println(Arrays.toString(hash));
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(command);
    }

    public int getLength() {
        return length;
    }

    public byte[] getHash() {
        return hash;
    }

    public int getNetwork() {
        return network;
    }

    public String getCommand() {
        return command;
    }

    private byte[] firstBytes(byte[] bytes, int len) {
        byte[] res = new byte[len];
        for(int i=0; i<len; i++) res[i] = bytes[i];
        return res;
    }

    private byte[] sha256(byte[] message) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(message);
    }
}
