package com.sri.ltc;

import com.sri.ltc.versioncontrol.RepositoryFactory;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

/**
 * Some generic tests about the repository factory.
 * @author linda
 */
public class TestRepository {

    @ClassRule
    static public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test(expected = IllegalArgumentException.class)
    public void testNullPath() throws Exception {
        RepositoryFactory.fromPath(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonExisting() throws Exception {
        RepositoryFactory.fromPath(new File("/I/really/hope/this/file/does/not/exist"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotFile() throws Exception {
        RepositoryFactory.fromPath(temporaryFolder.getRoot());
    }

    @Test(expected = RuntimeException.class)
    public void testNotUnderVC() throws Exception {
        // create a new file, then try to create a repo from it:
        RepositoryFactory.fromPath(temporaryFolder.newFile("myFile.txt"));
    }
}
