package com.sri.ltc.versioncontrol;

import com.sri.ltc.filter.Author;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public abstract class Commit<RepositoryClass extends Repository, TrackedFileClass extends TrackedFile<RepositoryClass>> {
    public final static Logger LOGGER = Logger.getLogger(Commit.class.getName());

    private final static DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    protected RepositoryClass repository;
    protected TrackedFileClass trackedFile;

    protected Commit(RepositoryClass repository, TrackedFileClass trackedFile) {
        this.repository = repository;
        this.trackedFile = trackedFile;
    }

    abstract public String getId();
    abstract public String getMessage();
    abstract public Author getAuthor();
    abstract public Date getDate();

    abstract public List<Commit> getParents();

    // note: this only makes sense if the commit was a single-file commit
    // if the commit was not filtered by file, this method will return null
    abstract public InputStream getContentStream() throws VersionControlException;

    // TODO: these two methods should probably go into a utility class of some form
    public static String serializeDate(Date date) {
        return FORMATTER.format(date);
    }

    public static Date deSerializeDate(String date) throws ParseException {
        return FORMATTER.parse(date);
    }
    
    public Reader getContents() throws VersionControlException {
        InputStream is = getContentStream();
        if (is == null)
            throw new NullPointerException("Cannot get content stream");
        return new InputStreamReader(getContentStream());
    }

    public RepositoryClass getRepository() {
        return repository;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Commit)) return false;

        Commit commit = (Commit) o;

        if (getId() != null ? !getId().equals(commit.getId()) : commit.getId() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        return getId() //.substring(0, LTCserverInterface.ON_DISK.length())
                + "  " + serializeDate(getDate())
                + "  " + getAuthor().gitRepresentation();
    }
}
