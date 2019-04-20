package org.ea.main;

import org.ea.messages.Block;
import org.ea.messages.data.Header;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReadTrans {
    public static void main(String[] args) {
//            String id = "00000d83ce548705a73dee4c1b77b1d33647bfe7d224b41e1c04fd0c00000000";
//            String id = "00001c618fde044e1c53ac3b44b9250357f1784ec1120b168800000000000000";

        File dataDir = new File("data");
        File headersFile = new File(dataDir, "header.data");
        int numOfHeaders = 0;
        int numOfVerified = 0;
        //List<String> incomplete = new ArrayList<>();
        //List<String> incorrect = new ArrayList<>();

        try {
            FileInputStream headerFIS = new FileInputStream(headersFile);
            //byte[] headerBytes = new byte[80];
            final int BUFFER_SIZE = 1024 * 1024;
            byte[] data = new byte[BUFFER_SIZE];
            byte[] buffer = new byte[800000];

            int numRead;
            while((numRead = headerFIS.read(buffer)) != -1) {
                for(int i = 0; i < numRead / 80; i++) {
                    byte[] headerBytes = Arrays.copyOfRange(buffer, i * 80, (i + 1) * 80);

                    //while (headerFIS.read(headerBytes) == 80) {
                    String id = new Header(headerBytes).getId();
                    //String id = "e17c5a88950d81e9024d713edf7a4220bb22ae662ca31938ced10dc900000000";
                    //System.out.println(Utils.reverse("f0315ffc38709d70ad5647e22048358dd3745f3ce3874223c80a7c92fab0c8ba"));
//                System.out.println(Utils.reverse(id));
//                System.out.println(id);

                    File file = Utils.findFile(id);

                    if (file == null || !file.exists()) continue;

                    FileInputStream fis = new FileInputStream(file);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int read;
                    do {
                        read = fis.read(data);
                        baos.write(data, 0, read);
                    } while (read == BUFFER_SIZE);
                    baos.close();
                    byte[] inputData = baos.toByteArray();

                    Block block = new Block();
                    try {
                        block.setData(inputData);

                        if (block.verifyMerkleRoot()) {
                            numOfVerified++;
                        } else {
                            System.out.println("Incorrect: " + block.getId());
                            //file.deleteOnExit();
                        }
                    } catch (Exception iae) {
                        System.out.println("Incomplete");
                        //file.deleteOnExit();
                    }

                    fis.close();
                    numOfHeaders++;

                    if (numOfHeaders % 10000 == 0) {
                        System.out.println(numOfHeaders);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Verified " + numOfVerified + " of " + numOfHeaders);
    }
}
