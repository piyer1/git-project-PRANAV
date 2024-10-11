1. Yes i coded stage. It has been extensively tested and works very well with files and folders
2. Yes. I tested it with an initial commit of 4 files, and then commited again with an extra file and everything seems to work as expected
3. yes. From the test I have done, it seems to work as intended. It deleted all the files then restored only the ones from an old commit perfectly.
4. null pointer exception from incorrect pathing, not concatenating paths correctly, mistaking tree files and commit files, .git v. ./git, etc. Fixed all of em

How to use gitInterface
Git repo = new Git();

//stage files:
File varName = new File ("./fileName");
repo.stage(varName);

//stage folders:
File varName2 = new File ("./folderName/");
repo.stage(varName);

//commit:
String commitHash = repo.commit(authorName, message);

//checkout:
repo.checkout(commitOfHashIWantToCheckout);
