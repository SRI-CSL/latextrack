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

import com.google.common.collect.Sets;
import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.TrackedFile;
import com.sri.ltc.versioncontrol.VersionControlException;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

public class SVNTrackedFile extends TrackedFile<SVNRepository> {

    private class SVNLogEntryHandler implements ISVNLogEntryHandler {
        private SVNTrackedFile trackedFile = null;
        private List<Commit> commits = new ArrayList<Commit>();
        private Date inclusiveLimitDate;
        private Long inclusiveLimitRevision;

        public SVNLogEntryHandler(SVNTrackedFile trackedFile, @Nullable Date exclusiveLimitDate, @Nullable Long exclusiveLimitRevision) {
            this.trackedFile = trackedFile;
            this.inclusiveLimitDate = exclusiveLimitDate;
            this.inclusiveLimitRevision = exclusiveLimitRevision;
        }

        @Override
        public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
            if ((inclusiveLimitDate != null) && (inclusiveLimitDate.compareTo(logEntry.getDate()) > 0)) {
                return;
            }

            if ((inclusiveLimitRevision != null) && (inclusiveLimitRevision > logEntry.getRevision())) {
                return;
            }

            SVNCommit commit = new SVNCommit(trackedFile.getRepository(), trackedFile, logEntry);

            if (commits.size() > 0) {
                SVNCommit previous = (SVNCommit)commits.get(commits.size() - 1);
                previous.setParent(commit);
            }

            commits.add(commit);
        }

