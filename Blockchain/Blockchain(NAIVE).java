//Cr34t0r: @p1nkpengw1n
//This is a naive/basic implementation of a blockchain in Java. Blocks are added w/o proof of work. The SHA-256 algorithm is not a personal implementation, but an official one.  
//30.07.19//

package blockchain;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;

class StringUtil {
    /* Applies Sha256 to a string and returns a hash. */
    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            /* Applies sha256 to our input */
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte elem : hash) {
                String hex = Integer.toHexString(0xff & elem);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

class Blockchain implements Serializable {
    private static final long serialVersionUID = 7L;

    private List<Block> blocks;
    private int idIncrementer = 1;
    private long startTime;
    private long endTime;
    private long elapsedTime;
    private int numberOfZeroes;
    private String numberOfZeroesString;

    public Blockchain(int numberOfZeroes) {
        blocks = new ArrayList<>();
        this.numberOfZeroes = numberOfZeroes;
        int copyOfZeroes = numberOfZeroes;
        StringBuilder sb = new StringBuilder();
        while(copyOfZeroes > 0) {
            sb.append('0');
            copyOfZeroes--;
        }
        numberOfZeroesString = sb.toString();
        startTime = new Date().getTime();
        Block b = new Block(idIncrementer, "0");
        while(false == b.getHashCode().substring(0,numberOfZeroes).equals(numberOfZeroesString)) {
            b = new Block(idIncrementer, "0");
        }
        blocks.add(b);
        endTime = new Date().getTime();
        elapsedTime = endTime - startTime;
        b.setElapsedTime(elapsedTime);
        idIncrementer++;
    }

    public void addBlock() {
        startTime = new Date().getTime();
        Block b = new Block(this.getIdIncrementer(),this.getBlocks().get(this.getIdIncrementer()-2).getHashCode());
        while(false == b.getHashCode().substring(0,numberOfZeroes).equals(numberOfZeroesString)) {
            b = new Block(this.getIdIncrementer(),this.getBlocks().get(this.getIdIncrementer()-2).getHashCode());
        }
        endTime = new Date().getTime();
        elapsedTime = endTime - startTime;
        b.setElapsedTime(elapsedTime);
        blocks.add(b);
        idIncrementer++;
    }

    public boolean validate() {
        Block currentBlock;
        Block previousBlock;

        for(int i = blocks.size()-1; i > 1; i--) {
            currentBlock = blocks.get(i);
            previousBlock = blocks.get(i-1);

            if(currentBlock.getPrevHashCode().equals(previousBlock.getHashCode())
            && currentBlock.getHashCode().substring(0,numberOfZeroes).equals(numberOfZeroesString)) {
                continue;
            }
            return false;
        }
        return true;
    }

    public int getIdIncrementer() {
        return idIncrementer;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void serialize() {
        try {
            FileOutputStream file = new FileOutputStream("Blockchain.ser");
            ObjectOutputStream outstr = new ObjectOutputStream (file);
            outstr.writeObject(this);
            outstr.close();
            file.close();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Blockchain deserialize(String fileName) {
        try{
            FileInputStream file = new FileInputStream(fileName);
            ObjectInputStream instr = new ObjectInputStream (file);
            Blockchain bc = (Blockchain)instr.readObject();
            instr.close();
            file.close();
            return bc;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void show(Blockchain bc) {
        for(Block b: bc.getBlocks()) {
            System.out.println();
            System.out.println("Block:");
            System.out.print("Id: ");
            System.out.println(b.getId());
            System.out.print("Timestamp: ");
            System.out.println(b.getTimeStamp());
            System.out.print("Magic number: ");
            System.out.println(b.getMagicNumber());
            System.out.println("Hash of the previous block:");
            System.out.println(b.getPrevHashCode());
            System.out.println("Hash of the block:");
            System.out.println(b.getHashCode());
            System.out.println("Block was generating for " + b.getElapsedTime()/1000 + "." + b.getElapsedTime() % 1000 + " seconds");
        }
    }
}

class Block implements Serializable {

    private int id;
    private long timeStamp;
    private String hashCode;
    private String prevHashCode;
    private long elapsedTime;
    private int magicNumber;

    public Block(int id, String prevHashCode) {
        this.id = id;
        this.prevHashCode = prevHashCode;
        this.hashCode = StringUtil.applySha256(this.toString());
        this.timeStamp = new Date().getTime();
        Random rand = new Random();
        this.magicNumber = 10000000 + rand.nextInt(90000000);
    }

    public int getId() {
        return id;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getHashCode() {
        return hashCode;
    }

    public String getPrevHashCode() {
        return prevHashCode;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public int getMagicNumber() {
        return magicNumber;
    }

    @Override
    public String toString() {
        return super.toString() + this.magicNumber;
    }
}

//DEMO-RUN

public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter how many zeroes the hash must start with: ");
        int numberOfZeroes = sc.nextInt();

        Blockchain blockchain = new Blockchain(numberOfZeroes);
        blockchain.serialize(); //serializing after each block added
        blockchain.addBlock();
        blockchain.serialize();
        blockchain.addBlock();
        blockchain.serialize();
        blockchain.addBlock();
        blockchain.serialize();
        blockchain.addBlock();
        blockchain.serialize();

        Blockchain.show(blockchain);

        //Deserialization test after serialization, with validating assertions

        Blockchain copy = Blockchain.deserialize("Blockchain.ser");
        if(true == copy.validate()) {
            Blockchain.show(copy);
        }
    }
}
