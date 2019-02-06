package org.ea.main;

import org.ea.messages.data.Header;
import org.ea.messages.data.InvVector;
import org.ea.messages.data.NetAddr;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.util.*;

public class HeaderConvert {

    private static Set<Header> headers = new HashSet<>();

    public static void readData(File dbFile) throws Exception {
        JSONObject jsonDB = (JSONObject)
                JSONValue.parse(new FileReader(dbFile));

        if(jsonDB.containsKey("headers")) {
            JSONArray headerArr = (JSONArray) jsonDB.get("headers");
            for (Object header : headerArr) {
                headers.add(new Header(
                        (JSONObject) header
                ));
            }
        }

        System.out.println("Read " + headers.size() + " headers");
    }


    public static void main(String[] args) {
        File dataDir = new File("data");
        File headersFile = new File(dataDir, "header.data");
        File newHeadersFile = new File(dataDir, "header.new");

        try {
            newHeadersFile.createNewFile();

            FileInputStream fis = new FileInputStream(headersFile);
            FileOutputStream fos = new FileOutputStream(newHeadersFile);
            byte[] buffer = new byte[800000];

            int numRead;
            int count = 0;
            String previousId = null;

            readloop:
            while((numRead = fis.read(buffer)) != -1) {
                for(int i = 0; i < numRead / 80; i++) {
                    byte[] headerBytes = Arrays.copyOfRange(buffer, i * 80, (i+1) * 80);
                    Header header = new Header(headerBytes);
                    if(previousId == null || previousId.equals(header.getPrevBlock())) {
                        previousId = header.getId();
                        fos.write(headerBytes);
                    } else {
                        break readloop;
                    }
                }
                System.out.print(".");
                count++;
                if(count % 100 == 0) System.out.println();
            }
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(newHeadersFile.length() / 80);


        /*
        File dbFile = new File(dataDir, "db.json");
        try {
            FileOutputStream fos = new FileOutputStream(headersFile);
            readData(dbFile);
            for(Header header : headers) {
                fos.write(header.getHeaderData());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }
}
