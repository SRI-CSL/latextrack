package com.sri.ltc.git;

import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.TrackedFile;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestGitRepository {
    @ClassRule
    public static TemporaryGitRepository temporaryGitRepository = new TemporaryGitRepository();

    @Test
    public void testUntracked() {
        Repository repository = temporaryGitRepository.getRepository();

        try {
            assertTrue(temporaryGitRepository.getRoot().exists());
            File file = File.createTempFile("foo", ".txt", temporaryGitRepository.getRoot());
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.append("testing");
            fileWriter.flush();
            fileWriter.close();

            TrackedFile.Status status;

            // file does not exist in repo
            TrackedFile trackedFile = repository.getFile(file);
            status = trackedFile.getStatus();
            assertTrue(status == TrackedFile.Status.NotTracked);
        } catch (Exception e) {
            assertFalse(e.getMessage(), false);
        }
    }

    @Test
    public void testAddAndCommit() {
        Repository repository = temporaryGitRepository.getRepository();

        try {
            assertTrue(temporaryGitRepository.getRoot().exists());
            File file = File.createTempFile("foo", ".txt", temporaryGitRepository.getRoot());
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.append("testing");
            fileWriter.flush();
            fileWriter.close();

            TrackedFile.Status status;

            repository.addFile(file);
            TrackedFile trackedFile = repository.getFile(file);
            status = trackedFile.getStatus();
            assertTrue(status == TrackedFile.Status.Added);
            
            repository.commit("Test commit");
            status = trackedFile.getStatus();
            assertTrue(status == TrackedFile.Status.Unchanged);
        } catch (Exception e) {
            assertFalse(e.getMessage(), false);
        }
    }
    
    @Test
    public void testAddCommitAndModify() {
        assertTrue(temporaryGitRepository.getRoot().exists());
        Repository repository = temporaryGitRepository.getRepository();

        try {
            File file = File.createTempFile("foo", ".txt", temporaryGitRepository.getRoot());
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.append("testing");
            fileWriter.flush();
            fileWriter.close();

            TrackedFile.Status status;

            repository.addFile(file);
            TrackedFile trackedFile = repository.getFile(file);
            status = trackedFile.getStatus();
            assertTrue(status == TrackedFile.Status.Added);

            repository.commit("Test commit");
            status = trackedFile.getStatus();
            assertTrue(status == TrackedFile.Status.Unchanged);

            fileWriter = new FileWriter(file);
            fileWriter.append("modification");
            fileWriter.flush();
            fileWriter.close();
            status = trackedFile.getStatus();
            assertTrue(status == TrackedFile.Status.Modified);
        } catch (Exception e) {
            assertFalse(e.getMessage(), false);
        }
    }
}
