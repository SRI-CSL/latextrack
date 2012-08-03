/**
 ************************ 80 columns *******************************************
 * Commit
 *
 * Created on Aug 9, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.git;

import com.sri.ltc.filter.Author;
import com.sri.ltc.server.LTCserverInterface;
import edu.nyu.cs.javagit.api.responses.GitLogResponse;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * @author linda
 */

// TODO: move out of git pacakge
public final class Commit {

    public final static DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    public final static Logger LOGGER = Logger.getLogger(Commit.class.getName());

    public final String sha1;
    public final String message;
    public final Author author;
    public final Date date;

    public Commit(String sha1, String dateAsString, String authorName, String authorEmail, String message) throws ParseException {
        if (sha1 == null || "".equals(sha1))
            throw new IllegalArgumentException("Cannot create commit with empty SHA1");
        this.sha1 = sha1;
        this.message = message;
        this.author = new Author(authorName, authorEmail, null);
        this.date = FORMATTER.parse(dateAsString);
    }

    // TODO: remove this - replace with implementation within Repository object
    public static Commit translate(GitLogResponse.Commit commit) throws ParseException {
        Author author = Author.parse(commit.getAuthor());
        return new Commit(commit.getSha(), commit.getDate(), author.name, author.email, commit.getMessage());
    }

    // TODO: move toArray here
    public static Commit fromArray(Object[] array) throws ParseException {
        if (array == null)
            throw new IllegalArgumentException("Cannot create commit from NULL array");
        if (array.length < 5)
            throw new IllegalArgumentException("Cannot create commit from less than 5 objects in array");
        return new Commit(array[0].toString(), array[4].toString(), array[2].toString(), array[3].toString(), array[1].toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Commit commit = (Commit) o;

        if (!sha1.equals(commit.sha1)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return sha1.hashCode();
    }

    @Override
    public String toString() {
        return sha1.substring(0, LTCserverInterface.ON_DISK.length())+"  "+FORMATTER.format(date)+"  "+author.gitRepresentation();
    }
}
