/**
 ************************ 80 columns *******************************************
 * Session
 *
 * Created on May 18, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.server;

import com.sri.ltc.filter.Author;
import com.sri.ltc.git.CompleteHistory;
import com.sri.ltc.git.FileRemotes;
import com.sri.ltc.git.Self;
import com.sri.ltc.latexdiff.Accumulate;
import edu.nyu.cs.javagit.api.GitFile;
import edu.nyu.cs.javagit.api.JavaGitException;

import javax.swing.text.BadLocationException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.*;

/**
 * @author linda
 */
public final class Session {

    private static int nextID = 1;

    final int ID;
    String text = ""; // contains last known, raw editor text
    final GitFile gitFile;
    private final CompleteHistory completeHistory;
    private final FileRemotes remotes;
    private final Set<Author> knownAuthors = new HashSet<Author>();
    private final Set<Author> limitedAuthors = new HashSet<Author>();
    private String limit_date = "";
    private String limit_rev = "";
    private final Accumulate accumulate;

    protected Session(GitFile gitFile, String currentText) throws ParseException, IOException, JavaGitException, BadLocationException {
        if (gitFile == null)
            throw new IllegalArgumentException("cannot create session with NULL as git file");
        ID = generateID();
        this.gitFile = gitFile;
        this.text = currentText;
        if (currentText == null || "".equals(currentText)) {
            // read file contents into current text
            StringBuilder buffer = new StringBuilder();
            int c;
            Reader r = new FileReader(gitFile.getFile());
            while ((c = r.read()) != -1)
                buffer.append((char) c);
            r.close();
            currentText = buffer.toString();            
        }
        this.accumulate = new Accumulate(currentText);
        completeHistory = new CompleteHistory(gitFile);
        addAuthors(completeHistory.getAuthors());
        addAuthors(Collections.singleton(new Self(gitFile).getSelf()));
        remotes = new FileRemotes(gitFile);
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

    public List<Object[]> getCommitGraphAsList() throws IOException, ParseException, JavaGitException {
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
    public boolean removeLimitedAuthor(Author author) {
        return limitedAuthors.remove(author);
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
