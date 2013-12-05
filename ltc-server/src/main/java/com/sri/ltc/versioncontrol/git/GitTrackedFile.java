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
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.lib.ObjectId;
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
    public List<Commit> getCommits(@Nullable Date inclusiveLimitDate, @Nullable String inclusiveLimitRevision)
            throws IOException, VersionControlException {
        // note: we could use the simpler LogCommand with add + addPath

        List<Commit> commits = new ArrayList<Commit>();
        Repository wrappedRepository = getRepository().getWrappedRepository();

        try {

            // do we have a HEAD?
            ObjectId head = wrappedRepository.resolve(Constants.HEAD);
            if (head == null)
                return commits;

            RevWalk revWalk = new RevWalk(wrappedRepository);
            revWalk.setTreeFilter(
                    AndTreeFilter.create(
                            PathFilterGroup.createFromStrings(getRepositoryRelativeFilePath()),
                            TreeFilter.ANY_DIFF)
            );
            RevCommit rootCommit = revWalk.parseCommit(head);
            revWalk.sort(RevSort.COMMIT_TIME_DESC);
            revWalk.markStart(rootCommit);

            RevCommit limitRevCommit = null;

            if (inclusiveLimitRevision != null)
                try {
                    ObjectId limitId = wrappedRepository.resolve(inclusiveLimitRevision);
                    if (limitId == null)
                        throw new VersionControlException("Cannot resolve revision \""+inclusiveLimitRevision+"\"");
                    limitRevCommit = revWalk.parseCommit(limitId);
                } catch (RevisionSyntaxException e) {
                    throw new VersionControlException("Revision \""+inclusiveLimitRevision+"\" does not comply with standard syntax");
                }

            if (inclusiveLimitDate == null)
                inclusiveLimitDate = new Date(0);

            boolean stopOnNextItr = false;
            Date lastDateItr = null;
            for (RevCommit revCommit : revWalk) {

                // if lastDateItr is set, then we want to keep adding until the current commit date is
                // earlier than the last one. see explanation below.
                if (lastDateItr != null && lastDateItr.compareTo(GitCommit.CommitDate(revCommit)) > 0)
                    break;

                // need to add the current rev
                commits.add(new GitCommit(getRepository(), this, revCommit));

                // if we were told to stop last itr, then stop!
                if (stopOnNextItr)
                    break;

                // now test if this was the last commit we wanted, we need to include one more commit then stop.
                if (revCommit.getId().equals(limitRevCommit))
                    stopOnNextItr = true;


                // check to see if we are past the first date threshold, if yes we want to keep going until
                // the times after the crossing aren't equal...
                //      consider:  [ 10, 9, 8, 7'', 7', 7, 6'', 6', 6, 5, 4 ]
                // if threshold is 7, we need to include 10:6
                // if threshold is 8, we need to include 10:7
                // if threshold is 6, we need to include 10:5
                if (inclusiveLimitDate.compareTo(GitCommit.CommitDate(revCommit)) > 0) {
                    if (lastDateItr == null) {
                        lastDateItr =  GitCommit.CommitDate(revCommit);
                    }
                }
            }

        } catch (Exception e) {
            throw new VersionControlException(e);
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
