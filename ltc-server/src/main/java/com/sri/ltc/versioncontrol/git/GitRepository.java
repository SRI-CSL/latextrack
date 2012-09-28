package com.sri.ltc.versioncontrol.git;

import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.Remotes;
import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.TrackedFile;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GitRepository implements Repository {
    private final static Logger LOGGER = Logger.getLogger(GitRepository.class.getName());
    private org.eclipse.jgit.lib.Repository repository = null;

    public GitRepository(File localPath) throws IOException {
        this(localPath, false);
    }
    
    public GitRepository(File localPath, boolean create) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        if (create) {
            builder.setWorkTree(localPath);
        } else {
            builder.findGitDir(localPath);
        }

        builder.readEnvironment();

        repository = builder.build();

        if (create) {
            repository.create();
        }
    }

    @Override
    public void addFile(File file) throws Exception {
        Git git = new Git(repository);
        git.add().addFilepattern(file.getName()).call();
    }

    @Override
    public Commit commit(String message) throws Exception {
        Git git = new Git(repository);
        // note: -must- assign the result to a variable or else nothing seems to actually happen
        // todo: we are automatically staging changes and deletes here - that may not be desirable!
        RevCommit revCommit = git.commit().setAll(true).setMessage(message).call();
        return new GitCommit(this, null, revCommit);
    }

    public org.eclipse.jgit.lib.Repository getWrappedRepository() {
        return repository;
    }

    @Override
    public TrackedFile getFile(File file) throws IOException {
        return new GitTrackedFile(this, file);
    }

    @Override
    public Remotes getRemotes() {
        return new GitRemotes(this);
    }

    @Override
    public Author getSelf() {
        String name = repository.getConfig().getString("user", null, "name");
        String email = repository.getConfig().getString("user", null, "email");

        return new Author(name, email, null);
    }

    @Override
    public void setSelf(Author author) {
        // TODO: Disabling this code since it has not yet been tested!
//        repository.getConfig().setString("user", null, "name", author.name);
//        repository.getConfig().setString("user", null, "email", author.email);
//        LOGGER.fine("Set current author to \""+author.name+"\"");
    }

    @Override
    public void resetSelf() {
        // TODO: Disabling this code since it has not yet been tested!
//        repository.getConfig().unset("user", null, "name");
//        repository.getConfig().unset("user", null, "email");
//        LOGGER.fine("Reset current author");
    }
}
