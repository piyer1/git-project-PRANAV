import java.io.File;
import java.io.IOException;
import java.io.*;


public class GitTester {
    public static void main (String [] args) throws IOException{
        //initialize repository
        Git repo = new Git();
        File test = new File("./test/");

        //check repository initialization
        File git = new File ("./git/");
        File objects = new File ("./git/objects/");
        File index = new File ("./git/index");
        File head = new File ("./git/HEAD");
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
        if (head.exists())
            System.out.println ("/head  creation success");
        else
            System.out.println ("WARNING: head creation failure");
        
        //check Sha1Hash
        if (repo.Sha1Hash(test).equals("9054fbe0b622c638224d50d20824d2ff6782e308"))
            System.out.println ("Sha1Hash functioning properly");
        else
            System.out.println ("WARNING: Sha1Hash method not functioning properly (or you changed testData.txt)");
        
        //check Hashing
        repo.createBlobGeneral("./test/");
        boolean isInIndex = false;
        String hashCode;
        //finds compressed hashcode if nessesary
        if (Git.COMPRESS_FILES)
            hashCode = repo.Sha1Hash(repo.compress(test));
        else
            hashCode = repo.Sha1Hash(test);
        
        //checks to see if in index
        String expectedIndex = ("tree " + hashCode + " " + test.getPath());
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
        if (false){
            repo.deleteRepository();
        }
        
    }
}
