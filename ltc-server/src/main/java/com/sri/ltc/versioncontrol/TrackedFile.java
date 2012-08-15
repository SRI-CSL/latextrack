package com.sri.ltc.versioncontrol;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class TrackedFile<RepositoryClass extends Repository> {
    private RepositoryClass repository;
    private File file;

    public enum Status {
        Unknown,
        Added,
        Changed,
        Modified,
        Removed,
        NotTracked
    }
    
    abstract public List<Commit> getCommits() throws IOException;
    // TODO: must support/default to the following options:
    //    options.setOptFormatDate("iso8601");
    //    options.setOptOrderingTopological(true);
    //    options.setOptGraph(true);
    //    options.setOptFormat("commit %H%nAuthor: %an <%ae>%nDate: %ad%nParents: %P%n%s%n");

    abstract public Status getStatus() throws IOException;

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
