package com.sri.ltc.versioncontrol.svn;

import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.TrackedFile;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.xml.SVNXMLLogHandler;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class SVNTrackedFile extends TrackedFile<SVNRepository> {
    private class SVNLogEntryHandler implements ISVNLogEntryHandler {
        private SVNTrackedFile trackedFile;
        private List<Commit> commits;
        
        public SVNLogEntryHandler(SVNTrackedFile trackedFile, @Nullable Date exclusiveLimitDate, @Nullable String exclusiveLimitRevision) {
            this.trackedFile = trackedFile;
            commits = new ArrayList<Commit>();
        }
        
        @Override
        public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
            Commit commit = new SVNCommit(trackedFile, logEntry);
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
    public List<Commit> getCommits() throws Exception {
        SVNLogEntryHandler handler = new SVNLogEntryHandler(this, null, null);
        getRepository().getClientManager().getLogClient()
            .doLog(
                new File[]{ getFile() },
                SVNRevision.create(0), SVNRevision.create(-1), false, false, 0, handler);

        return handler.getCommits();
    }

    @Override
    public List<Commit> getCommits(@Nullable Date exclusiveLimitDate, @Nullable String exclusiveLimitRevision) throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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


//
//    public String getRepositoryRelativeFilePath() {
//        String basePath = getRepository().getWrappedRepository().getWorkTree().getPath();
//        return new File(basePath).toURI().relativize(getFile().toURI()).getPath();
//    }

}
