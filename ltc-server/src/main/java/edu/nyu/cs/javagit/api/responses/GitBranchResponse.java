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

import edu.nyu.cs.javagit.api.Ref;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A response data object for the git-branch command.
 */
public class GitBranchResponse extends AbstractResponse {
    /**
     * An enumeration of the types of response. In normal case a list of branches, otherwise some
     * message such as "Deleted branch".
     */
    public static enum responseType {
        BRANCH_LIST, MESSAGE, EMPTY
    }

    // The list of branches in the response of git-branch.
    private List<Ref> branchList;

    // The list of branch records, in response of git-branch with verbose option.
    private List<BranchRecord> listOfBranchRecord;

    // String Buffer to store the message after execution of git-branch command.
    private StringBuffer messages = new StringBuffer();

    // Variable to store the current branch.
    private Ref currentBranch;

    // The type of this response.
    private responseType responseType;

    /**
     * Constructor.
     */
    public GitBranchResponse() {
        branchList = new ArrayList<Ref>();
        listOfBranchRecord = new ArrayList<BranchRecord>();
    }

    /**
     * Add the branch displayed by git-branch command into the list of branches.
     *
     * @return true after the file gets added.
     */
    public boolean addIntoBranchList(Ref branchName) {
        return branchList.add(branchName);
    }

    /**
     * Add the record displayed by git-branch command with -v option into the list of records.
     *
     * @return True after the record gets added.
     */
    public boolean addIntoListOfBranchRecord(BranchRecord record) {
        return listOfBranchRecord.add(record);
    }

    /**
     * Sets a message about the git-branch operation that was run.
     *
     * @param message A message about the git-branch operation that was run.
     */
    public void addMessages(String message) {
        messages.append(message);
    }

    /**
     * Sets the current branch from the list of branches displayed by git-branch operation.
     *
     * @param currentBranch The current branch from the list of branches displayed by git-branch operation.
     */
    public void setCurrentBranch(Ref currentBranch) {
        this.currentBranch = currentBranch;
    }

    /**
     * Sets the type of the response.
     *
     * @param responseType The responseType to set to one of the three types.
     */
    public void setResponseType(responseType responseType) {
        this.responseType = responseType;
    }

    /**
     * Get an <code>Iterator</code> with which to iterate over the branch list.
     *
     * @return An <code>Iterator</code> with which to iterate over the branch list.
     */
    public Iterator<Ref> getBranchListIterator() {
        return (new ArrayList<Ref>(branchList).iterator());
    }

    /**
     * Get an <code>Iterator</code> with which to iterate over the branch record list.
     *
     * @return An <code>Iterator</code> with which to iterate over the branch record list.
     */
    public Iterator<BranchRecord> getListOfBranchRecordIterator() {
        return (new ArrayList<BranchRecord>(listOfBranchRecord).iterator());
    }

    /**
     * Gets the type of the response. Branch list, message or empty.
     *
     * @return The responseType.
     */
    public responseType getResponseType() {
        return responseType;
    }

    /**
     * Gets a message about the git-branch operation that was run.
     *
     * @return A message about the git-branch operation that was run.
     */
    public String getMessages() {
        return messages.toString();
    }

    /**
     * Gets the current branch from the list of branches displayed by git-branch operation.
     *
     * @return The current branch from the list of branches displayed by git-branch operation.
     */
    public Ref getCurrentBranch() {
        return currentBranch;
    }

    public static class BranchRecord {
        private final Ref branch;

        // The SHA Refs of a branch in the response of git-branch with -v option.
        private final Ref sha1;

        // String Buffer to store the comment after execution of git-branch command with -v option.
        private final String comment;

        // Variable to store the current branch.
        private final boolean isCurrentBranch;

        public BranchRecord(Ref branch, Ref sha1, String comment, boolean isCurrentBranch) {
            this.branch = branch;
            this.sha1 = sha1;
            this.comment = comment;
            this.isCurrentBranch = isCurrentBranch;
        }
    }
}
