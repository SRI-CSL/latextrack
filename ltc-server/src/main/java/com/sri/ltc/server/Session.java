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
package com.sri.ltc.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.VersionControlException;
import com.sri.ltc.versioncontrol.history.CompleteHistory;
import com.sri.ltc.versioncontrol.Remotes;
import com.sri.ltc.latexdiff.Accumulate;
import com.sri.ltc.versioncontrol.TrackedFile;
import com.sri.ltc.versioncontrol.history.LimitedHistory;

import java.awt.*;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.List;

/**
 * @author linda
 */
public final class Session {

    private static int nextID = 1;

    final int ID;
    private final TrackedFile trackedFile;
    private final Accumulate accumulate = new Accumulate();
    private final Set<Author> knownAuthors = Sets.newHashSet();
    private final Set<Author> limitedAuthors = Sets.newHashSet();
    private String limit_date = "";
    private String limit_rev = "";

    protected Session(TrackedFile trackedFile) throws IOException, ParseException, VersionControlException {
        ID = generateID();
        if (trackedFile == null)
            throw new IllegalArgumentException("cannot create session with NULL as tracked file");
        this.trackedFile = trackedFile;
        // initializations based on tracked file:
        addAuthors(new CompleteHistory(trackedFile).getAuthors());
        addAuthors(Collections.singleton(trackedFile.getRepository().getSelf()));
    }

    public TrackedFile getTrackedFile() {
        return trackedFile;
    }

    // NEED THIS METHOD IN ORDER TO HAVE "synchronized" KEYWORD
    private static synchronized int generateID() {
        return nextID++; // could wrap...
    }

    public Accumulate getAccumulate() {
        return accumulate;
    }

    // synchronize all the following accessor methods to the data sets in this session:

    // --- known authors ---  TODO: make colors unique?

    public synchronized Set<Author> getAuthors() {
        return knownAuthors;
    }
    public synchronized void addAuthors(Collection<? extends Author> newAuthors) {
        knownAuthors.addAll(newAuthors);
    }

    // --- create limited history ---
    public synchronized LimitedHistory createLimitedHistory(boolean collapseAuthors) throws Exception {
        return new LimitedHistory(trackedFile, limitedAuthors, limit_date, limit_rev, collapseAuthors);
    }

    // --- limited authors ---

    public synchronized Set<Author> getLimitedAuthors() {
        return limitedAuthors;
    }
    public synchronized boolean addLimitedAuthor(Author author) {
        return limitedAuthors.add(author);
    }
    public synchronized void resetLimitedAuthors() {
        limitedAuthors.clear();
    }

    // --- limited date ---

    public synchronized void setLimitDate(String date) {
        limit_date = date==null?"":date;
    }
    public synchronized String getLimitDate() {
        return limit_date;
    }

    // --- limited revision ---

    public synchronized void setLimitRev(String limit_rev) {
        this.limit_rev = limit_rev==null?"":limit_rev;
    }
    public synchronized String getLimitRev() {
        return limit_rev;
    }
}
