package com.sri.ltc.git;

import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.TrackedFile;
import com.sri.ltc.versioncontrol.git.GitRepository;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

    public TrackedFile createTestFileInRepository(String prefix, String suffix, String contents, boolean add)
            throws Exception {
        if (repository == null)
            throw new RuntimeException("Repository is not initialized");

        File file = File.createTempFile(prefix, suffix, getRoot());
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.append(contents);
        fileWriter.flush();
        fileWriter.close();

        if (add) {
            getRepository().addFile(file);
        }

        return getRepository().getFile(file);
    }

    public void modifyTestFileInRepository(TrackedFile file, String text, boolean append) throws IOException {
        if (repository == null)
            throw new RuntimeException("Repository is not initialized");

        // check that file is in our repository
        if (!repository.equals(file.getRepository()))
            throw new RuntimeException("given file \""+file+"\" is not tracked in this repository");

        // append text
        FileWriter fileWriter = new FileWriter(file.getFile(), append);
        fileWriter.append(text);
        fileWriter.flush();
        fileWriter.close();
    }
}
