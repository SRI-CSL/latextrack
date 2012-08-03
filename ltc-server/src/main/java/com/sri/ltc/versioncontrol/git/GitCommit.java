package com.sri.ltc.versioncontrol.git;

import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.Commit;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Date;

public class GitCommit extends Commit {
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
}
