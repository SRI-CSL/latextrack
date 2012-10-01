package com.sri.ltc.versioncontrol.svn;

import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.Remotes;
import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.TrackedFile;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;

public class SVNRepository implements Repository {
    private File initialPath;
    private SVNClientManager clientManager = null;

    public SVNRepository(File initialPath) throws Exception {
        clientManager = SVNClientManager.newInstance();
        this.initialPath = initialPath;
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

    public String getURL() throws SVNException {
        SVNInfo info = clientManager.getWCClient().doInfo(initialPath, SVNRevision.create(-1));
        return info.getURL().getPath();
    }
}