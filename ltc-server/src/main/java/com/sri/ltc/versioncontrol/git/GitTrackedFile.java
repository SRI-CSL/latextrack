package com.sri.ltc.versioncontrol.git;

import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.TrackedFile;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GitTrackedFile extends TrackedFile<GitRepository> {
    public GitTrackedFile(GitRepository repository, File file) {
        super(repository, file);
    }

    @Override
    public List<Commit> getCommits() throws IOException {
        // TODO: implement!
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Status getStatus() throws IOException {
        org.eclipse.jgit.lib.Repository wrappedRepository = getRepository().getWrappedRepository();
        IndexDiff diff = new IndexDiff(wrappedRepository, "HEAD", new FileTreeIterator(wrappedRepository));
        diff.setFilter(PathFilterGroup.createFromStrings(getFile().getPath()));
        diff.diff();

        if (!diff.getAdded().isEmpty())
			return Status.Added;
		if (!diff.getChanged().isEmpty())
            return Status.Changed;
        if (!diff.getModified().isEmpty())
			return Status.Modified;
        if (!diff.getRemoved().isEmpty())
            return Status.Removed;
        if (!diff.getUntracked().isEmpty())
            return Status.NotTracked;

        assert(false);
        return Status.Unknown;
    }
}
