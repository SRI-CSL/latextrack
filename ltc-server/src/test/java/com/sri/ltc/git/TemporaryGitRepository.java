package com.sri.ltc.git;

import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.git.GitRepository;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class TemporaryGitRepository extends TemporaryFolder {
    private Repository repository = null;

    public Repository getRepository() {
        return repository;
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        assert this.getRoot().exists();

        System.out.println("Creating a git repo at " + this.getRoot().toString());
        repository = new GitRepository(new File(this.getRoot().toString()), true);

        File testGitDir = new File(this.getRoot().toString() + File.separatorChar + ".git");
        assertTrue(testGitDir.exists());
    }
}
