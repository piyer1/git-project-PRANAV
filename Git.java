import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.security.MessageDigest;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;

public class Git{
    //if compressFiles is true, git will zip files before caching them
    private boolean compressFiles = true;
    
    //constructs a new repository
    public Git (){
        File git = new File("./git/");
        File objects = new File("./git/objects/");
        File index = new File ("./git/index");
        //Checks if repository currently exists
        if (git.exists() && objects.exists() && index.exists())
            System.out.println ("Git Repository already exists");
        else{
            if (!git.exists())
                git.mkdir();
            if (!objects.exists())
                objects.mkdir();
            if (!index.exists()){
                try {
                    index.createNewFile();}
                catch (IOException e){
                    System.out.println ("Index creation failed");
                    e.printStackTrace();
                }
            }
        }
    }

    //string filePath is path of blob to be hashed
    //returns true if blob hashes, returns false otherwise
    public void HashData (String filePath){
        File file = new File(filePath);
        String filename = file.getName();
        //checks if blob exits
        if (!file.exists())
            throw new NullPointerException();
            //im not sure that NullPointer is the right exception so feel free to swap it
        
        //compresses the file
        if (compressFiles)
            file = compress(file);
        
        String hashCode = Sha1Hash(file);
        //write to objects directory
        try {
            FileInputStream input = new FileInputStream(file);
            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream("./git/objects/" + hashCode));
            int data = input.read();
            while (data != -1){
                output.write(data);
                data = input.read();
            }
            input.close();
            output.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        // write to index
        try{
            //Checks if filename and hash is already in index
            boolean isInIndex = false;
            String index = (hashCode + " " + filename);
            BufferedReader reader = new BufferedReader(new FileReader("./git/index"));
            while (reader.ready()){
                //System.out.println (reader.readLine());
                // if (index == reader.readLine())
                //     isInIndex = true;
            }
            reader.close();
            if (!isInIndex){
                BufferedWriter writer = new BufferedWriter(new FileWriter("./git/index", true));
                writer.write(index);
                writer.newLine();
                writer.close();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    //returns hexadecimal SHA-1 hash for file
    public String Sha1Hash (File file){
        //implementation is copied from geeksforgeeks
        //https://www.geeksforgeeks.org/sha-1-hash-in-java/
        try {
            MessageDigest digester = MessageDigest.getInstance("SHA-1");
            byte[] sha1bytes = digester.digest(Files.readAllBytes(file.toPath()));
            BigInteger sha1data = new BigInteger(1, sha1bytes);
            String hash = sha1data.toString(16);
            return hash;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //Compresses file
    //If compression fails it will return input file
    public File compress (File file){
        try {
            File compressedFile = File.createTempFile("compress", null);
            FileInputStream input = new FileInputStream(file);
            DeflaterOutputStream output = new DeflaterOutputStream(new FileOutputStream(compressedFile));
            int data = input.read();
            while (data != -1){
                output.write(data);
                data = input.read();
            }
            input.close();
            output.close();
            return compressedFile;
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return file;
    }

    //removes git folder
    public void deleteRepository (){
        File git = new File("git/");
        if (git.exists()){
            removeDirectory(git);
            git.delete();
        }
        
        if (!git.exists())
            System.out.println ("Deletion Successful");
        else
            System.out.println ("Deletion Failed");
    }

    //recursively clears and deletes all files/directories in directory
    public void removeDirectory (File directory){
        if (!directory.isDirectory())
            throw new IllegalArgumentException();
        
        for (File childFile : directory.listFiles()){
            if (childFile.isDirectory())
                removeDirectory(childFile);
            childFile.delete();
        }
    }
}