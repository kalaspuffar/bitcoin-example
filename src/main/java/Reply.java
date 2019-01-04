import java.nio.ByteBuffer;
import java.util.Arrays;

public class Reply {
    private int network;
    private String command;
    public Reply(byte[] data) {
        network = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 4)).getInt();
        
    }

    public String getCommand() {
        return command;
    }
}
