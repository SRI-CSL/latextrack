package com.sri.ltc.versioncontrol;

import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.git.GitRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class RepositoryFactory {
    public static Repository fromPath(File path) throws IOException {
        // TODO: look at path and determine what kind of repository this is
        // or try to guess it. For now, we're assuming this is a Git Repo
        
        return new GitRepository(path);
    }
}
