package com.sri.ltc.versioncontrol.git;

import com.sri.ltc.filter.Author;
import com.sri.ltc.server.LTCserverInterface;
import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.Repository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GitCommit extends Commit {
    private RevCommit revCommit;

    public static Date CommitDate(RevCommit revCommit) {
        return new Date(revCommit.getCommitTime() * 1000L);
    }
    
    public GitCommit(Repository repository, RevCommit revCommit) {
        super(repository);
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
    public List<Commit> getParents() {
        List<Commit> parents = new ArrayList<Commit>();
        for (RevCommit parentCommit : revCommit.getParents()) {
            parents.add(new GitCommit(repository, parentCommit));
        }

        return parents;
    }
}
