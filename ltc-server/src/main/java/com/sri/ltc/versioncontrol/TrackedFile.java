package com.sri.ltc.versioncontrol;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Date;
import java.util.List;

public abstract class TrackedFile<RepositoryClass extends Repository> {
    private RepositoryClass repository;
    private File file;

    public enum Status {
        Added,
        Changed,
        Conflicting,
        Ignored,
        Missing,
        Modified,
        NotTracked,
        Removed,
        Unchanged,

        Unknown
    }
    
    abstract public List<Commit> getCommits() throws Exception;
    // Note: must support/default to the following options or their equivalent
    //    options.setOptFormatDate("iso8601");
    //    options.setOptOrderingTopological(true);
    //    options.setOptGraph(true);
    //    options.setOptFormat("commit %H%nAuthor: %an <%ae>%nDate: %ad%nParents: %P%n%s%n");

    abstract public List<Commit> getCommits(@Nullable Date inclusiveLimitDate, @Nullable String inclusiveLimitRevision)
            throws Exception;

    abstract public Status getStatus() throws Exception;

    abstract public Commit commit(String message) throws Exception;

    public Class getRepositoryClass() {
        return repository.getClass();
    }
    
    protected TrackedFile(RepositoryClass repository, File file) {
        setRepository(repository);
        setFile(file);
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public RepositoryClass getRepository() {
        return repository;
    }

    protected void setRepository(RepositoryClass repository) {
        this.repository = repository;
    }
}