        public List<Commit> getCommits() {
            return commits;
        }
    }

    private static final Map<Integer, Status> svnStatus = new HashMap<Integer, Status>() {
        {
            put(SVNStatusType.INAPPLICABLE.getID(), Status.Unknown);
            put(SVNStatusType.UNKNOWN.getID(), Status.Unknown);
            put(SVNStatusType.OBSTRUCTED.getID(), Status.Unknown);

            put(SVNStatusType.UNCHANGED.getID(), Status.Unchanged);
            put(SVNStatusType.STATUS_NORMAL.getID(), Status.Unchanged);

            put(SVNStatusType.MISSING.getID(), Status.Missing);
            put(SVNStatusType.STATUS_MISSING.getID(), Status.Missing);

            put(SVNStatusType.CHANGED.getID(), Status.Modified);
            put(SVNStatusType.MERGED.getID(), Status.Modified);

            put(SVNStatusType.CONFLICTED.getID(), Status.Conflicting);
            put(SVNStatusType.CONFLICTED_UNRESOLVED.getID(), Status.Conflicting);
            put(SVNStatusType.STATUS_CONFLICTED.getID(), Status.Conflicting);
            put(SVNStatusType.STATUS_MODIFIED.getID(), Status.Conflicting);

            put(SVNStatusType.STATUS_ADDED.getID(), Status.Added);
            put(SVNStatusType.STATUS_REPLACED.getID(), Status.Added);

            put(SVNStatusType.STATUS_DELETED.getID(), Status.Removed);

            put(SVNStatusType.STATUS_UNVERSIONED.getID(), Status.NotTracked);

            put(SVNStatusType.STATUS_IGNORED.getID(), Status.Ignored);
        }

        // the following codes exist in SVNStatusType but are not included in the map above. These
        // will map to "Unknown".
        //        public static final SVNStatusType INAPPLICABLE = new SVNStatusType(0, "inapplicable");
        //        public static final SVNStatusType UNKNOWN = new SVNStatusType(1, "unknown");
        //        public static final SVNStatusType OBSTRUCTED = new SVNStatusType(4, "obstructed");
        //        public static final SVNStatusType LOCK_INAPPLICABLE = new SVNStatusType(0, "lock_inapplicable");
        //        public static final SVNStatusType LOCK_UNKNOWN = new SVNStatusType(1, "lock_unknown");
        //        public static final SVNStatusType LOCK_UNCHANGED = new SVNStatusType(2, "lock_unchanged");
        //        public static final SVNStatusType LOCK_LOCKED = new SVNStatusType(3, "lock_locked");
        //        public static final SVNStatusType LOCK_UNLOCKED = new SVNStatusType(4, "lock_unlocked");
        //        public static final SVNStatusType STATUS_NONE = new SVNStatusType(0, "none");
        //        public static final SVNStatusType STATUS_OBSTRUCTED = new SVNStatusType(10, "obstructed", '~');
        //        public static final SVNStatusType STATUS_INCOMPLETE = new SVNStatusType(12, "incomplete", '!');
        //        public static final SVNStatusType STATUS_EXTERNAL = new SVNStatusType(13, "external", 'X');
        //        public static final SVNStatusType STATUS_NAME_CONFLICT = new SVNStatusType(-1, "name_conflict", 'N');
        //        public static final SVNStatusType STATUS_MERGED = new SVNStatusType(8, "merged", 'G');
        //        public static final SVNStatusType NO_MERGE = new SVNStatusType(14, "no_merge");
    };
    private final static Set<SVNStatusType> STATE_WITH_LOG = Collections.unmodifiableSet(Sets.newHashSet(
            SVNStatusType.STATUS_NORMAL,
            SVNStatusType.STATUS_MODIFIED,
            SVNStatusType.STATUS_CONFLICTED,
            SVNStatusType.STATUS_REPLACED
    ));

    public SVNTrackedFile(SVNRepository repository, File file) {
        super(repository, file);
    }

    @Override
    public Commit commit(String message) throws Exception {
        // TODO: test!
        SVNCommitInfo info = getRepository().getClientManager().getCommitClient()
                .doCommit(new File[] { getFile() }, false, message, null, null, false, false, SVNDepth.FILES);

        // this is somewhat inefficient, but it gets us the "parent" commit.
        List<Commit> commits = getCommits();
        for (Commit commit : commits) {
            SVNCommit svnCommit = (SVNCommit)commit;
            if (svnCommit.getLogEntry().getRevision() == info.getNewRevision()) {
                return commit;
            }
        }

        return null;
    }

    @Override
    public List<Commit> getCommits() throws VersionControlException {
        return getCommits(null, null);
    }

    @Override
    public List<Commit> getCommits(@Nullable Date inclusiveLimitDate, @Nullable String inclusiveLimitRevision) throws VersionControlException {
        return getCommits(inclusiveLimitDate, inclusiveLimitRevision, 0);
    }

    private List<Commit> getCommits(@Nullable Date inclusiveLimitDate, @Nullable String inclusiveLimitRevision, int limit) throws VersionControlException {
        SVNLogEntryHandler handler = new SVNLogEntryHandler(
                this,
                inclusiveLimitDate,
                (inclusiveLimitRevision == null) ? null : Long.parseLong(inclusiveLimitRevision));

        try {
            SVNClientManager manager = getRepository().getClientManager();
            SVNStatus status = manager.getStatusClient().doStatus(getFile(), false);
            if (STATE_WITH_LOG.contains(status.getContentsStatus()))
                manager.getLogClient().doLog(new File[]{getFile()},
                        SVNRevision.UNDEFINED, SVNRevision.UNDEFINED, false, false, limit, handler);
        } catch (SVNException e) {
            throw new VersionControlException(e);
        }

        return handler.getCommits();
    }

    @Override
    public Status getStatus() throws VersionControlException {
        try {
            SVNStatus status = getRepository().getClientManager().getStatusClient().doStatus(getFile(), false);
            SVNStatusType contentsStatus = status.getContentsStatus();
            if (svnStatus.containsKey(contentsStatus.getID())) {
                return svnStatus.get(contentsStatus.getID());
            }
        } catch (SVNException e) {
            throw new VersionControlException(e);
        }

        return Status.Unknown;
    }
}
