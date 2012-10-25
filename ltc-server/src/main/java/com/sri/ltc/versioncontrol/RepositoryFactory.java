package com.sri.ltc.versioncontrol;

import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.git.GitRepository;
import com.sri.ltc.versioncontrol.svn.SVNRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class RepositoryFactory {
    public static Repository fromPath(File path) throws Exception {
        File testPath = new File(path.getParent(), ".svn");
        if (testPath.exists()) {
            return new SVNRepository(path);
        }

        return new GitRepository(path);
    }
}
