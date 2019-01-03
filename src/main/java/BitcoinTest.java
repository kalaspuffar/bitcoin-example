import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.Random;

public class BitcoinTest {
    private static Socket clientSocket;
    private static PrintWriter out;
    private static BufferedReader in;

    public static byte[] combine(byte[] a, byte b) {
        return combine(a, new byte[] {b});
    }

    public static byte[] combine(byte[] a, byte[] b) {
        byte[] keys = new byte[a.length + b.length];
        System.arraycopy(a, 0, keys, 0, a.length);
        System.arraycopy(b, 0, keys, a.length, b.length);
        return keys;
    }

    private static byte[] reverse(byte[] data) {
        for(int i = 0; i < data.length / 2; i++) {
            byte temp = data[i];
            data[i] = data[data.length - i - 1];
            data[data.length - i - 1] = temp;
        }
        return data;
    }

    private static byte[] getShortToBytes(short input) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(input).array();
    }


    private static byte[] getIntToBytes(int input) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(input).array();
    }

    private static byte[] getLongToBytes(long input) {
        return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(input).array();
    }

    private static byte[] getCommand(String cmd) {
        byte[] res = new byte[12];
        Arrays.fill(res, (byte)0);
        System.arraycopy(cmd.getBytes(), 0, res, 0, cmd.length());
        return res;
    }

    private static byte[] getNetworkAddress(byte[] addr, short port) {
        byte[] res = new byte[0];
        res = combine(res, getLongToBytes(1));
        byte[] ipv6 = new byte[12];
        Arrays.fill(ipv6, (byte)0);
        ipv6[10] = (byte)0xFF;
        ipv6[11] = (byte)0xFF;
        res = combine(res, ipv6);
        res = combine(res, addr);
        res = combine(res, reverse(getShortToBytes(port)));
        return res;
    }

    /*
fa bf b5 da
76 65 72 73 69 6f 6e 0 0 0 0 0
55 0 0 0
ca 5e 0 2

d1 0 0 0
1 0 0 0 0 0 0 0
a3 f 2e 5c 0 0 0 0
1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 ff ff 88 f3 8b 60 47 9d
1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 ff ff c0 a8 6  2  47 9d
2c d6 f5 7f 56 90 46 e2
0
0 0 0 0

FA BF B5 DA
76 65 72 73 69 6F 6E 00 00 00 00 00
55 00 00 00
8C AB 53 6D

D1 00 00 00
01 00 00 00 00 00 00 00
F9 16 2E 5C 00 00 00 00
01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF 00 00 00 00 00 00
01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF 00 00 00 00 00 00
2C 07 7A DE A8 2C E4 2D
00
00 00 00 00
*/

    public static void main(String[] args) {
        try {
            InetAddress dnsresult[] = InetAddress.getAllByName("seed.tbtc.petertodd.org");
            String ip = null;
            byte[] ipAddr = null;
            for (int i = 0; i < dnsresult.length; i++) {
                if(dnsresult[i].getHostAddress().startsWith("1")) {
                    ip = dnsresult[i].getHostAddress();

                    ipAddr = dnsresult[i].getAddress();
                }
            }

            long randomId = new Random().nextLong();

            short default_port = 18333;
            InetAddress localHost = InetAddress.getLocalHost();

            clientSocket = new Socket(ip, default_port);
            OutputStream out = clientSocket.getOutputStream();
            InputStream is = clientSocket.getInputStream();

            byte[] message = new byte[0];
            // Protocol version
            message = combine(message, getIntToBytes(209));
            // Network services
            message = combine(message, getLongToBytes(1));
            // Timestamp
            message = combine(message, getLongToBytes(Instant.now().getEpochSecond()));

            // To addr
//            message = combine(message, getNetworkAddress(ipAddr, default_port));
            // From addr
//            message = combine(message, getNetworkAddress(localHost.getAddress(), default_port));

            message = combine(message, getNetworkAddress(new byte[4], (short)0));
            message = combine(message, getNetworkAddress(new byte[4], (short)0));


            // Random id
            message = combine(message, getLongToBytes(randomId));
            // User agent
            message = combine(message, (byte)0);
            // Last block
            message = combine(message, getIntToBytes(0));

            byte[] checksum = firstBytes(sha256(sha256(message)), 4);

            byte[] header = new byte[0];
            // Network
            header = combine(header, getIntToBytes(0xDAB5BFFA));

            // Command
            header = combine(header, getCommand("version"));

            // Length
            header = combine(header, getIntToBytes(message.length));
            // Checksum
            header = combine(header, checksum);

            byte[] sendMessage = combine(header, message);

            printArray(sendMessage);

            out.write(sendMessage);
            out.flush();

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            System.out.println(Arrays.toString(buffer.toByteArray()));

            is.close();
            out.close();
            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printArray(byte[] sendMessage) {
        for(int i=1; i<sendMessage.length + 1; i++) {
            System.out.print(getByteStr(sendMessage[i-1]) + " ");
            if(i % 20 == 0) {
                System.out.println();
            }
        }
    }

    private static String getByteStr(byte b) {
        String s = Integer.toHexString(b & 0XFF);
        s = s.toUpperCase();
        if(s.length() == 1) {
            s = "0" + s;
        }
        return s;
    }

    private static byte[] firstBytes(byte[] bytes, int len) {
        byte[] res = new byte[len];
        for(int i=0; i<len; i++) res[i] = bytes[i];
        return res;
    }

    private static byte[] sha256(byte[] message) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(message);
    }
}
