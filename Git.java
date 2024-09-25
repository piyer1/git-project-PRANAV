import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.zip.DeflaterOutputStream;
import java.nio.channels.*;;

public class Git{
    //if compressFiles is true, git will zip files before caching them
    public static final boolean COMPRESS_FILES = false;
    
    public Git (){
        initializeRepository();
    }

    //constructs a new repository
    public void initializeRepository (){
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

    //string filePath is part of blob to be hashed
    //Uses HashFile for files
    public void createBlobGeneral(String filePath) throws IOException{
        File file = new File(filePath);
        String filename = file.getName();
        //checks if blob exits
        if (!file.exists()){
            throw new NullPointerException();
            //im not sure that NullPointer is the right exception so feel free to swap it
        }
        if (file.isFile()){
            HashFile(file, filename);
        }
        else{ 
            String hashCode = Sha1Hash(file);
            File file_objects = new File ("./git/objects/" + hashCode);
            FileChannel source = null;
            FileChannel destination = null;
            File[] allFiles = file.listFiles();
            File file_combined = new File("./combined" + file.getName());
            for (File child : allFiles){
                try {
                    //write to objects directory
                    BufferedWriter output = new BufferedWriter(new FileWriter(file_combined));
                    output.write(child.getName());
                    output.newLine();
                    output.close();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            try {
                source = new FileInputStream(file_combined).getChannel();
                destination = new FileOutputStream(file_objects).getChannel();
                destination.transferFrom(source, 0, source.size());
                boolean bool1 = file_combined.delete();
            }
            finally {
                if(source != null) {
                    source.close();
                }
                if(destination != null) {
                    destination.close();
                }
            }
            
            // write to index
            try{
                //Checks if filename and hash is already in index
                boolean isInIndex = false;
                String index = ("tree " + hashCode + " " + filename);
                BufferedReader reader = new BufferedReader(new FileReader("./git/index"));
                while (reader.ready()){
                    if (index.equals(reader.readLine()))
                        isInIndex = true;
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
            for (File child : file.listFiles()){
                createBlobGeneral(child.getPath());
            }
        }
    }

    //string filePath is path of blob to be hashed
    //returns true if blob hashes, returns false otherwise
    public void HashFile (File file, String filename){
        //compresses the file
        if (COMPRESS_FILES)
            file = compress(file);
        
        String hashCode = Sha1Hash(file);
        //checks if file is already stored
        File storedFile = new File("./git/objects/" + hashCode);
        if (!storedFile.exists()){
            try {
                //write to objects directory
                FileInputStream input = new FileInputStream(file);
                BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(storedFile));
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
        }

        // write to index
        try{
            //Checks if filename and hash is already in index
            boolean isInIndex = false;
            String index = ("blob " + hashCode + " " + filename);
            BufferedReader reader = new BufferedReader(new FileReader("./git/index"));
            while (reader.ready()){
                if (index.equals(reader.readLine()))
                    isInIndex = true;
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
        if (file.isFile()){
            try {
                MessageDigest digester = MessageDigest.getInstance("SHA-1");
                byte[] sha1bytes = digester.digest(Files.readAllBytes(file.toPath()));
                BigInteger sha1data = new BigInteger(1, sha1bytes);
                String hash = sha1data.toString(16);
                while (hash.length() < 40) {
                    hash = "0" + hash;
                }
                return hash;
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else{
            File[] allFiles = file.listFiles();
            File file_combined = new File("./combined" + file.getName());
            for (File child : allFiles){
                try {
                    //write to objects directory
                    BufferedWriter output = new BufferedWriter(new FileWriter(file_combined));
                    output.write(child.getName());
                    output.newLine();
                    output.close();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            String hash = Sha1Hash(file_combined);
            boolean bool1 = file_combined.delete();
            return (hash);
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