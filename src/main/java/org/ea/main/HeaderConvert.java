package org.ea.main;

import org.ea.messages.data.Header;
import org.ea.messages.data.InvVector;
import org.ea.messages.data.NetAddr;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        File headersFile = new File(dataDir, "header.old");
        File newHeadersFile = new File(dataDir, "header.data");

        List<String> ids = new ArrayList<String>();
        try {
            newHeadersFile.createNewFile();

            FileInputStream fis = new FileInputStream(headersFile);
            FileOutputStream fos = new FileOutputStream(newHeadersFile);
            byte[] headerBytes = new byte[80];
            while(fis.read(headerBytes) == 80) {
                String id = new Header(headerBytes).getId();
                if(!ids.contains(id)) {
                    fos.write(headerBytes);
                    ids.add(id);
                }
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
