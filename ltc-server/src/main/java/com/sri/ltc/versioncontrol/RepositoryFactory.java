package com.sri.ltc.versioncontrol;

import com.sri.ltc.versioncontrol.git.GitRepository;
import com.sri.ltc.versioncontrol.svn.SVNRepository;

import java.io.File;
import java.io.FilenameFilter;

public class RepositoryFactory {

    private final static FilenameFilter gitFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            return ".git".equals(s);
        }
    };

    public static Repository fromPath(File path) throws Exception {
        File testPath;

        testPath = new File(path.getParent(), ".svn");
        if (testPath.exists())
            return new SVNRepository(path);

        // walk up the parent dirs and look for .git directory
        testPath = path.getParentFile();
        while (testPath != null && testPath.isDirectory()) {
            if (testPath.listFiles(gitFilter).length == 1)
                return new GitRepository(path);
            else
                testPath = testPath.getParentFile();
        }

        throw new RuntimeException("Could not create repository from given file "+path);
    }
}
