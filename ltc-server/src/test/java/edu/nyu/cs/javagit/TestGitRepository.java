/**
 ************************ 80 columns *******************************************
 * TestGitRepository
 *
 * Created on Oct 1, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package edu.nyu.cs.javagit;

import edu.nyu.cs.javagit.api.*;
import edu.nyu.cs.javagit.api.commands.IGitAdd;
import edu.nyu.cs.javagit.api.commands.IGitRemote;
import edu.nyu.cs.javagit.api.options.GitAddOptions;
import edu.nyu.cs.javagit.api.responses.AbstractResponse;
import edu.nyu.cs.javagit.api.responses.GitAddResponse;
import edu.nyu.cs.javagit.api.responses.GitCommitResponse;
import edu.nyu.cs.javagit.api.responses.GitRemoteResponse;
import edu.nyu.cs.javagit.client.Factory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;

/**
 * Testing various versions of creating a git repository and files.
 *
 * @author linda
 */
public final class TestGitRepository {

    private final static String gitPath = "/usr/local/git/bin"; // location of git if not on PATH

    private static File tempGitDir;
    private final static String DIR_STRUCTURE = "A" + File.separator + "B";
    private static File tempRemoteDir;

    // commands
    private final IGitAdd gitAdd = Factory.createGitAdd();
    private final IGitRemote gitRemote = Factory.createGitRemote();

