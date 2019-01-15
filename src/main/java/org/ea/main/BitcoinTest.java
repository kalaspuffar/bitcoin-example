package org.ea.main;

import org.ea.messages.*;
import org.ea.messages.data.InvVector;
import org.ea.messages.data.NetAddr;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

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
    private static Set<NetAddr> addresses = new HashSet<>();
    private static Set<InvVector> invVectors = new HashSet<>();

    public static void readData(File dbFile) throws Exception {
        JSONObject jsonDB = (JSONObject)
                JSONValue.parse(new FileReader(dbFile));
        if(jsonDB == null) jsonDB = new JSONObject();

        if(jsonDB.containsKey("addr")) {
            JSONArray addrArr = (JSONArray) jsonDB.get("addr");
            for (Object addr : addrArr) {
                addresses.add(new NetAddr(
                        (JSONObject) addr
                ));
            }
        }

        if(jsonDB.containsKey("inv")) {
            JSONArray invArr = (JSONArray) jsonDB.get("inv");
            for (Object iv : invArr) {
                invVectors.add(new InvVector(
                        (JSONObject) iv
                ));
            }
        }

        System.out.println("Read " + addresses.size() + " addresses");
        System.out.println("Read " + invVectors.size() + " vectors");
    }

    private static void writeData(File dbFile) throws Exception {
        JSONObject jsonDB = (JSONObject)
                JSONValue.parse(new FileReader(dbFile));

        jsonDB.put("addr", new JSONArray());
        JSONArray addressesJson = (JSONArray) jsonDB.get("addr");
        for(NetAddr addr : addresses) {
            addressesJson.add(addr.getJSONObject());
        }
        jsonDB.put("inv", new JSONArray());
        JSONArray invJson = (JSONArray) jsonDB.get("inv");
        for(InvVector iv : invVectors) {
            invJson.add(iv.getJSONObject());
        }

        FileWriter fw = new FileWriter(dbFile);
        if (jsonDB != null) {
            jsonDB.writeJSONString(fw);
        }
        fw.flush();
        fw.close();

        System.out.println("Wrote " + addresses.size() + " addresses");
        System.out.println("Wrote " + invVectors.size() + " vectors");
    }

    public static void main(String[] args) {
        File dataDir = new File("data");
        File dbFile = new File(dataDir, "db.json");

        try {
            if(!dataDir.exists()) dataDir.mkdir();
            if(!dbFile.exists()) dbFile.createNewFile();

            readData(dbFile);

            if(true) {
                List<InvVector> values = Utils.blockLocator(invVectors);
                for(InvVector iv : values) {
                    iv.print();
                }
                System.exit(0);
            }

            InetAddress dnsresult[] = InetAddress.getAllByName("seed.tbtc.petertodd.org");
            String ip = null;
            short default_port = 18333;
            if(addresses.size() == 0) {
                for (int i = 0; i < dnsresult.length; i++) {
                    if (dnsresult[i].getHostAddress().startsWith("1")) {
                        ip = dnsresult[i].getHostAddress();
                    }
                }
            } else {
                int addressId = new Random().nextInt(addresses.size());
                NetAddr netAddr = addresses.toArray(
                        new NetAddr[addresses.size()]
                )[addressId];
                ip = netAddr.getHostIPv4();
                default_port = netAddr.getPort();
            }

            long randomId = new Random().nextLong();


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

            outer:
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
                byte[] magic = Arrays.copyOfRange(buffer.toByteArray(), 0, 4);

                byte[] currentBlock = new byte[0];

                while(buffer.size() > 0) {
                    if (Arrays.compare(magic, Utils.getIntToBytes(network)) == 0) {
                        int length = ByteBuffer.wrap(Arrays.copyOfRange(buffer.toByteArray(), 16, 20))
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .getInt();
                        length += 24;
                        if (buffer.size() < length) continue outer;
                        currentBlock = Arrays.copyOfRange(buffer.toByteArray(), 0, length);
                        if (buffer.size() >= length) {
                            byte[] rest = Arrays.copyOfRange(buffer.toByteArray(), length, buffer.size());
                            buffer.reset();
                            buffer.write(rest);
                        }
                    }

                    Reply reply = Reply.build(currentBlock);
                    if (reply instanceof Version) {
                        Verack verackMsg = new Verack(network);
                        out.write(verackMsg.getByteData());
                        out.flush();
                    }
                    if (reply instanceof Verack) {
                        SendHeaders sendHeaders = new SendHeaders(network);
                        out.write(sendHeaders.getByteData());
                        out.flush();
                    }

                    if(reply instanceof SendHeaders) {
                        /*
                        GetHeaders getHeadersMsg = new GetHeaders(network);
                        getHeadersMsg.addHash("dde7f4b78f3b38d6262e16ad6c9ae0c567c23c6735563abba5a5bec1f103506b");
                        out.write(getHeadersMsg.getByteData());
                        out.flush();
                        */

                        GetData getData = new GetData(network);
                        for (InvVector iv : invVectors) {
                            getData.addVector(iv);
                        }
                        out.write(getData.getByteData());
                        out.flush();
                    }

                    if (reply instanceof Ping) {
                        out.write(((Ping) reply).getPongData());
                        out.flush();
                    }

                    if (reply instanceof Inv) {
                        invVectors.addAll(((Inv)reply).getInvVectors());
/*
                        GetHeaders getHeadersMsg = new GetHeaders(network);
                        for (InvVector iv : ((Inv) reply).getInvVectors()) {
                            getHeadersMsg.addHash(iv.getHash());
                        }
                        out.write(getHeadersMsg.getByteData());
                        out.flush();
*/
/*
                        GetData getData = new GetData(network);
                        for (InvVector iv : ((Inv) reply).getInvVectors()) {
                            getData.addVector(iv);
                        }
                        out.write(getData.getByteData());
                        out.flush();
*/
                    }

                    if (reply instanceof Addr) {
                        addresses.addAll(((Addr)reply).getAddresses());
                    }

                    if (reply instanceof Reject) {
                        System.out.println(reply);
                    }
                }
                writeData(dbFile);
            }
            is.close();
            out.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writeData(dbFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
