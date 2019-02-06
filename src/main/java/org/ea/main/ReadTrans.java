package org.ea.main;

import org.ea.messages.Block;
import org.ea.messages.data.Header;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ReadTrans {
    public static void main(String[] args) {
//            String id = "00000d83ce548705a73dee4c1b77b1d33647bfe7d224b41e1c04fd0c00000000";
//            String id = "00001c618fde044e1c53ac3b44b9250357f1784ec1120b168800000000000000";

        File dataDir = new File("data");
        File headersFile = new File(dataDir, "header.data");

        try {
            FileInputStream headerFIS = new FileInputStream(headersFile);
            byte[] headerBytes = new byte[80];
            while (headerFIS.read(headerBytes) == 80) {
                String id = new Header(headerBytes).getId();
                //String id = Utils.reverse("00000000b873e79784647a6c82962c70d228557d24a747ea4d1b8bbe878e1206");
                //System.out.println(Utils.reverse("f0315ffc38709d70ad5647e22048358dd3745f3ce3874223c80a7c92fab0c8ba"));
                System.out.println(Utils.reverse(id));
                System.out.println(id);

                File file = Utils.findFile(id);

                FileInputStream fis = new FileInputStream(file);
                final int BUFFER_SIZE = 1024 * 1024;
                byte[] data = new byte[BUFFER_SIZE];

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int read;
                do {
                    read = fis.read(data);
                    baos.write(data, 0, read);
                } while (read == BUFFER_SIZE);
                byte[] inputData = baos.toByteArray();

                Block block = new Block();
                block.setData(inputData);

                if(block.verifyMerkleRoot()) {
                    System.out.println("ALL RIGHT");
                }

                fis.close();

                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
