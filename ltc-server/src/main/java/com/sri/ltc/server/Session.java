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

import com.google.common.collect.Sets;
import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.VersionControlException;
import com.sri.ltc.versioncontrol.history.CompleteHistory;
import com.sri.ltc.versioncontrol.Remotes;
import com.sri.ltc.latexdiff.Accumulate;
import com.sri.ltc.versioncontrol.TrackedFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * @author linda
 */
public final class Session {

    private static int nextID = 1;

    final int ID;
    private final TrackedFile trackedFile;
    private final CompleteHistory completeHistory;
    private final Remotes remotes;
    private final Set<Author> knownAuthors = Sets.newHashSet();
    private final Set<Author> limitedAuthors = Sets.newHashSet();
    private String limit_date = "";
    private String limit_rev = "";
    private final Accumulate accumulate = new Accumulate();

    protected Session(TrackedFile trackedFile) throws IOException, ParseException, VersionControlException {
        ID = generateID();
        if (trackedFile == null)
            throw new IllegalArgumentException("cannot create session with NULL as tracked file");
        this.trackedFile = trackedFile;
        // initializations based on tracked file:
        completeHistory = new CompleteHistory(trackedFile);
        addAuthors(completeHistory.getAuthors());
        addAuthors(Collections.singleton(trackedFile.getRepository().getSelf()));
        remotes = trackedFile.getRepository().getRemotes();
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

    public Remotes getRemotes() {
        return remotes;
    }

    public List<Object[]> getCommitGraphAsList() throws Exception {
        return completeHistory.update();
    }

    // --- known authors ---

    public Set<Author> getAuthors() {
        return knownAuthors;
    }
    public void addAuthors(Collection<? extends Author> newAuthors) {
        knownAuthors.addAll(newAuthors);
    }

    // --- limited authors ---

    public Set<Author> getLimitedAuthors() {
        return limitedAuthors;
    }
    public boolean addLimitedAuthor(Author author) {
        return limitedAuthors.add(author);
    }
    public void resetLimitedAuthors() {
        limitedAuthors.clear();
    }

    // --- limited date ---

    public void setLimitDate(String date) {
        limit_date = date==null?"":date;
    }
    public String getLimitDate() {
        return limit_date;
    }

    // --- limited revision ---

    public void setLimitRev(String limit_rev) {
        this.limit_rev = limit_rev==null?"":limit_rev;
    }
    public String getLimitRev() {
        return limit_rev;
    }
}
