import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;


public class GitTester {
    public static void main (String [] args){
        //initialize repository
        Git repo = new Git();
        File test = new File("testData.txt");

        //check repository initialization
        File git = new File ("./git/");
        File objects = new File ("./git/objects/");
        File index = new File ("./git/index");
        if (git.exists())
            System.out.println ("/git directory creation success");
        else
            System.out.println ("WARNING: git directory creation failure");
        if (objects.exists())
            System.out.println ("/git/objects directory creation success");
        else
            System.out.println ("WARNING: objects directory creation failure");
        if (index.exists())
            System.out.println ("/index  creation success");
        else
            System.out.println ("WARNING: index creation failure");
        
        //check Sha1Hash
        if (repo.Sha1Hash(test).equals("d720c05e3787a35beeb764524814e83ed26d0e7d"))
            System.out.println ("Sha1Hash functioning properly");
        else
            System.out.println ("WARNING: Sha1Hash method not functioning properly (or you changed testData.txt)");

        //check Hashing
        repo.HashData("testData.txt");
        boolean isInIndex = false;
        String hashCode;
        //finds compressed hashcode if nessesary
        if (repo.COMPRESS_FILES)
            hashCode = repo.Sha1Hash(repo.compress(test));
        else
            hashCode = repo.Sha1Hash(test);
        
        //checks to see if in index
        String expectedIndex = (hashCode + " " + test.getName());
        try {
            BufferedReader reader = new BufferedReader(new FileReader("./git/index"));
            while (reader.ready()){
                if (expectedIndex.equals(reader.readLine()))
                    isInIndex = true;
            }
            reader.close();
        }
        catch (IOException e){
            e.printStackTrace();;
        }
        if (isInIndex)
            System.out.println("Blob indexing successful");
        else
            System.out.println("WARNING: Blob indexing unsuccessful");
        
        //check to see if in objects
        File blob = new File("./git/objects/" + hashCode);
        if (blob.exists())
            System.out.println ("Blob object creation successful");
        else
            System.out.println ("WARNING: Blob object creation unsuccessful");

        //leave true if you want repository to reset at end of test
        if (true){
            repo.deleteRepository();
        }
    }
}
