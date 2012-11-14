package com.sri.ltc.git;

import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.RepositoryFactory;
import com.sri.ltc.versioncontrol.TrackedFile;
import com.sri.ltc.versioncontrol.VersionControlException;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class TestGitRepository {
    @ClassRule
    public static TemporaryGitRepository temporaryGitRepository = new TemporaryGitRepository();

    @Rule
    public TemporaryGitRepository toBeRemoved = new TemporaryGitRepository();

    @Test
    public void testUntracked() {
        assertTrue(temporaryGitRepository.getRoot().exists());

        try {
            TrackedFile trackedFile = temporaryGitRepository.createTestFileInRepository("foo", ".txt", "testUntracked", false);
            assertTrue(trackedFile.getStatus() == TrackedFile.Status.NotTracked);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testAddAndCommit() {
        assertTrue(temporaryGitRepository.getRoot().exists());

        try {
            TrackedFile trackedFile = temporaryGitRepository.createTestFileInRepository("foo", ".txt", "testAddAndCommit", true);
            assertTrue(trackedFile.getStatus() == TrackedFile.Status.Added);

            trackedFile.commit("commit from testAddAndCommit");
            assertTrue(trackedFile.getStatus() == TrackedFile.Status.Unchanged);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testAddCommitAndModify() {
        assertTrue(temporaryGitRepository.getRoot().exists());

        TrackedFile.Status status;

        try {
            System.out.println("Working with repository in " + temporaryGitRepository.getRoot().getPath());

            TrackedFile trackedFile = temporaryGitRepository.createTestFileInRepository("foo", ".txt", "testAddCommitAndModify", true);
            status = trackedFile.getStatus();
            assertTrue(status == TrackedFile.Status.Added);

            trackedFile.commit("commit A from testAddCommitAndModify");

            status = trackedFile.getStatus();
            assertTrue(status == TrackedFile.Status.Unchanged);

            {
                // create a different file, so we can verify that we get commits only for the file we do care about
                TrackedFile garbageFile = temporaryGitRepository.createTestFileInRepository("garbage", ".txt", "testAddCommitAndModify - not the file we care about", true);
                garbageFile.commit("commit garbage file from testAddCommitAndModify");
            }

            System.out.println("Getting commits for " + trackedFile.getFile().getPath());
            List<Commit> commits;
            commits = trackedFile.getCommits();
            assertTrue(commits.size() == 1);
            assertTrue(commits.get(0).getMessage().equals("commit A from testAddCommitAndModify"));

            temporaryGitRepository.modifyTestFileInRepository(trackedFile, "modification", true);
            status = trackedFile.getStatus();
            assertTrue(status == TrackedFile.Status.Modified);

            trackedFile.commit("commit B from testAddCommitAndModify");

            System.out.println("Getting commits (after modification) for " + trackedFile.getFile().getPath());
            commits = trackedFile.getCommits();
            assertTrue(commits.size() == 2);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testMultifileCommit() {
        // the idea here is to make sure that when more than one file is modified at a time,
        // and we commit, we commit just the file we mean to.
        assertTrue(temporaryGitRepository.getRoot().exists());

        try {
            System.out.println("Working with repository in " + temporaryGitRepository.getRoot().getPath());

            TrackedFile trackedFile0 = temporaryGitRepository.createTestFileInRepository("file0", ".txt", "file0 contents", true);
            TrackedFile trackedFile1 = temporaryGitRepository.createTestFileInRepository("file1", ".txt", "file1 contents", true);
            TrackedFile trackedFile2 = temporaryGitRepository.createTestFileInRepository("file2", ".txt", "file2 contents", true);

            trackedFile1.commit("Three file modify, one file commit from testMultifileCommit");

            {
                List<Commit> commits = trackedFile0.getCommits();
                assertTrue(commits.size() == 0);
            }

            {
                List<Commit> commits = trackedFile1.getCommits();
                Commit file1Commmit = commits.get(0);

                BufferedReader reader = new BufferedReader(file1Commmit.getContents());
                String line = reader.readLine();
                assertTrue(line.equals("file1 contents"));
            }

            {
                List<Commit> commits = trackedFile2.getCommits();
                assertTrue(commits.size() == 0);
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test(expected = NullPointerException.class)
    public void badThingsWithRepo() throws VersionControlException, IOException {
        // do bad things to repo such as removing .git or the whole tree...

        assertTrue(toBeRemoved.getRoot().exists());
        TrackedFile trackedFile = null;
        List<Commit> commits = null;

        try {
            // commit a few revisions
            trackedFile = toBeRemoved.createTestFileInRepository("foo", ".txt", "first version of file", true);
            assertEquals("tracked file is added", TrackedFile.Status.Added, trackedFile.getStatus());
            trackedFile.commit("commit A from badThingsWithRepo");
            toBeRemoved.modifyTestFileInRepository(trackedFile, "\n more text into file", true);
            assertEquals("tracked file is modified", TrackedFile.Status.Modified, trackedFile.getStatus());
            trackedFile.commit("commit B from badThingsWithRepo");

            // getting commits works
            commits = trackedFile.getCommits();
            assertEquals("2 commits", 2, commits.size());

            // checking out file structure
            assertTrue("root is directory", toBeRemoved.getRoot().isDirectory());
            File[] gitDir = toBeRemoved.getRoot().listFiles(RepositoryFactory.GIT_FILTER);
            assertTrue(".git exists", gitDir != null);
            assertEquals("only 1 .git exists", 1, gitDir.length);

            // now doing bad things...
            assertTrue(".git is directory", gitDir[0].isDirectory());
            deleteFolder(gitDir[0]);
            assertTrue("second deletion doesn't work", !gitDir[0].delete());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assert trackedFile != null;
        commits = trackedFile.getCommits();
    }

    private static void deleteFolder(File folder) {
        if (!folder.isDirectory())
            return;
        File[] files = folder.listFiles();
        if (files!=null)  //some JVMs return null for empty dirs
            for (File f: files) {
                if (f.isDirectory())
                    deleteFolder(f);
                else
                    f.delete();
            }
        folder.delete();
    }
}
