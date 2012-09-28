package com.sri.ltc.versioncontrol.svn;

import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.Remotes;
import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.TrackedFile;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;

public class SVNRepository implements Repository {
    private SVNClientManager clientManager = null;

    public SVNRepository(File localPath) throws Exception {
        clientManager = SVNClientManager.newInstance();
    }

    public SVNClientManager getClientManager() {
        return clientManager;
    }

    @Override
    public void addFile(File file) throws Exception {
        // TODO:
        throw new NotImplementedException();
    }

    @Override
    public TrackedFile getFile(File file) throws IOException {
        return new SVNTrackedFile(this, file);
    }

    @Override
    public Remotes getRemotes() {
        return new SVNRemotes(this);
    }

    @Override
    public Author getSelf() {
        // TODO:
        return new Author("<default>", null, null);
    }

    @Override
    public void setSelf(Author author) {
        // TODO:
        throw new NotImplementedException();
    }

    @Override
    public void resetSelf() {
        // TODO:
        throw new NotImplementedException();
    }
}