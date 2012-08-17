package com.sri.ltc.versioncontrol.git;

import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.TrackedFile;
import org.eclipse.jgit.lib.Constants;
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
        return null;
    }

    @Override
    public Status getStatus() throws IOException {
        org.eclipse.jgit.lib.Repository wrappedRepository = getRepository().getWrappedRepository();

        String basePath = wrappedRepository.getWorkTree().getPath();
        String relativePath = new File(basePath).toURI().relativize(getFile().toURI()).getPath();

        IndexDiff diff = new IndexDiff(wrappedRepository, Constants.HEAD, new FileTreeIterator(wrappedRepository));
        diff.setFilter(PathFilterGroup.createFromStrings(relativePath));
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

        // well, if it's none of these things, then it must be in the index and happy
        return Status.Unchanged;
    }
}
