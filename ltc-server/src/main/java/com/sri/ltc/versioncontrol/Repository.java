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

    public TrackedFile getFile(File file) throws IOException;

    public Remotes getRemotes();
    
    // TODO: could push these into a separate interface, but probably not needed
    public Author getSelf();
    public void setSelf(Author author);
    public void resetSelf();
}
