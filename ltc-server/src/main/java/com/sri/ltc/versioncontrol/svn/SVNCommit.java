package com.sri.ltc.versioncontrol.svn;

import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.git.GitRepository;
import com.sri.ltc.versioncontrol.git.GitTrackedFile;
import org.eclipse.jgit.revwalk.RevCommit;
import org.tmatesoft.svn.core.SVNLogEntry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

public class SVNCommit extends Commit<SVNRepository> {
    private SVNLogEntry logEntry;

    public SVNCommit(SVNTrackedFile trackedFile, SVNLogEntry logEntry) {
        super(trackedFile.getRepository());
        this.logEntry = logEntry;
//        this.trackedFile = trackedFile;
    }

    @Override
    public String getId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getMessage() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Author getAuthor() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Date getDate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Commit<SVNRepository>> getParents() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public InputStream getContentStream() throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
