package com.sri.ltc.versioncontrol;

import com.sri.ltc.filter.Author;
import com.sri.ltc.server.LTCserverInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public abstract class Commit {
    public final static Logger LOGGER = Logger.getLogger(Commit.class.getName());

    private final static DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    protected Repository repository;

    protected Commit(Repository repository) {
        this.repository = repository;
    }

    abstract public String getId();
    abstract public String getMessage();
    abstract public Author getAuthor();
    abstract public Date getDate();

    abstract public List<Commit> getParents();

    // TODO: these two methods should probably go into a utility class of some form
    public static String serializeDate(Date date) {
        return FORMATTER.format(date);
    }

    public static Date deSerializeDate(String date) throws ParseException {
        return FORMATTER.parse(date);
    }
    
    public Reader getContents() throws IOException {
        // TODO: this is a muddle - could move this implementation down into GitCommit
        // or change GitTrackedFile to call into the repository class
        InputStream inputStream = repository.getContentStream(this);
        return new InputStreamReader(inputStream);
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
        return getId().substring(0, LTCserverInterface.ON_DISK.length())
                + "  " + serializeDate(getDate())
                + "  " + getAuthor().gitRepresentation();
    }
}
