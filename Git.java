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
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.nio.channels.*;;
public class Git implements GitInterface{
    //if compressFiles is true, git will zip files before caching them
    public static final boolean COMPRESS_FILES = false;
    
    //test commit
    public Git (){
        initializeRepository();
    }

    //constructs a new repository
    public void initializeRepository (){
        File git = new File("./git/");
        File objects = new File("./git/objects/");
        File index = new File ("./git/index");
        File head = new File("./git/HEAD");
        //Checks if repository currently exists
        if (git.exists() && objects.exists() && index.exists() && head.exists())
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
            if (!head.exists()) {
                try {
                    head.createNewFile();}
                catch (IOException e){
                    System.out.println ("Head creation failed");
                    e.printStackTrace();
                }
            }
            
        }
    }

    //string filePath is part of blob to be hashed
    //Uses HashFile for files
    public void stage(String filePath) throws IOException{
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
            for (File child : allFiles){
                stage(child.getPath());
            }
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
                String index = ("tree " + hashCode + " " + file.getPath());
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
            String index = ("blob " + hashCode + " " + file.getPath());
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
        else if (file.isDirectory()) {
            try {
                File[] allFiles = file.listFiles();
                if (allFiles == null) {
                    return null;
                }
                List<String> hashes = new ArrayList<>();
                for (File child : allFiles) {
                    String childHash = Sha1Hash(child);
                    if (childHash != null) {
                        hashes.add(childHash);
                    }
                }
                Collections.sort(hashes);
                MessageDigest digester = MessageDigest.getInstance("SHA-1");
                for (String hash : hashes) {
                    digester.update(hash.getBytes());
                }
                byte[] sha1bytes = digester.digest();
                BigInteger sha1data = new BigInteger(1, sha1bytes);
                String hash = sha1data.toString(16);
                while (hash.length() < 40) {
                    hash = "0" + hash;
                }
                return hash;
            } catch (Exception e) {
                e.printStackTrace();
            }
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


    public String commit(String author, String message) {
        //Gets the line from the head file
        String parent;
        try {
            List<String> al = Files.readAllLines(Paths.get("./git/HEAD"));
            if (al.isEmpty()) {
                parent = "";
            } else {
                parent = al.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Commit failed: HEAD file issue");
            return null;
        }
    
        //creates the fileTree
        List<String> fileTree = new ArrayList<String>();
        try {
            String parentFileTree="";
            if (!parent.equals("")) {
                for (String line : Files.readAllLines(Paths.get("./git/objects/" + parent))) {
                    if (line.startsWith("tree:")) {
                        parentFileTree = line.substring(6);
                    }
                }
                fileTree.addAll(0, Files.readAllLines(Paths.get("./git/objects/" + parentFileTree)));
            }
            fileTree.addAll(0, Files.readAllLines(Paths.get("./git/index")));
            Collections.sort(fileTree);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Commit failed: index file issue");
            return null;
        }
    
        //writes the contents of fileTree to the root tree file and gets its hash
        File rootTree = new File("./git/objects/rootTree");
        BufferedWriter output;
        try {
            output = new BufferedWriter(new FileWriter(rootTree));
            for (int i = 0; i < fileTree.size(); i++) {
                output.write(fileTree.get(i));
                if (i < fileTree.size() - 1) {
                    output.write("\n");
                }
            }
            output.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String rootTreeHash = Sha1Hash(rootTree);
    
        //Adds the whole file tree for this commit to the objects folder
        File bigTreeFile = new File("./git/objects/" + rootTreeHash);
        if (!bigTreeFile.exists()) {
            try {
                Files.copy(rootTree.toPath(), bigTreeFile.toPath());
                rootTree.delete();
            } catch (IOException e) {
                System.out.println("bigTreeFile creation failed");
                e.printStackTrace();
            }
        }
    
        //Creates the actual commit file
        Date date = new Date();
        StringBuilder commitText = new StringBuilder("");
        commitText.append("parent: " + parent + "\n");
        commitText.append("tree: " + rootTreeHash + "\n");
        commitText.append("author: " + author + "\n");
        commitText.append("date: " + date + "\n");
        commitText.append("message: " + message);
        MessageDigest digester;
        String hash = "";
        File commitFile = null;
        try {
            digester = MessageDigest.getInstance("SHA-1");
            byte[] sha1bytes = digester.digest(commitText.toString().getBytes());
            BigInteger sha1data = new BigInteger(1, sha1bytes);
            hash = sha1data.toString(16);
            while (hash.length() < 40) {
                hash = "0" + hash;
            }
            commitFile = new File("./git/objects/" + hash);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
        BufferedWriter commitWriter;
        try {
            commitWriter = new BufferedWriter(new FileWriter(commitFile));
            commitWriter.write(commitText.toString());
            commitWriter.close();
            //updates the HEAD file
            try (BufferedWriter headWriter = new BufferedWriter(new FileWriter("./git/HEAD"))) {
                headWriter.write(hash);
            }
            //clears the index file
            (new File("./git/index")).delete();
            (new File("./git/index")).createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return hash;
    }
    

    public void checkout(String commitHash) {
        //clear workingRepo
        File workingRepo = new File("./workingRepo/");
        if (workingRepo.exists()) {
            removeDirectory(workingRepo);
        }
        workingRepo.mkdir();
        //Finds commit file
        File commitFile = new File("./git/objects/" + commitHash);
        if (!commitFile.exists()) {
            System.out.println("WARNING: Commit " + commitHash + " does not exist.");
            return;
        }
        //finds the tree hash
        String treeHash = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(commitFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("tree: ")) {
                    treeHash = line.substring(6);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        //finds tree file and restores the files into workingRepo
        File treeFile = new File("./git/objects/" + treeHash);
        try (BufferedReader treeReader = new BufferedReader(new FileReader(treeFile))) {
            String line;
            while ((line = treeReader.readLine()) != null) {
                String[] parts = line.split(" ");
                String type = parts[0];
                String fileHash = parts[1];
                String path = parts[2];
                File objectFile = new File("./git/objects/" + fileHash);
                File recreatePath = new File(path);
                if (type.equals("blob")) {
                    remakeBlob(objectFile, recreatePath);
                } else {
                    if (!recreatePath.exists()) {
                        recreatePath.mkdirs();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //halpar method
    private void remakeBlob(File source, File destination) {
        try {
            File parentDir = destination.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            Files.copy(source.toPath(), destination.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}