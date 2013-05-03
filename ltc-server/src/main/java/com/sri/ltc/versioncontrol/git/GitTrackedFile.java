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

import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.TrackedFile;
import com.sri.ltc.versioncontrol.VersionControlException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GitTrackedFile extends TrackedFile<GitRepository> {
    public GitTrackedFile(GitRepository repository, File file) {
        super(repository, file);
    }

    @Override
    public Commit commit(String message) throws Exception {
        Git git = new Git(getRepository().getWrappedRepository());
        RevCommit revCommit = git.commit().setOnly(getRepositoryRelativeFilePath()).setMessage(message).call();
        return new GitCommit(getRepository(), this, revCommit);
    }

    @Override
    public List<Commit> getCommits() throws IOException, VersionControlException {
        return getCommits(null, null);
    }

    @Override
    public List<Commit> getCommits(@Nullable Date inclusiveLimitDate, @Nullable String inclusiveLimitRevision) throws IOException, VersionControlException {
        // note: we could use the simpler LogCommand with add + addPath

        List<Commit> commits = new ArrayList<Commit>();

        Repository wrappedRepository = getRepository().getWrappedRepository();
        RevWalk revWalk = new RevWalk(wrappedRepository);
        revWalk.setTreeFilter(
                AndTreeFilter.create(
                        PathFilterGroup.createFromStrings(getRepositoryRelativeFilePath()),
                        TreeFilter.ANY_DIFF)
        );

        try {
            RevCommit rootCommit = revWalk.parseCommit(wrappedRepository.resolve(Constants.HEAD));
            revWalk.sort(RevSort.COMMIT_TIME_DESC);
            revWalk.markStart(rootCommit);
        } catch (IncorrectObjectTypeException e) {
            throw new VersionControlException(e);
        } catch (AmbiguousObjectException e) {
            throw new VersionControlException(e);
        } catch (MissingObjectException e) {
            throw new VersionControlException(e);
        }

        RevCommit limitRevCommit = null;
        if (inclusiveLimitRevision != null)
            limitRevCommit = revWalk.parseCommit(wrappedRepository.resolve(inclusiveLimitRevision));

        if (inclusiveLimitDate == null)
            inclusiveLimitDate = new Date(0);

        for (RevCommit revCommit : revWalk) {

            // test limiting date first before adding commit
            if (inclusiveLimitDate.compareTo(GitCommit.CommitDate(revCommit)) > 0) break;

            commits.add(new GitCommit(getRepository(), this, revCommit));

            // now test if this was the last commit we wanted
            if (revCommit.getId().equals(limitRevCommit)) break;

//            TreeWalk treeWalk = TreeWalk.forPath(wrappedRepository, getRepositoryRelativeFilePath(), revCommit.getTree());
//            if (treeWalk != null) {
//                treeWalk.setRecursive(true);
//                treeWalk.setFilter(
//                        AndTreeFilter.create(
//                                PathFilterGroup.createFromStrings(getRepositoryRelativeFilePath()), TreeFilter.ANY_DIFF)
//                );
//
//                CanonicalTreeParser canonicalTreeParser = treeWalk.getTree(0, CanonicalTreeParser.class);
//
//                while (!canonicalTreeParser.eof()) {
//                    System.out.println("- found entry: " + canonicalTreeParser.getEntryPathString());
//                    canonicalTreeParser.next(1);
//                }
//            }
        }

        return commits;
    }

    @Override
    public Status getStatus() throws VersionControlException {
        org.eclipse.jgit.lib.Repository wrappedRepository = getRepository().getWrappedRepository();

        try {
            IndexDiff diff = new IndexDiff(wrappedRepository, Constants.HEAD, new FileTreeIterator(wrappedRepository));
            diff.setFilter(PathFilter.create(getRepositoryRelativeFilePath()));
            diff.diff();

            if (!diff.getAdded().isEmpty())
                return Status.Added;
            if (!diff.getChanged().isEmpty())
                return Status.Changed;
            if (!diff.getConflicting().isEmpty())
                return Status.Conflicting;
            if (!diff.getIgnoredNotInIndex().isEmpty())
                return Status.Ignored;
            if (!diff.getMissing().isEmpty())
                return Status.Missing;
            if (!diff.getModified().isEmpty())
                return Status.Modified;
            if (!diff.getRemoved().isEmpty())
                return Status.Removed;
            if (!diff.getUntracked().isEmpty())
                return Status.NotTracked;
        } catch (IOException e) {
            throw new VersionControlException(e);
        }

        // well, if it's none of these things, then it must be in the index and happy
        return Status.Unchanged;
    }

    public String getRepositoryRelativeFilePath() {
        String basePath = getRepository().getWrappedRepository().getWorkTree().getPath();
        return new File(basePath).toURI().relativize(getFile().toURI()).getPath();
    }
}
