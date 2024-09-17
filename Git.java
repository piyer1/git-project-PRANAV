import java.io.File;
import java.io.IOException;

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

    public static void main (String [] args){
        Git repo = new Git();
    }
}