package com.sri.ltc.versioncontrol.git;

import com.sri.ltc.filter.Author;
import com.sri.ltc.server.LTCserverInterface;
import com.sri.ltc.versioncontrol.Commit;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Date;
import java.util.List;

public class GitCommit extends com.sri.ltc.versioncontrol.Commit {
    private RevCommit revCommit;

    public GitCommit(RevCommit revCommit) {
        super();

        this.revCommit = revCommit;
    }

    @Override
    public String getId() {
        return revCommit.getId().toString();
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
        return new Date(revCommit.getCommitTime());
    }

    @Override
    public List<Commit> getParents() {
        // TODO
        return null;
    }

    // TODO: not sure about this LTCserverInterface reference - may need to abstract out somehow
    @Override
    public String toString() {
        return getId().substring(0, LTCserverInterface.ON_DISK.length()) + "  " + FORMATTER.format(getDate()) + "  " + getAuthor().gitRepresentation();
    }
}
