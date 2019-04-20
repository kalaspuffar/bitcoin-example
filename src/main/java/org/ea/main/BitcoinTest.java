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
    private static Set<String> blocksToDownload = new HashSet<>();
    private static int lastReported;

    private static byte[] getCommand(String cmd) {
        byte[] res = new byte[12];
        Arrays.fill(res, (byte)0);
        System.arraycopy(cmd.getBytes(), 0, res, 0, cmd.length());
        return res;
    }

    private static int network = 0x0709110B;
    private static Set<NetAddr> addresses = new HashSet<>();
    //private static Set<InvVector> invVectors = new HashSet<>();
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

        /*
        if(jsonDB.containsKey("inv")) {
            JSONArray invArr = (JSONArray) jsonDB.get("inv");
            for (Object iv : invArr) {
                invVectors.add(new InvVector(
                        (JSONObject) iv
                ));
            }
        }
        */

        System.out.println("Read " + addresses.size() + " addresses");
        //System.out.println("Read " + invVectors.size() + " vectors");

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
        /*
        jsonDB.put("inv", new JSONArray());
        JSONArray invJson = (JSONArray) jsonDB.get("inv");
        for(InvVector iv : invVectors) {
            invJson.add(iv.getJSONObject());
        }
        */

        FileWriter fw = new FileWriter(dbFile);
        if (jsonDB != null) {
            jsonDB.writeJSONString(fw);
        }
        fw.flush();
        fw.close();

        System.out.println("Wrote " + addresses.size() + " addresses");
        //System.out.println("Wrote " + invVectors.size() + " vectors");

        long numBlocks = 0;
        for(File f : Utils.getDataPath().listFiles()) {
            if(f.isDirectory()) {
                numBlocks += f.listFiles().length;
            }
        }
        System.out.println("Blocks downloaded " + numBlocks);
    }

    public static void readBlocksToRead(File headersFile) {
        if(headersFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(headersFile);
                byte[] buffer = new byte[800000];

                int numRead;
                int count = 0;
                int dup = 0;
                while ((numRead = fis.read(buffer)) != -1) {
                    for (int i = 0; i < numRead / 80; i++) {
                        byte[] headerBytes = Arrays.copyOfRange(buffer, i * 80, (i + 1) * 80);
                        String id = Utils.getId(headerBytes);
                        if (!Utils.findFileName(id)) {
                            if (blocksToDownload.contains(id)) {
                                dup++;
                            }
                            blocksToDownload.add(id);
                        }
                    }
                    System.out.print(".");
                    count++;
                    if (count % 100 == 0) System.out.println();
                }

                System.out.println("Num dups: " + dup);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        File dataDir = Utils.getDataPath();
        File dbFile = new File(dataDir, "db.json");
        File headersFile = new File(dataDir, "header.data");

        long lastBlock = (headersFile.length() / 80);
        System.out.println("\nHave " + lastBlock + " headers");

        int connectionsDone = 11;
        while(true) {
            lastBlock = (headersFile.length() / 80);
            System.out.println("\nHave " + lastBlock + " headers");

            try {
                if(!dataDir.exists()) dataDir.mkdir();
                if(!dbFile.exists()) dbFile.createNewFile();

                if(connectionsDone > 10) {
                    readBlocksToRead(headersFile);
                    connectionsDone = 0;
                }
                connectionsDone++;
                readData(dbFile);

                if (addresses.size() < 20) {
                    InetAddress dnsresult[] = InetAddress.getAllByName("seed.tbtc.petertodd.org");
                    for (int i = 0; i < dnsresult.length; i++) {
                        NetAddr netAddr = new NetAddr();
                        netAddr.setIpv4(dnsresult[i].getHostAddress());
                        netAddr.setPort((short) 18333);
                        addresses.add(netAddr);
                    }
                }
                int addressId = new Random().nextInt(addresses.size());
                NetAddr netAddr = addresses.toArray(
                        new NetAddr[addresses.size()]
                )[addressId];
                String ip = netAddr.getHostIPv4();
                short default_port = netAddr.getPort();

                long randomId = new Random().nextLong();

                clientSocket = new Socket(ip, default_port);
                OutputStream out = clientSocket.getOutputStream();
                InputStream is = clientSocket.getInputStream();

                Version versionMsg = new Version();
                versionMsg.setVersion(70013);
                versionMsg.setTimestamp(Instant.now().getEpochSecond());
                versionMsg.setNodeId(Utils.getLongToBytes(randomId));
                versionMsg.setLastBlock((int)lastBlock);
                versionMsg.setNetwork(network);

                out.write(versionMsg.getByteData());
                out.flush();

                int nRead;
                byte[] data = new byte[5 * 1024 * 1024];
                byte[] workBuffer = new byte[0];

                outer:
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    workBuffer = Utils.combine(workBuffer, Arrays.copyOfRange(data, 0, nRead));
                    byte[] magic = Arrays.copyOfRange(workBuffer, 0, 4);

                    byte[] currentBlock = new byte[0];

                    while (workBuffer.length > 0) {
                        if (Arrays.compare(magic, Utils.getIntToBytes(network)) == 0) {
                            int length = ByteBuffer.wrap(Arrays.copyOfRange(workBuffer, 16, 20))
                                    .order(ByteOrder.LITTLE_ENDIAN)
                                    .getInt();
                            length += 24;
                            if (workBuffer.length < length) continue outer;
                            currentBlock = Arrays.copyOfRange(workBuffer, 0, length);
                            if (workBuffer.length >= length) {
                                workBuffer = Arrays.copyOfRange(workBuffer, length, workBuffer.length);
                            }
                        }

                        Reply reply = Reply.build(currentBlock);
                        if (reply instanceof Version) {
                            lastReported = ((Version)reply).getLastBlock();
                            Verack verackMsg = new Verack(network);
                            out.write(verackMsg.getByteData());
                            out.flush();
                        } else if (reply instanceof Verack) {
                            SendHeaders sendHeaders = new SendHeaders(network);
                            out.write(sendHeaders.getByteData());
                            out.flush();
                        } else if (reply instanceof SendHeaders) {
                            if(lastBlock < lastReported) {
                                GetHeaders getHeadersMsg = new GetHeaders(network);
                                List<Header> headLocators = Utils.blockLocator(headersFile);
                                for (Header head : headLocators) {
                                    getHeadersMsg.addHash(head.getId());
                                }
                                out.write(getHeadersMsg.getByteData());
                                out.flush();
                            } else {
                                GetData getData = new GetData(network);
                                int count = 0;
                                Iterator<String> it = blocksToDownload.iterator();
                                while(it.hasNext()) {
                                    String id = it.next();
                                    if(count > 10) break;
                                    getData.addVector(new InvVector(2, id));
                                    count++;
                                }
                                out.write(getData.getByteData());
                                out.flush();
                            }
                        } else if (reply instanceof Ping) {
                            out.write(((Ping) reply).getPongData());
                            out.flush();
                        } else if (reply instanceof Inv) {
                            /*
                            List<InvVector> list = ((Inv) reply).getInvVectors();
                            invVectors.addAll(list);
                            writeData(dbFile);
                            */

                            GetData getData = new GetData(network);
                            int count = 0;
                            Iterator<String> it = blocksToDownload.iterator();
                            while(it.hasNext()) {
                                String id = it.next();
                                if(count > 10) break;
                                getData.addVector(new InvVector(2, id));
                                count++;
                            }
                            out.write(getData.getByteData());
                            out.flush();
                            System.out.println("Blocks left in queue " + blocksToDownload.size());
                        } else if (reply instanceof Headers) {
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
                            } else {
                                GetData getData = new GetData(network);
                                int count = 0;
                                Iterator<String> it = blocksToDownload.iterator();
                                while (it.hasNext()) {
                                    String id = it.next();
                                    if (count > 10) break;
                                    getData.addVector(new InvVector(2, id));
                                    count++;
                                }
                                out.write(getData.getByteData());
                                out.flush();
                            }
                        } else if (reply instanceof Block) {
                            Block b = ((Block) reply);
                            try {
                                b.updateData();
                                if(b.verifyMerkleRoot()) {
                                    b.writeData(false);
                                } else {
                                    System.err.println("Incorrect merkle " + b.transSize());
                                    b.writeData(true);
                                }
                                String id = b.getId();
                                blocksToDownload.remove(id);
                            } catch (Exception e) {
                                e.printStackTrace();
                                b.writeData(true);
                            }
                        } else if (reply instanceof Addr) {
                            addresses.addAll(((Addr) reply).getAddresses());
                            writeData(dbFile);
                        } else if (reply instanceof Reject) {
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
                System.out.println("Blocks left in queue " + blocksToDownload.size());

                try {
                    writeData(dbFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
