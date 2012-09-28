/**
 ************************ 80 columns *******************************************
 * FileHistory
 *
 * Created on Jul 24, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.git;

import com.sri.ltc.filter.Author;
import com.sri.ltc.latexdiff.CommitReaderWrapper;
import com.sri.ltc.latexdiff.ReaderWrapper;
import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.TrackedFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author linda
 */
public final class LimitedHistory extends FileHistory {

    private final static Logger LOGGER = Logger.getLogger(LimitedHistory.class.getName());
    private final Set<Author> limitingAuthors;
    private final String limitingDate;
    private final String limitingRev;

    public LimitedHistory(TrackedFile gitFile, Set<Author> limitingAuthors, String limitingDate, String limitingRev)
            throws Exception {
        super(gitFile);
        this.limitingAuthors = limitingAuthors;
        this.limitingDate = limitingDate;
        this.limitingRev = limitingRev;
        update();
    }

    @Override
    List<Commit> updateCommits() throws Exception {
        // perform git log:
        //   with limiting date, if applicable
        return trackedFile.getCommits(
                ((limitingDate == null) || limitingDate.isEmpty()) ? null : Commit.deSerializeDate(limitingDate),
                ((limitingRev == null) || limitingRev.isEmpty()) ? null : limitingRev);
    }

    @Override
    void transformGraph() {
        // TODO: implement - maybe? (wasn't implemented pre git change)
//        // reduce commit graph to authors
//        commitGraph.reduceByAuthors(limitingAuthors);
//
//        // collapse sequences of same author
//        if (commitGraph.vertexSet().size() > 0) {
//            Author currentAuthor = Author.parse(gitCommits.get(0).getAuthor());
//            for (ListIterator<GitLogResponse.Commit> i = gitCommits.listIterator(1); i.hasNext(); ) {
//                Author a = Author.parse(i.next().getAuthor());
//                if (a.equals(currentAuthor))
//                    i.remove();
//                else
//                    currentAuthor = a;
//            }
//        }
//
//        // if no limiting date nor rev then reduce list until last commit of calling author (by name only)
//        if ((limitingDate == null || "".equals(limitingDate)) &&
//                (limitingRev == null || "".equals(limitingRev))) {
//            int i = 0; // start with most current
//            // ignore most recent commit(s) of self:
//            for (; i < gitCommits.size() && self.name.equals(Author.parse(gitCommits.get(i).getAuthor()).name); i++);
//            // keep all commits from other authors
//            for (; i < gitCommits.size() && !self.name.equals(Author.parse(gitCommits.get(i).getAuthor()).name); i++);
//            if (i < gitCommits.size())
//                gitCommits.subList(i+1, gitCommits.size()).clear(); // remove all remaining commits
//        }
    }

    @Override
    void transformList() throws IOException {
        Author self = trackedFile.getRepository().getSelf();

        // reduce commit graph to authors
        if (limitingAuthors != null && !limitingAuthors.isEmpty())
            for (ListIterator<Commit> i = commitList.listIterator(); i.hasNext(); )
                if (!limitingAuthors.contains(i.next().getAuthor()))
                    i.remove();

        // collapse sequences of same author
        if (commitList.size() > 0) {
            Author currentAuthor = commitList.get(0).getAuthor();
            for (ListIterator<Commit> i = commitList.listIterator(1); i.hasNext(); ) {
                Author a = i.next().getAuthor();
                if (a.equals(currentAuthor))
                    i.remove();
                else
                    currentAuthor = a;
            }
        }

        // if no limiting date nor rev then reduce list until last commit of calling author (by name only)
        if ((limitingDate == null || "".equals(limitingDate)) &&
                (limitingRev == null || "".equals(limitingRev))) {
            int i = 0; // start with most current
            // ignore most recent commit(s) of self:
            for (; i < commitList.size() && self.name.equals(commitList.get(i).getAuthor().name); i++);
            // keep all commits from other authors
            for (; i < commitList.size() && !self.name.equals(commitList.get(i).getAuthor().name); i++);
            if (i < commitList.size())
                commitList.subList(i+1, commitList.size()).clear(); // remove all remaining commits
        }

        LOGGER.info("Transformed list for \""+ trackedFile.getFile().getName()+"\" to "+commitList.size()+" commits");
    }

    public final List<Commit> getCommitsList() {
        return commitList;
    }

    public final List<Author> getAuthorsList() throws IOException, ParseException {
        List<Author> authors = new ArrayList<Author>();
        for (Commit commit : commitList)
            authors.add(commit.getAuthor());
        return authors;
    }

    public final List<ReaderWrapper> getReadersList() throws IOException, ParseException {
        List<ReaderWrapper> readers = new ArrayList<ReaderWrapper>();
        for (Commit commit : commitList) {
            // obtain string for readers
            readers.add(new CommitReaderWrapper(commit));
        }
        return readers;
    }
}
