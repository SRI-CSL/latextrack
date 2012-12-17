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
    private Author defaultAuthor;
    private Author currentAuthor;
    private File initialPath;
    private SVNClientManager clientManager = null;

    public SVNRepository(File initialPath) throws Exception {
        clientManager = SVNClientManager.newInstance();
        this.initialPath = initialPath;

        defaultAuthor = new Author(System.getProperty("user.name"), null, null);
        currentAuthor = defaultAuthor;
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
    public File getBundle(File outputDirectory) throws IOException {
        // TODO: decide here what to do
        return null;
    }

    @Override
    public Author getSelf() {
        return currentAuthor;
    }

    @Override
    public void setSelf(Author author) {
        currentAuthor = author;
    }

    @Override
    public void resetSelf() {
        currentAuthor = defaultAuthor;
    }

    public String getURL() throws SVNException {
        SVNInfo info = clientManager.getWCClient().doInfo(initialPath, SVNRevision.create(-1));
        return info.getURL().getPath();
    }
}