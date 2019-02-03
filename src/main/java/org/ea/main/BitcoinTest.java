package org.ea.main;

import org.ea.messages.*;
import org.ea.messages.data.Header;
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

        System.out.println("Read " + addresses.size() + " addresses");
        System.out.println("Read " + invVectors.size() + " vectors");

        long numBlocks = 0;
        for(File f : Utils.getDataPath().listFiles()) {
            if(f.isDirectory()) {
                numBlocks += f.listFiles().length;
            }
        }
        System.out.println("Blocks downloaded " + numBlocks);
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

        FileWriter fw = new FileWriter(dbFile);
        if (jsonDB != null) {
            jsonDB.writeJSONString(fw);
        }
        fw.flush();
        fw.close();

        System.out.println("Wrote " + addresses.size() + " addresses");
        System.out.println("Wrote " + invVectors.size() + " vectors");

        long numBlocks = 0;
        for(File f : Utils.getDataPath().listFiles()) {
            if(f.isDirectory()) {
                numBlocks += f.listFiles().length;
            }
        }
        System.out.println("Blocks downloaded " + numBlocks);
    }

    public static void main(String[] args) {
        File dataDir = Utils.getDataPath();
        File dbFile = new File(dataDir, "db.json");
        File headersFile = new File(dataDir, "header.data");

        List<String> blocksToDownload = new ArrayList<>();

        try {
            FileInputStream fis = new FileInputStream(headersFile);
            byte[] buffer = new byte[800000];

            int numRead;
            int count = 0;
            while((numRead = fis.read(buffer)) != -1) {
                for(int i = 0; i < numRead / 80; i++) {
                    byte[] headerBytes = Arrays.copyOfRange(buffer, i * 80, (i+1) * 80);
                    String id = Utils.getId(headerBytes);
                    if (!Utils.findFileName(id)) {
                        blocksToDownload.add(id);
                    }
                }
                System.out.print(".");
                count++;
                if(count % 100 == 0) System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        long lastBlock = (headersFile.length() / 80);
        System.out.println("\nHave " + lastBlock + " headers");

        while(true) {
            try {
                if(!dataDir.exists()) dataDir.mkdir();
                if(!dbFile.exists()) dbFile.createNewFile();

                readData(dbFile);

                String ip = null;
                short default_port = 18333;
                if (addresses.size() < 20) {
                    InetAddress dnsresult[] = InetAddress.getAllByName("seed.tbtc.petertodd.org");
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
                versionMsg.setLastBlock((int)lastBlock);
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

                    while (buffer.size() > 0) {
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

                        if (reply instanceof SendHeaders) {

                            GetData getData = new GetData(network);
                            int count = 0;
                            Iterator<String> it = blocksToDownload.iterator();
                            while(it.hasNext()) {
                                String id = it.next();
                                it.remove();
                                if(count > 1000) break;
                                getData.addVector(new InvVector(2, id));
                                count++;
                            }
                            out.write(getData.getByteData());
                            out.flush();

                            System.out.println("Blocks left in queue " + blocksToDownload.size());
                        }

                        if (reply instanceof Ping) {
                            out.write(((Ping) reply).getPongData());
                            out.flush();
                        }

                        if (reply instanceof Inv) {
                            List<InvVector> list = ((Inv) reply).getInvVectors();
                            invVectors.addAll(list);
                            writeData(dbFile);

                            GetData getData = new GetData(network);
                            int count = 0;
                            Iterator<String> it = blocksToDownload.iterator();
                            while(it.hasNext()) {
                                String id = it.next();
                                it.remove();
                                if(count > 1000) break;
                                getData.addVector(new InvVector(2, id));
                                count++;
                            }
                            out.write(getData.getByteData());
                            out.flush();

                            System.out.println("Blocks left in queue " + blocksToDownload.size());
                        }

                        if (reply instanceof Headers) {
                            List<Header> list = ((Headers) reply).getHeaders();
                            Utils.handleHeights(headersFile, list);

                            if (list.size() == 2000) {
                                GetHeaders getHeadersMsg = new GetHeaders(network);
                                List<Header> headLocators = Utils.blockLocator(headersFile);
                                for (Header head : headLocators) {
                                    getHeadersMsg.addHash(head.getId());
                                }
                                out.write(getHeadersMsg.getByteData());
                                out.flush();
                            }
                        }

                        if (reply instanceof Addr) {
                            addresses.addAll(((Addr) reply).getAddresses());
                            writeData(dbFile);
                        }

                        if (reply instanceof Reject) {
                            System.out.println(reply);
                        }
                    }
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
}
