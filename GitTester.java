import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.io.*;

public class GitTester {
    public static void main (String [] args) throws IOException{
        //initialize repository
        Git repo = new Git();
        File test = new File("./workingRepo/test/");

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
        
        //check Hashing
        repo.stage("./workingRepo/test/");
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
        if (true){
            repo.deleteRepository();
        }
        commitTest();
        if (true){
            repo.deleteRepository();
        }
        
    }
    private static void commitTest() throws IOException {
        Git repo = new Git();
        repo.stage("./workingRepo/test/");
        String commitHash = repo.commit("rizzlord", "word gng");
        //checks if the commit file is created
        File commitFile = new File("./git/objects/" + commitHash);
        if (commitFile.exists()) {
            System.out.println("Commit file creation successful");
        } else {
            System.out.println("WARNING: Commit file creation unsuccessful");
        }
        
        //verifies the contents of the commit file
        String commitFileText = new String(Files.readAllBytes(commitFile.toPath()));
        if (commitFileText.contains("author: rizzlord") && commitFileText.contains("message: word gng")) {
            System.out.println("Commit content correct");
        } else {
            System.out.println("WARNING: Commit content is incorrect");
        }

        //Tests second commit
        //create new file
        File newFile = new File("./workingRepo/newTestFile.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(newFile))) {
            writer.write("skibidi skibidi on the wall who is the sigmaest of them all?");
        }
        repo.stage("./workingRepo/newTestFile.txt");
        //second commit
        String secondCommitHash = repo.commit("rizzlord", "second commit");
        //verify second commit
        File secondCommitFile = new File("./git/objects/" + secondCommitHash);
        if (secondCommitFile.exists()) {
            System.out.println("Second commit file creation successful");
        } else {
            System.out.println("WARNING: Second commit file creation unsuccessful");
        }

        //verify contents of second commit file
        String secondCommitFileText = new String(Files.readAllBytes(secondCommitFile.toPath()));
        if (secondCommitFileText.contains("author: rizzlord") && secondCommitFileText.contains("message: second commit")) {
            System.out.println("Second commit content correct");
        } else {
            System.out.println("WARNING: Second commit content is incorrect");
        }

        

        //tests checkout
        repo.checkout(commitHash);
        if ((new File("./workingRepo/test/")).exists()) {
            System.out.println("Second commit file creation successful");
        } else {
            System.out.println("WARNING: Second commit file creation unsuccessful");
        }
        if(!newFile.exists()) {
            System.out.println("New file deleted correctly");
        } else {
            System.out.println("WARNING: new file deleted incorrectly");
        }
    }
}
