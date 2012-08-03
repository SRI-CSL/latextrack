package com.sri.ltc.jgit;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class TestJGit {
    @ClassRule
    public static TemporaryGitRepository temporaryGitRepository = new TemporaryGitRepository();

    @Test
    public void testAddAndCommit() throws IOException, GitAPIException, NoMessageException, ConcurrentRefUpdateException, WrongRepositoryStateException, UnmergedPathsException {
        assertTrue(temporaryGitRepository.getRoot().exists());
        File file = File.createTempFile("foo", ".txt", temporaryGitRepository.getRoot());
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.append("testing");
        fileWriter.flush();
        fileWriter.close();

//        System.out.println("Adding " + file.getName());
//        Git git = new Git(temporaryGitRepository.getRepository());
//        git.add().addFilepattern(file.getName()).call();
//
//        Status status = git.status().call();
//        assertTrue(status.getAdded().size() == 1);
//
//        // note: must assign the result to a variable or else nothing seems to actually happen
//        RevCommit testCommit = git.commit().setMessage("test commit").call();
//
//        int commitCount = 0;
//        for (RevCommit commit : git.log().addPath(file.getName()).call()) {
//            ++commitCount;
//        }
//        assertTrue(commitCount > 0);
    }

    @Test
    public void testModifyAndCommit() {
        assertTrue(temporaryGitRepository.getRoot().exists());
    }
}
