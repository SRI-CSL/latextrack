package com.sri.ltc.versioncontrol;

import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.git.GitRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class RepositoryFactory {
    public static Repository fromURI(URI uri) throws IOException {
        // TODO: look at URL and determine what kind of repository this is
        // or try to guess it. For now, we're assuming this is a Git Repo, and not
        // really treating this as a URI
        
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        GitRepository repository = new GitRepository();
        repository.initialize(new File(uri.getPath()));

        return repository;
    }
}
