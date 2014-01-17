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
import com.sri.ltc.latexdiff.FileReaderWrapper;
import com.sri.ltc.latexdiff.StringReaderWrapper;
import com.sri.ltc.versioncontrol.VersionControlException;
import com.sri.ltc.versioncontrol.history.CompleteHistory;
import com.sri.ltc.latexdiff.Accumulate;
import com.sri.ltc.versioncontrol.TrackedFile;
import com.sri.ltc.versioncontrol.history.HistoryUnit;
import com.sri.ltc.versioncontrol.history.LimitedHistory;

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

    // --- known authors ---

    public synchronized Set<Author> getAuthors() {
        return knownAuthors;
    }
    public synchronized void addAuthors(Collection<? extends Author> newAuthors) {
        knownAuthors.addAll(newAuthors);
    }

    // --- create limited history ---

    /**
     * Get limited history of tracked file with any limits on date or revision observed. Also observe
     * given setting whether to collapse authors.  Use also a flag whether text is modified in editor
     * and then the given current text.  If not modified, then this will test whether the file on
     * disk is currently different than the last repository version.
     * <p>
     * The returned ordered list of history units always has at least one element.
     *
     * @param collapseAuthors Whether to collapse subsequent authors and only use the latest revision
     *                        in the sequence of history units
     * @param isModified Whether the text is currently modified in the editor
     * @param currentText Current text to use if the flag <code>isModified</code> is TRUE
     * @return ordered list of history units with at least one entry
     * @throws Exception
     */
    public synchronized List<HistoryUnit> createLimitedHistory(boolean collapseAuthors, boolean isModified,
                                                               String currentText) throws Exception {
        Author self = getTrackedFile().getRepository().getSelf();

        // process modified & on disk cases first:
        HistoryUnit last = null;
        if (isModified)
            // use current text from editor, if modified since last save:
            last = new HistoryUnit(self,
                    LTCserverInterface.MODIFIED,
                    new StringReaderWrapper(currentText));
        else
            switch (getTrackedFile().getStatus()) {
                case Added:
                case Modified:
                case Changed:
                case Conflicting: // TODO: once we implement merge assistance, maybe this gets handled differently
                    // use file on disk, if not yet committed:
                    last = new HistoryUnit(self,
                            LTCserverInterface.ON_DISK,
                            new FileReaderWrapper(getTrackedFile().getFile().getCanonicalPath()));
            }

        String limitingRev = limit_rev;
        // handle if limit_rev matches "on disk" or "modified" (or starts with those!)
        // but only allow matching if present in 'units'!
        if (!limit_rev.isEmpty() && last != null && last.revision.startsWith(limit_rev))
            // no limiting revision OR limiting revision is neither the beginning of "modified" nor "on disk":
            // obtain revision history from version control system
            limitingRev = TrackedFile.HAT_REVISION;
        List<HistoryUnit> units = new LimitedHistory(trackedFile, limit_date, limitingRev, collapseAuthors).getHistoryUnits();

        // process last unit: if exists, possibly replace the last one from VC
        if (last != null) {
            // check if we collapse authors and need to remove the newest version:
            if (collapseAuthors && !units.isEmpty() && last.author.equals(self))
                units.remove(units.size()-1);
            units.add(last); // add given last unit to those retrieved from version control
        }

        // if no history, then use text from file and self as author
        if (units.isEmpty())
            units.add(new HistoryUnit(self, "",
                    new FileReaderWrapper(getTrackedFile().getFile().getCanonicalPath())));

        return units;
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
