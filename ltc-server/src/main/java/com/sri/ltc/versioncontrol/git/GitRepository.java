package com.sri.ltc.versioncontrol.git;

import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.Repository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.revwalk.DepthWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GitRepository implements Repository {
    private org.eclipse.jgit.lib.Repository repository = null;
    
    @Override
    public void initialize(File localPath) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        repository = builder
                .setWorkTree(localPath)
                .readEnvironment()
                .build();
    }

    @Override
    public void create() throws IOException {
        repository.create();
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
        RevCommit revCommit = git.commit().setMessage(message).call();
        return new GitCommit(revCommit);
    }

    @Override
    public List<Commit> getCommits() throws Exception {
        List<Commit> commitsList = new ArrayList<Commit>();
        
        Git git = new Git(repository);
        Iterable<RevCommit> log = git.log().call();
        for (RevCommit revCommit : log) {
            commitsList.add(new GitCommit(revCommit));
        }

        return commitsList;
    }

    @Override
    public List<URI> getRemoteRepositories() throws Exception {
        return null;
    }

    @Override
    public Author getSelf() {
        String name = repository.getConfig().getString("user", null, "name");
        String email = repository.getConfig().getString("user", null, "email");

        return new Author(name, email, null);
    }
}
