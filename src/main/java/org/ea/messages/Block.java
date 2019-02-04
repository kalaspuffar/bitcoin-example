package org.ea.messages;

import org.ea.main.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Block extends Reply {
    protected Block(byte[] msg) throws Exception{
        super(msg);

        int version = ByteBuffer.wrap(
                Arrays.copyOfRange(data, 0, 4)
        ).order(ByteOrder.LITTLE_ENDIAN).getInt();

        //System.out.println("BLOCK " + version);

        try {
            byte[] idHash = Utils.dhash(Arrays.copyOfRange(data, 0, 80));
            String id = Utils.byte2hex(idHash);

            File blockDir = new File(Utils.getDataPath(), "blocks");
            File dir = new File(blockDir, id.substring(0, 4));
            dir.mkdirs();
            File file = new File(dir, id);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
