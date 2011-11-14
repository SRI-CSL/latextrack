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

import edu.nyu.cs.javagit.api.commands.IGitBranch;
import edu.nyu.cs.javagit.api.options.GitBranchOptions;
import edu.nyu.cs.javagit.api.responses.GitAddResponse;
import edu.nyu.cs.javagit.api.responses.GitBranchResponse;
import edu.nyu.cs.javagit.api.responses.GitCommitResponse;
import edu.nyu.cs.javagit.client.Factory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The <code>WorkingTree</code> represents the working copy of the files in the current branch.
 */
public final class WorkingTree {

    // This guy's a per-repository singleton, so we need a static place to store our instances.
    private static final Map<String, WorkingTree> INSTANCES = new HashMap<String, WorkingTree>();

    // The directory that contains the .git in question.
    private final File path;

    // The canonical pathname from this file. Store this here so that we don't need to continually hit
    // the filesystem to resolve it.
    private final String canonicalPath;

    // A git-specific representation of the same place this class is pointing.
    private GitDirectory rootDir;

    /**
     * The constructor. Private because this singleton-ish (per each repository) class is only
     * available via the getInstance method.
     *
     * @param path          The path to the working directory represented by the instance being created.
     * @param canonicalPath canonical representation of path.
     */
    private WorkingTree(File path, String canonicalPath) {
        this.path = path;
        this.canonicalPath = canonicalPath;
        try {
            this.rootDir = new GitDirectory(path, this);
        }
        catch (JavaGitException e) {
            //that is really impossible
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorkingTree that = (WorkingTree) o;

        if (canonicalPath != null ? !canonicalPath.equals(that.canonicalPath) : that.canonicalPath != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return canonicalPath != null ? canonicalPath.hashCode() : 0;
    }

    /**
     * Static factory method for retrieving an instance of this class.
     *
     * @param path <code>File</code> object representing the path to the repository.
     * @return The <code>WorkingTree</code> instance for this path
     * @throws JavaGitException
     */
    public static synchronized WorkingTree getInstance(File path) throws JavaGitException {
        // TODO (rs2705): make sure that path is valid

        try {
            String canonicalPath = path.getCanonicalPath();
            if (!INSTANCES.containsKey(canonicalPath))
                INSTANCES.put(canonicalPath, new WorkingTree(path, canonicalPath));
            return INSTANCES.get(canonicalPath);
        } catch (IOException e) {
            throw new JavaGitException("Error while obtaining canonical path of file "+path.getAbsolutePath());
        }
    }

    /**
     * Convenience method for retrieving an instance of the class using a <code>String</code>
     * instead of a <code>File</code>.
     *
     * @param path <code>String</code> object representing the path to the repository.
     * @return The <code>WorkingTree</code> instance for this path
     * @throws JavaGitException
     */
    public static WorkingTree getInstance(String path) throws JavaGitException {
        return getInstance(new File(path));
    }

    /**
     * Adds all known and modified files in the working directory to the index.
     *
     * @return response from git add
     * @throws JavaGitException
     */
    public GitAddResponse add() throws JavaGitException {
        return rootDir.add();
    }

    /**
     * Adds a directory to the working directory (but not to the repository!)
     *
     * @param dir name of the directory
     * @return The new <code>GitDirectory</code> object
     * @throws JavaGitException File path specified does not belong to git repo/ working tree
     */
    public GitDirectory addDirectory(String dir) throws JavaGitException {
        return new GitDirectory(new File(dir), this);
    }

    /**
     * Since instances of this class are singletons, don't allow cloning.
     *
     * @return None - always throws exception
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Commits the objects specified in the index to the repository.
     *
     * @param comment Developer's comment about the change
     * @return response from git commit
     */
    public GitCommitResponse commit(String comment) throws IOException, JavaGitException {
        return Factory.createGitCommit().commit(path, null, comment, (String[]) null);
    }

    /**
     * Automatically stage files that have been modified and deleted, but new files you have not
     * told git about are not affected
     *
     * @param comment Developer's comment about the change
     * @return response from git commit
     */
    public GitCommitResponse commitAll(String comment) throws IOException, JavaGitException {
        return Factory.createGitCommit().commitAll(path, comment);
    }

    /**
     * Stage all files and commit (including untracked)
     *
     * @param comment Developer's comment about the change
     * @return <code>GitCommitResponse</code> object
     * @throws java.io.IOException      I/O operation fails
     * @throws JavaGitException git command fails
     */
    public GitCommitResponse addAndCommitAll(String comment) throws IOException, JavaGitException {
        return rootDir.commit(comment);
    }

    /**
     * Gets the currently checked-out branch of the working directory.
     *
     * @return The currently checked-out branch of the working directory.
     */
    public Ref getCurrentBranch() throws IOException, JavaGitException {
        IGitBranch gitBranch = Factory.createGitBranch();
        GitBranchOptions options = new GitBranchOptions();
        GitBranchResponse response = gitBranch.branch(path, options);
        return response.getCurrentBranch();
    }

    /**
     * Take a standard <code>File</code> object and return it wrapped in a <code>GitDirectory</code>.
     *
     * @return A new <code>GitDirectory</code> object representing the given <code>File</code>.
     * @throws JavaGitException File path specified does not belong to git repo/ working tree
     */
    public GitDirectory getDirectory(File file) throws JavaGitException {
        return new GitDirectory(file, this);
    }

    /**
     * Take a standard <code>File</code> object and return it wrapped in a <code>GitFile</code>.
     *
     * @return A new <code>GitFile</code> object representing the given <code>File</code>.
     * @throws JavaGitException File path specified does not belong to git repo/ working tree
     */
    public GitFile getFile(File file) throws JavaGitException {
        return new GitFile(file, this);
    }

    /**
     * Show commit logs
     *
     * @return List of commits for the working directory
     */
    public List<Commit> getLog() {
        // TODO (ma1683): Implement this method
        return null;
    }

    /**
     * Gets the .git representation for this git repository
     *
     * @return The DotGit
     * @throws JavaGitException
     */
    public DotGit getDotGit() throws JavaGitException {
        return DotGit.getInstance(path);
    }

    public File getPath() {
        return path;
    }

    public String getCanonicalPath() {
        return canonicalPath;
    }

    /**
     * Gets the filesystem tree; equivalent to git-status
     *
     * @return The list of objects at the root directory
     * @throws JavaGitException File path specified does not belong to git repo/ working tree
     */
    public List<GitFileSystemObject> getTree() throws IOException, JavaGitException {
        // TODO (rs2705): Make this work - will throw NullPointerException
        return new GitDirectory(path, this).getChildren();
    }

    /**
     * Reverts the specified git commit
     *
     * @param commit Git commit that user wishes to revert
     */
    public void revert(Commit commit) {
        // TODO (ma1683): Implement this method
        // GitRevert.revert(commit.getSHA1());
    }

    /**
     * Switches to a new branch
     *
     * @param ref Git branch/sha1 to switch to
     */
    public void checkout(Ref ref) throws IOException, JavaGitException {
        Factory.createGitCheckout().checkoutNewBranch(path, null, ref.toString(), null);

        /*
        * TODO (rs2705): Figure out why this function is setting this.path. When does the WorkingTree
        * path change?
        */
        // this.path = branch.getBranchRoot().getPath();
    }
}