/**
 ************************ 80 columns *******************************************
 * Session
 *
 * Created on May 18, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.server;

import com.google.common.collect.Sets;
import com.sri.ltc.filter.Author;
import com.sri.ltc.git.CompleteHistory;
import com.sri.ltc.git.FileRemotes;
import com.sri.ltc.latexdiff.Accumulate;
import com.sri.ltc.versioncontrol.TrackedFile;

import javax.swing.text.BadLocationException;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * @author linda
 */
public final class Session {

    private static int nextID = 1;

    final int ID;
    private final TrackedFile gitFile;
    private final CompleteHistory completeHistory;
    private final FileRemotes remotes;
    private final Set<Author> knownAuthors = Sets.newHashSet();
    private final Set<Author> limitedAuthors = Sets.newHashSet();
    private String limit_date = "";
    private String limit_rev = "";
    private final Accumulate accumulate = new Accumulate();

    protected Session(TrackedFile gitFile) throws ParseException, IOException, BadLocationException {
        ID = generateID();
        if (gitFile == null)
            throw new IllegalArgumentException("cannot create session with NULL as git file");
        this.gitFile = gitFile;
        // initializations based on git file:
        completeHistory = new CompleteHistory(gitFile);
        addAuthors(completeHistory.getAuthors());
        addAuthors(Collections.singleton(gitFile.getRepository().getSelf()));
        remotes = new FileRemotes(gitFile.getRepository());
    }

    public TrackedFile getTrackedFile() {
        return gitFile;
    }
    
    private static synchronized int generateID() {
        return nextID++;
    }

    public Accumulate getAccumulate() {
        return accumulate;
    }

    public FileRemotes getRemotes() {
        return remotes;
    }

    public List<Object[]> getCommitGraphAsList() throws IOException, ParseException {
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
