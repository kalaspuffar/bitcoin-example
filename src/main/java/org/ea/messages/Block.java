package org.ea.messages;

import org.ea.main.Utils;
import org.ea.messages.data.Header;
import org.ea.messages.data.Transaction;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Block extends Reply {
    private Header blockHeader;
    private List<Transaction> transactionList = new ArrayList<>();

    public Block() {}

    protected Block(byte[] msg) throws Exception{
        super(msg);
        blockHeader = new Header(data);

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

    public void setData(byte[] data) {
        blockHeader = new Header(data);
        int len = blockHeader.getHeaderLen();

        long numTrans = blockHeader.getTxnCount();

        byte[] transData = Arrays.copyOfRange(data, len + blockHeader.getTxnBytes(), data.length);

        for(int i=0; i<numTrans; i++) {
            Transaction trans = new Transaction();
            transData = trans.setData(transData);
            transactionList.add(trans);
        }
        System.out.println(transData.length);
    }
}
