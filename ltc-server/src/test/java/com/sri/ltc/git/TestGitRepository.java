package com.sri.ltc.git;

import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.TrackedFile;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestGitRepository {
    @ClassRule
    public static TemporaryGitRepository temporaryGitRepository = new TemporaryGitRepository();

    @Test
    public void testUntracked() {
        assertTrue(temporaryGitRepository.getRoot().exists());

        try {
            TrackedFile trackedFile = createTestFileInRepository("foo", ".txt", "testUntracked", false);
            assertTrue(trackedFile.getStatus() == TrackedFile.Status.NotTracked);
        } catch (Exception e) {
            assertFalse(e.getMessage(), false);
        }
    }

    @Test
    public void testAddAndCommit() {
        Repository repository = temporaryGitRepository.getRepository();
        assertTrue(temporaryGitRepository.getRoot().exists());

        try {
            TrackedFile trackedFile = createTestFileInRepository("foo", ".txt", "testAddAndCommit", true);
            assertTrue(trackedFile.getStatus() == TrackedFile.Status.Added);
            
            repository.commit("commit from testAddAndCommit");
            assertTrue(trackedFile.getStatus() == TrackedFile.Status.Unchanged);
        } catch (Exception e) {
            assertFalse(e.getMessage(), false);
        }
    }
    
    @Test
    public void testAddCommitAndModify() {
        assertTrue(temporaryGitRepository.getRoot().exists());
        Repository repository = temporaryGitRepository.getRepository();

        TrackedFile.Status status;

        try {
            System.out.println("Working with repository in " + temporaryGitRepository.getRoot().getPath());

            TrackedFile trackedFile = createTestFileInRepository("foo", ".txt", "testAddCommitAndModify", true);
            status = trackedFile.getStatus();
            assertTrue(status == TrackedFile.Status.Added);

            repository.commit("commit A from testAddCommitAndModify");

            status = trackedFile.getStatus();
            assertTrue(status == TrackedFile.Status.Unchanged);

            // create a different file, so we can verify that we get commits only for the file we do care about
            createTestFileInRepository("garbage", ".txt", "testAddCommitAndModify - not the file we care about", true);

            repository.commit("commit garbage file from testAddCommitAndModify");

            System.out.println("Getting commits for " + trackedFile.getFile().getPath());
            List<Commit> commits;
            commits = trackedFile.getCommits();
            assertTrue(commits.size() == 1);
            assertTrue(commits.get(0).getMessage().equals("commit A from testAddCommitAndModify"));

            commits = repository.getCommits();
            assertTrue(commits.size() >= 2); // we have more than two commits, since other tests may have run first

            FileWriter fileWriter = new FileWriter(trackedFile.getFile());
            fileWriter.append("modification");
            fileWriter.flush();
            fileWriter.close();
            status = trackedFile.getStatus();
            assertTrue(status == TrackedFile.Status.Modified);
            //repository.addFile(trackedFile.getFile()); // NOTE: need to add the file again to get it into the commit

            repository.commit("commit B from testAddCommitAndModify");

            System.out.println("Getting commits (after modification) for " + trackedFile.getFile().getPath());
            commits = trackedFile.getCommits();
            assertTrue(commits.size() == 2);

            commits = repository.getCommits();
            assertTrue(commits.size() >= 3); // we have more than three commits, since other tests may have run first

        } catch (Exception e) {
            assertFalse(e.getMessage(), false);
        }
    }

    private TrackedFile createTestFileInRepository(String prefix, String suffix, String contents, boolean add) throws Exception {
        File file = File.createTempFile(prefix, suffix, temporaryGitRepository.getRoot());
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.append(contents);
        fileWriter.flush();
        fileWriter.close();

        if (add) {
            temporaryGitRepository.getRepository().addFile(file);
        }

        return temporaryGitRepository.getRepository().getFile(file);
    }
}
