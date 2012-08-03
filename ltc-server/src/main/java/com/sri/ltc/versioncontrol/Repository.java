package com.sri.ltc.versioncontrol;

import com.sri.ltc.filter.Author;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.List;

public interface Repository {
    public void initialize(File localPath) throws IOException;
    public void create() throws IOException;

    public void addFile(File file) throws Exception;
    public Commit commit(String message) throws Exception;
    public List<Commit> getCommits() throws Exception;
    List<URI> getRemoteRepositories() throws Exception;

    public Author getSelf();

    // TODO how to handle push/pull for svn? could map pull to update
}
