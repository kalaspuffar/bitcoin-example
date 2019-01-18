package org.ea.main;

import org.ea.messages.*;
import org.ea.messages.data.Header;
import org.ea.messages.data.InvVector;
import org.ea.messages.data.NetAddr;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.math.BigInteger;
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
    private static Set<Header> headers = new HashSet<>();

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

        if(jsonDB.containsKey("headers")) {
            JSONArray headerArr = (JSONArray) jsonDB.get("headers");
            for (Object header : headerArr) {
                headers.add(new Header(
                        (JSONObject) header
                ));
            }
        }

        System.out.println("Read " + addresses.size() + " addresses");
        System.out.println("Read " + invVectors.size() + " vectors");
        System.out.println("Read " + headers.size() + " headers");
    }

    private static void writeData(File dbFile) throws Exception {
        JSONObject jsonDB = (JSONObject)
                JSONValue.parse(new FileReader(dbFile));

        if(jsonDB == null) {
            jsonDB = new JSONObject();
        }

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

        jsonDB.put("headers", new JSONArray());
        JSONArray headersJson = (JSONArray) jsonDB.get("headers");
        List<Header> sortedHeaders = new ArrayList<>();
        sortedHeaders.addAll(headers);
        Collections.sort(sortedHeaders);
        for(Header header : sortedHeaders) {
            headersJson.add(header.getJSONObject());
        }

        FileWriter fw = new FileWriter(dbFile);
        if (jsonDB != null) {
            jsonDB.writeJSONString(fw);
        }
        fw.flush();
        fw.close();

        System.out.println("Wrote " + addresses.size() + " addresses");
        System.out.println("Wrote " + invVectors.size() + " vectors");
        System.out.println("Wrote " + headers.size() + " headers");
    }

    public static void main(String[] args) {
        File dataDir = new File("data");
        File dbFile = new File(dataDir, "db.json");

        try {
            if(!dataDir.exists()) dataDir.mkdir();
            if(!dbFile.exists()) dbFile.createNewFile();

            readData(dbFile);
            Utils.handleHeights(headers);

            InetAddress dnsresult[] = InetAddress.getAllByName("seed.tbtc.petertodd.org");
            String ip = null;
            short default_port = 18333;
            if(addresses.size() < 20) {
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

                        GetHeaders getHeadersMsg = new GetHeaders(network);
                        List<Header> headLocators = Utils.blockLocator(headers);
                        for(Header head : headLocators) {
                            getHeadersMsg.addHash(head.getId());
                        }
                        out.write(getHeadersMsg.getByteData());
                        out.flush();


                        //                    1 - b849fd2fc65ef709bb3cbe7e959bcb7549c56e8d54a35ae63fd4f85f
                        //                    2 - 39adc6a805954c9fc038dbfab6d6ae2a0e16f02f3f0cacbf5c000000
/*
                        GetBlocks getBlocksMsg = new GetBlocks(network);
                        List<InvVector> vet = Utils.blockLocator(invVectors);
                        System.out.println("Number of vectors: " + vet.size());
                        for(InvVector iv : vet) {
                            iv.print();
                            getBlocksMsg.addHash(iv.getHash());
                        }
                        out.write(getBlocksMsg.getByteData());
                        out.flush();
*/
/*
                        GetData getData = new GetData(network);
                        for (InvVector iv : invVectors) {
                            getData.addVector(iv);
                        }
                        out.write(getData.getByteData());
                        out.flush();
*/
                    }

                    if (reply instanceof Ping) {
                        out.write(((Ping) reply).getPongData());
                        out.flush();
                    }

                    if (reply instanceof Inv) {
                        List<InvVector> list = ((Inv)reply).getInvVectors();
                        invVectors.addAll(list);
/*
                        if(list.size() == 500) {
                            GetBlocks getHeadersMsg = new GetBlocks(network);
                            List<InvVector> vet = Utils.blockLocator(invVectors);
                            for (InvVector iv : vet) {
                                iv.print();
                                getHeadersMsg.addHash(iv.getHash());
                            }
                            out.write(getHeadersMsg.getByteData());
                            out.flush();
                        }
*/
                    }

                    if (reply instanceof Headers) {
                        List<Header> list = ((Headers)reply).getHeaders();
                        headers.addAll(list);
                        Utils.handleHeights(headers);

                        if(list.size() == 2000) {
                            GetHeaders getHeadersMsg = new GetHeaders(network);
                            List<Header> headLocators = Utils.blockLocator(headers);
                            for(Header head : headLocators) {
                                getHeadersMsg.addHash(head.getId());
                            }
                            out.write(getHeadersMsg.getByteData());
                            out.flush();
                        }
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
