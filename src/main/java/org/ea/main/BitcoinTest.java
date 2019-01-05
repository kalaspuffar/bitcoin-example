package org.ea.main;

import org.ea.messages.*;

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

    private static byte[] getCommand(String cmd) {
        byte[] res = new byte[12];
        Arrays.fill(res, (byte)0);
        System.arraycopy(cmd.getBytes(), 0, res, 0, cmd.length());
        return res;
    }

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

            Version versionMsg = new Version();
            versionMsg.setVersion(70015);
            versionMsg.setTimestamp(Instant.now().getEpochSecond());
            versionMsg.setNodeId(Utils.getLongToBytes(randomId));
            versionMsg.setLastBlock(0);
            versionMsg.setNetwork(network);

            out.write(versionMsg.getByteData());
            out.flush();

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[5 * 1024 * 1024];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
                byte[] magic = Arrays.copyOfRange(buffer.toByteArray(), 0, 4);

                byte[] currentBlock = new byte[0];

                if(Arrays.compare(magic, Utils.getIntToBytes(network)) == 0) {
                    int length = ByteBuffer.wrap(Arrays.copyOfRange(buffer.toByteArray(), 16, 20))
                            .order(ByteOrder.LITTLE_ENDIAN)
                            .getInt();
                    length += 24;
                    if(buffer.size() < length) continue;
                    currentBlock = Arrays.copyOfRange(buffer.toByteArray(), 0, length);
                    if(buffer.size() > length) {
                        byte[] rest = Arrays.copyOfRange(buffer.toByteArray(), length, buffer.size());
                        buffer.reset();
                        buffer.write(rest);
                    }
                }

                Reply reply = Reply.build(currentBlock);
                if(reply instanceof Version) {
                    Verack verackMsg = new Verack();
                    verackMsg.setNetwork(network);
                    out.write(verackMsg.getByteData());
                    out.flush();
                }
                if(reply instanceof Ping) {
                    out.write(((Ping) reply).getPongData());
                    out.flush();
                }
                if(reply instanceof Reject) {
                    System.out.println(((Reject) reply).getMessage());
                }
            }
            is.close();
            out.close();
            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
