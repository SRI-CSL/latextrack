package com.sri.ltc.git;

import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.TrackedFile;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.BufferedReader;
import java.util.List;

import static org.junit.Assert.*;

public class TestGitRepository {
    @ClassRule
    public static TemporaryGitRepository temporaryGitRepository = new TemporaryGitRepository();

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

}
