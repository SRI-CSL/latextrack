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
package edu.nyu.cs.javagit.api;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <code>Commit</code> represents information about a commit to a git repository.
 * <p/>
 * TODO: Build out the class
 */
public final class Commit {

    private final Ref commitName;
    private final String tree = null;
    private final Set<Ref> parents = new HashSet<Ref>();
    private final String author;
    private final String committer;
    private final String comment;

    /**
     * Get a <code>Commit</code> instance for the specified HEAD commit offset.
     *
     * @return The <code>Commit</code>.
     */
    public static Commit getHeadCommit() {
        return new Commit(Ref.createHeadRef(0), "", "", null);
    }

    /**
     * Get a <code>Commit</code> instance for the specified SHA1 name.
     *
     * @param sha1Name See {@link edu.nyu.cs.javagit.api.Ref} for information on acceptable values of
     *                 <code>sha1Name</code>.
     * @return The <code>Commit</code>.
     */
    public static Commit getSha1Commit(String sha1Name, String author, String committer, String comment) {
        return new Commit(Ref.createSha1Ref(sha1Name), author, committer, comment);
    }

    /**
     * The constructor.
     *
     * @param commitName
     * @param author
     * @param committer
     * @param comment
     */
    private Commit(Ref commitName, String author, String committer, String comment) {
        this.commitName = commitName;
        this.author = author;
        this.committer = committer;
        this.comment = comment;
    }

    public Ref getCommitName() {
        return commitName;
    }

    public String getAuthor() {
        return author;
    }

    public String getCommitter() {
        return committer;
    }

    public void addParent(Ref parent) {
        parents.add(parent);
    }

    public Set<Ref> getParents() {
        return parents;
    }

    public String getTree() {
        return tree;
    }

    public String getComment() {
        return comment;
    }

    /**
     * Returns differences for this commit
     *
     * @return The list of differences (one per each git object).
     */
    public List<Diff> diff() {
        // GitDiff.diff();
        return null;
    }

    /**
     * Diffs this commit with another commit
     *
     * @param otherCommit The commit to compare current commit to
     * @return The list of differences (one per each git object).
     */
    public List<Diff> diff(Commit otherCommit) {
        // GitDiff.diff();
        return null;
    }

}