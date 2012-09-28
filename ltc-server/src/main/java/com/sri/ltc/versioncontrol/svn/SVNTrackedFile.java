package com.sri.ltc.versioncontrol.svn;

import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.TrackedFile;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
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
        private Date exclusiveLimitDate;
        private Long exclusiveLimitRevision;

        public SVNLogEntryHandler(SVNTrackedFile trackedFile, @Nullable Date exclusiveLimitDate, @Nullable Long exclusiveLimitRevision) {
            this.trackedFile = trackedFile;
            this.exclusiveLimitDate = exclusiveLimitDate;
            this.exclusiveLimitRevision = exclusiveLimitRevision;
        }

        @Override
        public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
            if ((exclusiveLimitDate != null) && (exclusiveLimitDate.after(logEntry.getDate()))) {
                return;
            }

            if ((exclusiveLimitRevision != null) && (exclusiveLimitRevision < logEntry.getRevision())) {
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

    public SVNTrackedFile(SVNRepository repository, File file) {
        super(repository, file);
    }

    @Override
    public Commit commit(String message) throws Exception {
        // TODO
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Commit> getCommits() throws Exception {
        return getCommits(null, null);
    }

    @Override
    public List<Commit> getCommits(@Nullable Date exclusiveLimitDate, @Nullable String exclusiveLimitRevision) throws Exception {
        return getCommits(exclusiveLimitDate, exclusiveLimitRevision, 0);
    }

    public List<Commit> getCommits(@Nullable Date exclusiveLimitDate, @Nullable String exclusiveLimitRevision, int limit) throws Exception {
       SVNLogEntryHandler handler = new SVNLogEntryHandler(
               this,
               exclusiveLimitDate,
               (exclusiveLimitRevision == null) ? null : Long.parseLong(exclusiveLimitRevision));

       getRepository().getClientManager().getLogClient()
            .doLog(
                new File[]{ getFile() },
                SVNRevision.create(-1), SVNRevision.create(-1), false, false, limit, handler);

        return handler.getCommits();
    }
    
    @Override
    public Status getStatus() throws Exception {
        SVNStatus status = getRepository().getClientManager().getStatusClient().doStatus(getFile(), false);
        SVNStatusType contentsStatus = status.getContentsStatus();
        if (svnStatus.containsKey(contentsStatus.getID())) {
            return svnStatus.get(contentsStatus.getID());
        }

        return Status.Unknown;
    }
}
