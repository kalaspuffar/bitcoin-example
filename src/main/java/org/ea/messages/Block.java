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
import java.util.Collections;
import java.util.List;

public class Block extends Reply {
    private Header blockHeader;
    private List<Transaction> transactionList = new ArrayList<>();

    public Block() {}

    protected Block(byte[] msg) throws Exception{
        super(msg);
        blockHeader = new Header(data);
    }

    public void updateData() {
        setData(data);
    }

    public void writeData(boolean incorrect) {

        try {
            byte[] idHash = Utils.dhash(Arrays.copyOfRange(data, 0, 80));
            String id = Utils.byte2hex(idHash);

            File blockDir = new File(Utils.getDataPath(), incorrect ? "incorrect" : "blocks");
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

    public String getId() {
        return blockHeader.getId();
    }

    public void setData(byte[] data) throws IllegalArgumentException {
        blockHeader = new Header(data);
        int len = blockHeader.getHeaderLen();

        long numTrans = blockHeader.getTxnCount();

        byte[] transData = Arrays.copyOfRange(data, len + blockHeader.getTxnBytes(), data.length);

        for(int i=0; i<numTrans; i++) {
            Transaction trans = new Transaction();
            transData = trans.setData(transData);
            transactionList.add(trans);
        }
        //System.out.println(transData.length);
    }

    private byte[] combineMerkleHashes(List<byte[]> hashes) throws Exception {
        if (hashes.size() == 1) {
            return hashes.get(0);
        }
        if (hashes.size() % 2 == 1) {
            hashes.add(hashes.get(hashes.size() - 1));
        }

        List<byte[]> newHashes = new ArrayList<>();
        for (int i = 0; i < hashes.size(); i += 2) {
            byte[] dataToHash = Utils.combine(hashes.get(i), hashes.get(i + 1));
            newHashes.add(Utils.dhash(dataToHash));
        }
        return combineMerkleHashes(newHashes);
    }

    private byte[] combineMerkle(List<Transaction> a) throws Exception {
        if(a.size() > 2) {
            int size = a.size();
            if(a.size() % 2 == 1) {
                 size++;
            }

            byte[] merkle1 = combineMerkle(a.subList(0, size / 2));
            byte[] merkle2 = combineMerkle(a.subList(size / 2, a.size()));
            return Utils.dhash(Utils.combine(merkle1, merkle2));
        } else if(a.size() == 2) {
            Transaction t1 = a.get(0);
            Transaction t2 = a.get(1);
            byte[] hash1 = Utils.dhash(t1.getData());
            byte[] hash2 = Utils.dhash(t2.getData());
            return Utils.dhash(Utils.combine(hash1, hash2));
        } else {
            Transaction t = a.get(0);
            byte[] hash1 = Utils.dhash(t.getData());
            return Utils.dhash(Utils.combine(hash1, hash1));
        }
    }

    public boolean verifyMerkleRoot() {
        try {
            byte[] merkleToVerify;
            if(transactionList.size() == 1) {
                merkleToVerify = Utils.dhash(transactionList.get(0).getData());
            } else {
                List<byte[]> hashes = new ArrayList<>();
                for(Transaction a : transactionList) hashes.add(Utils.dhash(a.getData()));

                merkleToVerify = combineMerkleHashes(hashes);
//                merkleToVerify = combineMerkle(transactionList);
            }
            return Arrays.compare(merkleToVerify, blockHeader.getMerkleRootBytes()) == 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int transSize() {
        return transactionList.size();
    }
}
