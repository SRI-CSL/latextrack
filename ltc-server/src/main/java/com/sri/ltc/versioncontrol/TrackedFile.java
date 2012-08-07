package com.sri.ltc.versioncontrol;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class TrackedFile {
    private Repository repository;
    private File file;

    abstract public List<Commit> getCommits() throws IOException;
    // TODO: must support/default to the following options:
    //    options.setOptFormatDate("iso8601");
    //    options.setOptOrderingTopological(true);
    //    options.setOptGraph(true);
    //    options.setOptFormat("commit %H%nAuthor: %an <%ae>%nDate: %ad%nParents: %P%n%s%n");


    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    protected TrackedFile(Repository repository, File file) {
        setRepository(repository);
    }

    public Repository getRepository() {
        return repository;
    }

    protected void setRepository(Repository repository) {
        this.repository = repository;
    }
}
