/*
 * #%L
 * LaTeX Track Changes (LTC) allows collaborators on a version-controlled LaTeX writing project to view and query changes in the .tex documents.
 * %%
 * Copyright (C) 2009 - 2012 SRI International
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package com.sri.ltc;

import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.Commit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

public class TestCommit extends Commit {
    private String id;
    private String authorName;
    private String authorEmail;
    private String message;
    private Date date;

    public TestCommit(String id, Date date, String authorName, String authorEmail, String message) {
        super(null, null);
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

    @Override
    public InputStream getContentStream() {
        assert(false); // not implemented for test class
        return null;
    }
}
