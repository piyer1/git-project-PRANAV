import java.io.File;

public class GitTester {
    public static void main (String [] args){
        Git repo = new Git();

        System.out.println(repo.Sha1Hash("GitTester.java"));

        repo.Blob("testData.txt");
        repo.Blob("GitTester.java");

        //change if statement to delete or not to delete repository after code runs
        if (false)
            repo.deleteRepository();
    }
}
