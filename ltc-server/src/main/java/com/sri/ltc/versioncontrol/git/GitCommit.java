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
package com.sri.ltc.versioncontrol.git;

import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.VersionControlException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GitCommit extends Commit<GitRepository, GitTrackedFile> {
    private RevCommit revCommit;

    public static Date CommitDate(RevCommit revCommit) {
        return new Date(revCommit.getCommitTime() * 1000L);
    }

    public GitCommit(GitRepository repository, GitTrackedFile trackedFile, RevCommit revCommit) {
        super(repository, trackedFile);
        this.revCommit = revCommit;
    }

    @Override
    public String getId() {
        return revCommit.getId().name();
    }

    @Override
    public String getMessage() {
        return revCommit.getFullMessage();
    }

    @Override
    public Author getAuthor() {
        return new Author(revCommit.getAuthorIdent().getName(), revCommit.getAuthorIdent().getEmailAddress(), null);
    }

    @Override
    public Date getDate() {
        return GitCommit.CommitDate(revCommit);
    }

    @Override
    public InputStream getContentStream() throws VersionControlException {
        if (trackedFile == null) return null;

        try {
            TreeWalk treeWalk = TreeWalk.forPath(getRepository().getWrappedRepository(), trackedFile.getRepositoryRelativeFilePath(), revCommit.getTree());
            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = getRepository().getWrappedRepository().open(objectId);

            return loader.openStream();
        } catch (IOException e) {
            throw new VersionControlException(e);
        }
    }

    @Override
    public List<Commit> getParents() {
        List<Commit> parents = new ArrayList<Commit>();
        for (RevCommit parentCommit : revCommit.getParents()) {
            parents.add(new GitCommit(repository, trackedFile, parentCommit));
        }

        return parents;
    }
}
