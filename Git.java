import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

public class Git{
    public Git (){
        File git = new File("git/");
        File objects = new File("git/objects/");
        File index = new File ("git/index");
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

    //filePath - path of blob to be hashed
    //returns true if blob hashes, returns false otherwise
    public Blob (String filePath){
        File blob = new File(filePath);
        //checks if blob exits
        if (!blob.exists()) throw new NoSuchFileException();
        
        //add compression implementation here
        
        int hashCode = Sha1Hash(filePath);
        FileInputStream reader = new FileInputStream(blob);
        BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream("git/objects/" + hashCode));
        while (reader.ready())
            writer.write(reader.read());
        reader.close();
        writer.close();
    }

    public int Sha1Hash (String filePath){
        return 0;
    }

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