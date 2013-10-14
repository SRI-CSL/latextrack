package com.sri.ltc.git;

import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.TrackedFile;
import com.sri.ltc.versioncontrol.git.GitRepository;
import org.eclipse.jgit.api.Git;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * A cloned GIT repository in a temporary folder for tests against fixed dates and revisions.
 *
 * @author linda
 */
public class TemporaryClonedRepository extends TemporaryFolder {
    private Repository repository = null;

    public Repository getRepository() {
        return repository;
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        assert this.getRoot().exists();
        System.out.println("Cloning git repo to directory " + this.getRoot().toString());

        // cloning from resource file:
        assertNotNull("Repo to clone from is missing", getClass().getResource("/independence.bundle"));
        Git.cloneRepository()
                .setURI(getClass().getResource("/independence.bundle").toURI().toString())
                .setDirectory(this.getRoot())
                .call();
        File testGitDir = new File(this.getRoot().toString() + File.separatorChar + ".git");
        assertTrue(testGitDir.exists());

        repository = new GitRepository(new File(this.getRoot().toString()), false);
    }

    public TrackedFile getTrackedFile() throws IOException {
        if (repository == null)
            throw new RuntimeException("Repository is not initialized");
        return repository.getFile(new File(getRoot() + "/independence.tex"));
    }
}
