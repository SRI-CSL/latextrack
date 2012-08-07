package com.sri.ltc.versioncontrol;

import com.sri.ltc.filter.Author;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

public interface Repository {
    public void addFile(File file) throws Exception;
    public Commit commit(String message) throws Exception;
    public List<Commit> getCommits() throws Exception;
    List<URI> getRemoteRepositories() throws Exception;

    // TODO: could push these into a separate interface, but probably not needed
    public Author getSelf();
    public void setSelf(Author author);
    public void resetSelf();
    public InputStream getContentStream(Commit commit) throws IOException;

    // TODO how to handle push/pull for svn? could map pull to update
    void push(URI remote);
    void pull(URI remote);
}
