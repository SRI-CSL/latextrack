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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Response data object for &lt;git-checkout&gt; command.
 */
public class GitCheckoutResponse extends AbstractResponse {

    /**
     * List of files that have been modified but not committed.
     */
    private final List<File> modifiedFiles = new ArrayList<File>();
    /**
     * List of files added by &lt;git-add&gt; but not committed.
     */
    private final List<File> addedFiles = new ArrayList<File>();
    /**
     * List of files deleted but not committed.
     */
    private final List<File> deletedFiles = new ArrayList<File>();

    /**
     * The new branch that is created and switched to by -b option provided by &lt;git-checkout&gt;.
     */
    private Ref newBranch;

    /**
     * The name of the branch to which &lt;git-checkout&gt; switches.
     */
    private Ref branch;

    /**
     * Sets the new branch name that is created by &lt;git-checkout&gt using -b option
     *
     * @param newBranch Name of the new branch created
     */
    public void setNewBranch(Ref newBranch) {
        this.newBranch = newBranch;
    }

    /**
     * Sets the branch to the branch, to which the &lt;git-checkout&gt switched the repository to.
     * This branch should already be existing in the repository. To create a new branch and switch to
     * it, use the -b option while running &lt;git-checkout&gt.
     *
     * @param branch
     */
    public void setBranch(Ref branch) {
        this.branch = branch;
    }

    /**
     * Adds the modified file to the list of modifiedFiles. When a file is modified locally but has
     * not been committed to the repository and if we try to switch the branch to another branch, the
     * &lt;git-checkout&gt fails and outputs the list of modified files that are not yet committed
     * unless -f option is used by &lt;git-checkout&gt.
     *
     * @param file
     */
    public void addModifiedFile(File file) {
        modifiedFiles.add(file);
    }

    /**
     * Adds the newly added file to the list of addedFiles. A newly added file is the one that is
     * added by &lt;git-add&gt; command but had not been committed.
     *
     * @param file
     */
    public void addAddedFile(File file) {
        addedFiles.add(file);
    }

    /**
     * Adds the locally deleted file to the list of deletedFiles. A locally deleted file is one that
     * has been removed but has not been removed from repository using &lt;git-rm&gt; command.
     *
     * @param file
     */
    public void addDeletedFile(File file) {
        deletedFiles.add(file);
    }

    /**
     * Returns the newly created branch by -b option by &lt;git-checkout&gt;.
     *
     * @return Name of the new branch
     */
    public Ref getNewBranch() {
        return newBranch;
    }

    /**
     * Returns the branch to which &lt;git-checkout&gt; switches to.
     *
     * @return Name of the branch
     */
    public Ref getBranch() {
        return branch;
    }

    /**
     * Returns iterator to the modified files list
     *
     * @return iterator to the list.
     */
    public Iterator<File> getModifiedFilesIterator() {
        return (new ArrayList<File>(modifiedFiles).iterator());
    }

    /**
     * Gets iterator to the copy of addedFiles list.
     *
     * @return iterator to the list.
     */
    public Iterator<File> getAddedFilesIterator() {
        return (new ArrayList<File>(addedFiles).iterator());
    }

    /**
     * Gets iterator to the copy of deletedFiles list.
     *
     * @return iterator to the list.
     */
    public Iterator<File> getDeletedFilesIterator() {
        return (new ArrayList<File>(deletedFiles).iterator());
    }

    /**
     * Returns the file at a given location in the addedFiles list
     *
     * @param index in the list and should be positive and less than no. of files added.
     * @return added file at the index in addedFiles list.
     */
    public File getAddedFile(int index) {
        return addedFiles.get(index);
    }

    /**
     * Returns the file at a given location in the deletedFiles list
     *
     * @param index in the list and should be positive and less than no. of files deleted.
     * @return deleted file at the index in deleteFiles list.
     */
    public File getDeletedFile(int index) {
        return deletedFiles.get(index);
    }

    /**
     * Returns the file at a given location in the modifiedFiles list.
     *
     * @param index in the list and it should be positive and less than total no. of files modified.
     * @return modified file at the index in modifiedFiles list.
     */
    public File getModifiedFile(int index) {
        return modifiedFiles.get(index);
    }

    /**
     * Gets the total no. of files in addedFiles list.
     *
     * @return no. of files
     */
    public int getNumberOfAddedFiles() {
        return addedFiles.size();
    }

    /**
     * Gets total no. of files in modifiedFiles list.
     *
     * @return no. of files.
     */
    public int getNumberOfModifiedFiles() {
        return modifiedFiles.size();
    }

    /**
     * Gets total no. o files in addedFiles List.
     *
     * @return no. of files.
     */
    public int getNumberOfDeletedFiles() {
        return deletedFiles.size();
    }
}
