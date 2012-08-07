package com.sri.ltc;

import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.Commit;

import java.util.Date;
import java.util.List;

public class TestCommit extends Commit {
    private String id;
    private String authorName;
    private String authorEmail;
    private String message;
    private Date date;


    public TestCommit(String id, Date date, String authorName, String authorEmail, String message) {
        this.id = id;
        this.date = date;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.message = message;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Author getAuthor() {
        return new Author(authorName, authorEmail, null);
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public List<Commit> getParents() {
        assert(false); // not implemented for test class
        return null;
    }
}
