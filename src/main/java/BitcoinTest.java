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
0B 11 09 07
72 65 6A 65 63 74 00 00 00 00 00 00
2A 00 00 00
B7 EE 71 4A

07 76 65 72 73 69 6F 6E 11 20 56 65 72 73 69 6F 6E 20 6D 75
73 74 20 62 65 20 33 31 38 30 30 20 6F 72 20 67
72 65 61 74 65 72 0B 11 09 07 70 69 6E 67 00 00 00 00 00 00
00 00 00 00 00 00 5D F6 E0 E2 0B 11 09 07 67 65 74 68 65 61
64 65 72 73 00 00 25 04 00 00 42 20 61 61 D1 00 00 00 20 74
DE 8A 81 D5 C7 F3 B2 A5 FE 33 A9 30 AA 37 45 5B 43 EB B3 43
8F 2C C3 A0 00 00 00 00 00 00 00 D3 B1 D5 21 2D 8A 76 EB 61
29 7D DE 84 E7 D7 EF B0 4F 7F 80 B8 CC D0 52 D4 98 87 B1 00
00 00 00 94 AF F7 2A D0 EC 48 F9 F2 12 AF 51 C5 5F 0F AE 73
38 C5 1C EE 77 F8 31 45 00 00 00 00 00 00 00 51 2B A3 5D 7C
74 1F D2 7A AD 66 A6 C7 6A 2B 13 23 B3 30 CE 81 D9 16 28 D5
00 00 00 00 00 00 00 DB 42 A6 65 C7 BD F3 AA B3 B6 A2 80 EE
42 6F FF 3E 48 C5 B2 83 89 FC 02 1A 77 01 00 00 00 00 00 9B
24 0D 16 55 42 63 6B 92 15 D8 73 9F 15 93 94 3E 1D 76 99 28
FB A1 87 22 01 00 00 00 00 00 00 50 94 9C BE 32 4E AB E3 0A
D2 0A 7D 23 B7 3B EC FF DD 2B 47 78 31 66 9E 84 00 00 00 00
00 00 00 0A 7C D0 3A 5B B9 3F 9C 03 5F 08 93 08 11 67 32 A7
84 EF F9 70 C4 3C 8D 92 00 00 00 00 00 00 00 06 4B 89 4B 39
9D A8 44 BA 3A CB 76 C1 71 AC E8 83 4F 4F 2C F2 65 92 F7 6F
00 00 00 00 00 00 00 E1 00 2C 7E 0B B0 35 4D 73 3D 7E 4E 75
DF 74 AD 94 A9 36 63 61 A2 59 DE 9F 00 00 00 00 00 00 00 83
60 BF 90 A0 C7 D9 F2 7F 38 17 B2 53 E4 8A 6B 54 2A 96 1C 61
33 7D 99 2A 01 00 00 00 00 00 00 E7 86 E4 DD 55 EE F6 20 3B
24 16 5F 4A 45 95 FD 46 1F 25 E8 A4 6E 6A B9 0E 00 00 00 00
00 00 00 00 08 52 53 17 BC 14 1E D7 F9 6C 87 B7 0D 0D 1E 7E
6E 0A 16 3F 21 4B F8 E9 00 00 00 00 00 00 00 AA B8 5E EB 96
34 A7 3A 58 B2 DE 70 95 CA 98 7B 5C 64 BE B7 9D 38 F8 0E 35
01 00 00 00 00 00 00 7D A8 89 B8 1D 1A B2 CF D9 69 E8 AD CF
C0 9C D0 77 71 88 C0 40 A6 6E 85 D9 00 00 00 00 00 00 00 56
E3 51 CA EF 8F 53 00 FD 97 8D 3F 33 CB F1 1C 53 05 6D 58 70
45 A3 DD 89 00 00 00 00 00 00 00 25 6C 2C 22 E3 46 5E 20 23
51 D5 8E 15 9F E7 61 31 3C 61 FF 2B C6 B3 31 C8 00 00 00 00
00 00 00 8F 34 10 64 91 34 2D 00 F8 36 D0 D0 08 9E D9 61 E4
AA 69 35 96 6E 43 E4 15 00 00 00 00 00 00 00 7C 20 7D 6D 09
CE AC 06 70 12 F8 90 FE 7F 88 E7 88 A0 05 2B C8 69 4F BA F7
34 0E 00 00 00 00 00 6E D4 C7 F0 B9 EB 32 F0 91 D3 FA D6 32
88 FB 66 2C 14 E5 D5 3A EB 1E 78 93 00 00 00 00 00 00 00 27
B0 C8 4D 8A 1C 39 35 39 F0 1D FF FE 3B BB 03 EE 54 3B 9E EA
98 6F 22 DF C7 1B 75 00 00 00 00 84 1A 38 62 1E AB 88 21 5D
C0 C7 49 A7 0F D7 B6 AE 32 50 A8 58 B6 5E D8 97 00 00 00 00
00 00 00 EF AC 4D 72 6C DF B4 EA 82 69 E7 17 77 A1 A7 09 10
C7 81 3C AF A9 E4 E3 1A 93 04 16 00 00 00 00 55 3E EB 0E C5
7E 62 EE 50 01 02 F2 A0 87 8E E6 60 F7 3B 8E 4A FE BD 95 B9
7F 10 00 00 00 00 00 6D E8 D6 50 0B F8 F7 03 80 FE 5E 5F 8E
F0 C1 5C 03 BC 4E 6A 5A AD FC D1 7B 18 00 00 00 00 00 00 33
75 FD F8 F1 D1 7F 66 9F FC 31 EC 4C B3 4A A7 29 58 51 35 0C
96 7D FA 3A E2 72 02 00 00 00 00 04 AF C7 C3 CC ED 80 9A 9F
BF C5 04 9E 8D 71 41 47 96 EB 2B 23 C6 7D F3 26 E2 0A 00 00
00 00 00 35 F2 01 F5 2B C0 62 FF 05 B6 D9 C7 21 BF DE 74 9A
D0 02 DD 55 56 C5 40 10 02 00 00 00 00 00 00 73 63 DD E7 92
D3 16 9C 36 EE B6 0E 3D 1F 9F 89 C8 FE 8D B5 7B 9A E0 D0 FC
FC 2F 00 00 00 00 00 E0 AC DA CA EC CD 3D F3 51 7B 31 04 43
E6 0D 65 A3 C2 20 48 DB 8C 12 13 10 00 00 00 00 00 00 00 33
BC DC 94 84 F8 1E 7A 38 8B 42 94 B8 85 EE 41 D4 BA FD 16 75
18 7A 04 65 31 4F 07 00 00 00 00 43 49 7F D7 F8 26 95 71 08
F4 A3 0F D9 CE C3 AE BA 79 97 20 84 E9 0E AD 01 EA 33 09 00
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
*/

    private static int network = 0x0709110B;

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
            header = combine(header, getIntToBytes(network));

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

            Reply reply = new Reply(buffer.toByteArray());
            System.out.println(reply.getCommand());

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
