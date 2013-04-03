/*
 * #%L
 * LaTeX Track Changes (LTC) allows collaborators on a version-controlled LaTeX writing project to view and query changes in the .tex documents.
 * %%
 * Copyright (C) 2009 - 2012 SRI International
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package com.sri.ltc.versioncontrol.svn;

import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.VersionControlException;
import org.tmatesoft.svn.core.SVNException;
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
        return new Author(logEntry.getAuthor(), null);
    }

    @Override
    public Date getDate() {
        return logEntry.getDate();
    }

    @Override
    public List<Commit> getParents() {
        return parents;
    }

    @Override
    public InputStream getContentStream() throws VersionControlException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            getRepository().getClientManager().getWCClient()
                    .doGetFileContents(
                            trackedFile.getFile(),
                            SVNRevision.create(logEntry.getRevision()), SVNRevision.create(logEntry.getRevision()),
                            true,
                            outputStream
                    );
        } catch (SVNException e) {
            throw new VersionControlException(e);
        }

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