    @BeforeClass
    public static void createDirs() {
        try {
            JavaGitConfiguration.setGitPath(gitPath);

            // create .git in temporary location
            tempGitDir = File.createTempFile("testGit-", "", null);
            tempGitDir.delete();
            if (!tempGitDir.mkdir())
                throw new IOException("Couldn't create temp dir at "+tempGitDir.getAbsolutePath());
            assertTrue(tempGitDir.isDirectory());
            HelperGitCommands.initRepo(tempGitDir);

            // create structure A/B/ and files
            File subdir = new File(tempGitDir, DIR_STRUCTURE);
            if (!subdir.mkdirs())
                throw new IOException("Couldn't create directory structure "+subdir.getAbsolutePath());
            for (String filename : new String[] {"bla", "foo"}) {
                File file = new File(subdir, filename);
                if (!file.createNewFile())
                    throw new IOException("Couldn't create file at "+ file.getAbsolutePath());
            }

            // create .git in remote location
            tempRemoteDir = File.createTempFile("testGitRemote-", "", null);
            tempRemoteDir.delete();
            if (!tempRemoteDir.mkdir())
                throw new IOException("Couldn't create remote dir at "+tempRemoteDir.getAbsolutePath());
            assertTrue(tempRemoteDir.isDirectory());
            HelperGitCommands.initRepo(tempRemoteDir, "--bare");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1); // cannot proceed
        } catch (JavaGitException e) {
            e.printStackTrace();
            System.exit(2); // cannot proceed
        }
    }

    @AfterClass
    public static void deleteTempFiles() throws FileNotFoundException {
        if (tempGitDir != null)
            deleteRecursive(tempGitDir);
        System.out.println();
    }

    @Before
    public void newline() {
        System.out.println();
    }

    @Before
    public void updatePath() {
        try {
            JavaGitConfiguration.setGitPath(gitPath);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JavaGitException e) {
            e.printStackTrace();
        }
    }
    
    private static void deleteRecursive(File f) throws FileNotFoundException {
        if (f.isDirectory())
            for (File c : f.listFiles())
                deleteRecursive(c);
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

    @Test(expected=JavaGitException.class)
    public void noGitInPath() throws JavaGitException {
        DotGit.getInstance(tempGitDir.getParentFile());
    }

    private DotGit obtainDotGit(File file) {
        DotGit dotGit = null;
        try {
            dotGit = DotGit.getInstance(file);
        } catch (JavaGitException e) {
            e.printStackTrace();
        }
        return dotGit;
    }

    @Test
    public void obtainDotGitFromBla() {
        DotGit dotGit = obtainDotGit(new File(tempGitDir, DIR_STRUCTURE + File.separator + "bla"));
        assertTrue(dotGit != null);
        System.out.println("Obtained GIT instance at "+dotGit.getPath().getAbsolutePath());
        assertTrue(dotGit.getPath().getAbsolutePath().equals(tempGitDir.getAbsolutePath()));
    }

    @Test
    public void statusOfBla() {
        DotGit dotGit = obtainDotGit(tempGitDir);
        assertTrue(dotGit != null);
        try {
            // untracked
            GitFile gitFile = dotGit.getWorkingTree().getFile(new File(tempGitDir, DIR_STRUCTURE + File.separator + "bla"));
            assertTrue(GitFileSystemObject.Status.UNTRACKED.equals(gitFile.getStatus()));
            System.out.println("File "+gitFile.getRelativePath()+" is untracked");
            // added
            GitAddResponse addResponse = gitAdd.add(gitFile.getWorkingTree().getPath(),
                    new GitAddOptions(false, false, false, false, false, true),
                    gitFile.getRelativePath());
            assertTrue(GitFileSystemObject.Status.ADDED.equals(gitFile.getStatus()));
            System.out.println("File "+gitFile.getRelativePath()+" is added with response: "+addResponse.getAddedFiles());
            // in repository
            gitFile.commit("initial commit");
            assertTrue(GitFileSystemObject.Status.IN_REPOSITORY.equals(gitFile.getStatus()));
            System.out.println("File "+gitFile.getRelativePath()+" is in repository");
            // modified
            FileWriter writer = new FileWriter(gitFile.getFile());
            writer.write("blub\n");
            writer.close();
            assertTrue(GitFileSystemObject.Status.MODIFIED.equals(gitFile.getStatus()));
            System.out.println("File "+gitFile.getRelativePath()+" is modified");
            gitFile.commit("second version");
            // deleted
            gitFile.rm();
            assertTrue(GitFileSystemObject.Status.DELETED.equals(gitFile.getStatus()));
            gitFile.commit("delete bla");
            System.out.println("File "+gitFile.getRelativePath()+" is deleted");
        } catch (JavaGitException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getFooFile() {
        return new File(tempGitDir, DIR_STRUCTURE + File.separator + "foo");
    }

    @Test
    public void showFoo() {
        final String contents = "blub\r\nbla";
        DotGit dotGit = obtainDotGit(tempGitDir);
        assertTrue(dotGit != null);
        try {
            // create, add, and commit file
            GitFile gitFile = dotGit.getWorkingTree().getFile(getFooFile());
            assertTrue(GitFileSystemObject.Status.UNTRACKED.equals(gitFile.getStatus()));
            System.out.println("File "+gitFile.getRelativePath()+" is untracked");
            // added
            GitAddResponse addResponse = gitAdd.add(gitFile.getWorkingTree().getPath(),
                    new GitAddOptions(false, false, false, false, false, true),
                    gitFile.getRelativePath());
            assertTrue(GitFileSystemObject.Status.ADDED.equals(gitFile.getStatus()));
            System.out.println("File "+gitFile.getRelativePath()+" is added with response: "+addResponse.getAddedFiles());
            // in repository
            gitFile.commit("initial commit");
            assertTrue(GitFileSystemObject.Status.IN_REPOSITORY.equals(gitFile.getStatus()));
            System.out.println("File "+gitFile.getRelativePath()+" is in repository");
            // modified and second commit
            FileWriter writer = new FileWriter(gitFile.getFile());
            writer.write(contents);
            writer.close();
            assertTrue(GitFileSystemObject.Status.MODIFIED.equals(gitFile.getStatus()));
            System.out.println("File "+gitFile.getRelativePath()+" is modified");
            GitCommitResponse commitResponse = gitFile.commit("second version");
            assertTrue(GitFileSystemObject.Status.IN_REPOSITORY.equals(gitFile.getStatus()));
            System.out.println("File "+gitFile.getRelativePath()+" is in repository (2nd version)");
            // obtain contents from last commit:
            String obtainedContents = Factory.createGitShow().show(gitFile.getWorkingTree().getPath(), null,
                    commitResponse.getShortSHA1()+":"+gitFile.getRelativePath());
            System.out.println("Obtained contents:\n"+obtainedContents);
            assertTrue(contents.equals(obtainedContents));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JavaGitException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void remotes() {
        final String remoteName = "localRepo";
        final String url = "../" + tempRemoteDir.getName();
        DotGit dotGit = obtainDotGit(tempGitDir);
        assertTrue(dotGit != null);
        try {
            // define remote
            Set<GitRemoteResponse.Remote> remotes;
            remotes = gitRemote.remote(dotGit.getWorkingTree().getPath());
            assertTrue(remotes.isEmpty());
            System.out.println("Remotes are empty.");
            int code;
            code = gitRemote.add(dotGit.getWorkingTree().getPath(), null, remoteName, url);
            assertTrue(code == 0);
            System.out.println("Added new remote.");
            code = gitRemote.add(dotGit.getWorkingTree().getPath(), null, remoteName, url);
            assertTrue(code != 0);
            System.out.println("Error when adding existing remote.");            
            remotes = gitRemote.remote(dotGit.getWorkingTree().getPath());
            assertTrue(!remotes.isEmpty());
            System.out.println("Remotes are not empty anymore.");
            code = gitRemote.rm(dotGit.getWorkingTree().getPath(), null, remoteName);
            assertTrue(code == 0);
            System.out.println("Removed existing remote.");
            code = gitRemote.rm(dotGit.getWorkingTree().getPath(), null, remoteName);
            assertTrue(code != 0);
            System.out.println("Error when removing non-existing remote.");

            // push to remote
            AbstractResponse response = Factory.createGitPush().master(dotGit.getWorkingTree().getPath(),
                    null, 
                    remoteName);
//            assertTrue(response.getExitCode() == 0);
            System.out.println("Pushed to remote:\n" + response.getOutput());

        } catch (JavaGitException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void path() {
        try {
            JavaGitConfiguration.setGitPath("/usr/local/git/bin");
            System.out.println("Git version: "+ JavaGitConfiguration.getGitVersion());
            assertTrue("Was able to set GIT path", true);
            JavaGitConfiguration.setGitPath((File) null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JavaGitException e) {
            e.printStackTrace();
        }
    }
}
