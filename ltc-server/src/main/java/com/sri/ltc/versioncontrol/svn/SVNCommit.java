package com.sri.ltc.versioncontrol.svn;

import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.Commit;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SVNCommit extends Commit<SVNRepository, SVNTrackedFile> {
    private SVNLogEntry logEntry;
    private List<Commit> parents = new ArrayList<Commit>();

    public SVNCommit(SVNRepository repository, SVNTrackedFile trackedFile, SVNLogEntry logEntry) {
        super(repository, trackedFile);
        this.logEntry = logEntry;
    }

    @Override
    public String getId() {
        return Long.toString(logEntry.getRevision());
    }

    @Override
    public String getMessage() {
        return logEntry.getMessage();
    }

    @Override
    public Author getAuthor() {
        return new Author(logEntry.getAuthor(), null, null);
    }

    @Override
    public Date getDate() {
        return logEntry.getDate();
    }

    @Override
    public List<Commit> getParents() throws Exception {
        return parents;
    }

    @Override
    public InputStream getContentStream() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        getRepository().getClientManager().getWCClient()
                .doGetFileContents(
                        trackedFile.getFile(),
                        SVNRevision.create(logEntry.getRevision()), SVNRevision.create(logEntry.getRevision()),
                        true,
                        outputStream
                        );

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    public void setParent(SVNCommit parent) {
        parents.clear();
        parents.add(parent);
    }

    public SVNLogEntry getLogEntry() {
        return logEntry;
    }
}
