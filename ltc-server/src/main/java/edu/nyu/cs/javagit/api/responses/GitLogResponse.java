/*
 * ====================================================================
 * Copyright (c) 2008 JavaGit Project.  All rights reserved.
 *
 * This software is licensed using the GNU LGPL v2.1 license.  A copy
 * of the license is included with the distribution of this source
 * code in the LICENSE.txt file.  The text of the license can also
 * be obtained at:
 *
 *   http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *
 * For more information on the JavaGit project, see:
 *
 *   http://www.javagit.com
 * ====================================================================
 */
package edu.nyu.cs.javagit.api.responses;

import java.util.ArrayList;
import java.util.List;

/**
 * A response data object for the git log command.
 */
public final class GitLogResponse extends AbstractResponse {

    private final List<Commit> commitList = new ArrayList<Commit>();

    public void addCommit(String sha1, List<String> merges,
                          String author, String date,
                          String committer, String committerDate,
                          List<String> parents, String message) {
        commitList.add(new Commit(sha1, merges, author, date, committer, committerDate, parents, message));
    }

    /**
     * @return This returns the commit list of the particular log instance.
     */
    public List<Commit> getLog() {
        return this.commitList;
    }

    /**
     * A data structure which  holds information about each commit.
     */
    public class Commit {

        final String sha;
        final List<String> mergeDetails;
        final String author;
        final String date;
        final String message;
        final String committer;
        final String committerDate;
        final List<String> parents;

        /**
         * Constructor for creating a commit data structure.
         *
         * @param sha           The SHA hash for a particular commit instance.
         * @param mergeDetails  The Merge details for a particular commit instance. Pass null is commit is not a merge
         * @param author        The Author for a particular commit instance.
         * @param date          The Date of a particular commit instance.
         * @param committer
         * @param committerDate
         * @param parents
         * @param message       The Message for a particular commit instance.
         */
        public Commit(String sha,
                      List<String> mergeDetails,
                      String author,
                      String date,
                      String committer, String committerDate, List<String> parents, String message) {
            this.sha = sha;
            this.mergeDetails = mergeDetails;
            this.author = author;
            this.date = date;
            this.committer = committer;
            this.committerDate = committerDate;
            this.parents = parents;
            this.message = message;
        }

        public String getSha() {
            return sha;
        }

        public List<String> getMergeDetails() {
            return mergeDetails;
        }

        public String getAuthor() {
            return author;
        }

        public String getDate() {
            return date;
        }

        public String getMessage() {
            return message;
        }

        public String getCommitter() {
            return committer;
        }

        public String getCommitterDate() {
            return committerDate;
        }

        public List<String> getParents() {
            return parents;
        }
    }
}
