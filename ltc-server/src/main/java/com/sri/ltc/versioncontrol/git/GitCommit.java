package com.sri.ltc.versioncontrol.git;

import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.Commit;
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
    public InputStream getContentStream() throws IOException {
        if (trackedFile == null) return null;
        
        TreeWalk treeWalk = TreeWalk.forPath(getRepository().getWrappedRepository(), trackedFile.getRepositoryRelativeFilePath(), revCommit.getTree());
        ObjectId objectId = treeWalk.getObjectId(0);
        ObjectLoader loader = getRepository().getWrappedRepository().open(objectId);

        return loader.openStream();
    }

    @Override
    public List<Commit> getParents() throws Exception {
        List<Commit> parents = new ArrayList<Commit>();
        for (RevCommit parentCommit : revCommit.getParents()) {
            parents.add(new GitCommit(repository, trackedFile, parentCommit));
        }

        return parents;
    }
}
