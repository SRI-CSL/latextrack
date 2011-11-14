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
import edu.nyu.cs.javagit.api.options.GitLogOptions;
import edu.nyu.cs.javagit.api.responses.GitBranchResponse;
import edu.nyu.cs.javagit.api.responses.GitLogResponse.Commit;
import edu.nyu.cs.javagit.client.Factory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The <code>DotGit</code> represents the .git directory.
 */
public final class DotGit {

    // use this filter to find proper working directory
    private final static FilenameFilter GIT_FILTER = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return ".git".equals(name);
        }};

    // This guy's a per-repository singleton, so we need a static place to store our instances.
    private static final Map<String, DotGit> INSTANCES = new HashMap<String, DotGit>();

    // The directory that contains the .git in question.
    private final File path;

    // The canonical pathname from this file. Store this here so that we don't need to continually hit
    // the filesystem to resolve it.
    private final String canonicalPath;

    /**
     * The constructor. Private because this singleton-ish (per each repository) class is only
     * available via the getInstance method.
     *
     * @param path The path to the directory containing the .git file in question.
     * @param canonicalPath String with canonical path of given path.
     */
    private DotGit(File path, String canonicalPath) {
        this.path = path;
        this.canonicalPath = canonicalPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DotGit dotGit = (DotGit) o;

        if (canonicalPath != null ? !canonicalPath.equals(dotGit.canonicalPath) : dotGit.canonicalPath != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return canonicalPath != null ? canonicalPath.hashCode() : 0;
    }

    /**
     * Checks if there is a DotGit instance for a given path
     *
     * @param path <code>File</code> object representing the path to the repository.
     * @return true if exits, false otherwise;
     */
    public static synchronized boolean existsInstance(File path) {
        String canonicalPath = "";

        try {
            canonicalPath = path.getCanonicalPath();
        } catch (IOException e) {
            //obviously, the answer is NO
            return false;
        }

        return INSTANCES.containsKey(canonicalPath);
    }

    /**
     * Static factory method for retrieving an instance of this class.
     *
     * The given File object is used to search for the occurrence of a ".git"
     * entry in any of the given file's ancestors.
     *
     * @param path <code>File</code> object to be expected being under a git repository.
     * @return The <code>DotGit</code> instance for this path
     * @throws edu.nyu.cs.javagit.api.JavaGitException if given path is not a directory or doesn't contain
     *   .git (or any ancestor of it)
     */
    public static synchronized DotGit getInstance(File path) throws JavaGitException {
        // walk-up file hierarchy until we find .git in the directory
        File dir = path;
        if (!path.isDirectory())
            dir = path.getParentFile();
        while (dir != null && dir.isDirectory() && dir.list(GIT_FILTER).length == 0)
            dir = dir.getParentFile();

        if (dir == null)
            throw new JavaGitException("Cannot find .git in any ancestor of given path "+path.getAbsolutePath());

        try {
            String canonicalPath = dir.getCanonicalPath();
            if (!INSTANCES.containsKey(canonicalPath)) 
                INSTANCES.put(canonicalPath, new DotGit(dir, canonicalPath));
            return INSTANCES.get(canonicalPath);
        } catch (IOException e) {
            throw new JavaGitException("Error while obtaining canonical path of directory "+dir.getAbsolutePath());
        }
    }

    /**
     * Convenience method for retrieving an instance of the class using a <code>String</code>
     * instead of a <code>File</code>.
     *
     * @param path <code>String</code> object representing the path to the repository.
     * @return The <code>DotGit</code> instance for this path
     * @throws edu.nyu.cs.javagit.api.JavaGitException
     * @see #getInstance(java.io.File)
     */
    public static DotGit getInstance(String path) throws JavaGitException {
        return getInstance(new File(path));
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
     * Creates a new branch
     *
     * @param name The name of the branch to create
     * @return The new branch
     */
    public Ref createBranch(String name) throws IOException, JavaGitException {
        Ref newBranch = Ref.createBranchRef(name);
        IGitBranch gitBranch = Factory.createGitBranch();
        gitBranch.createBranch(path, null, newBranch);
        return newBranch;
    }

    /**
     * Deletes a branch
     *
     * @param branch      The branch to delete
     * @param forceDelete True if force delete option -D should be used,
     *                    false if -d should be used.
     * @throws java.io.IOException      Thrown in case of I/O operation failure
     * @throws edu.nyu.cs.javagit.api.JavaGitException Thrown when there is an error executing git-branch.
     */
    public void deleteBranch(Ref branch, boolean forceDelete)
            throws IOException, JavaGitException {
        IGitBranch gitBranch = Factory.createGitBranch();
        gitBranch.deleteBranch(path, forceDelete, false, branch);
        branch = null;
    }

    /**
     * Renames a branch
     *
     * @param branchFrom  The branch to rename
     * @param nameTo      New branch name
     * @param forceRename True if force rename option -M should be used.
     *                    False if -m should be used.
     * @return new <code>Ref</code> instance
     * @throws java.io.IOException      Thrown in case of I/O operation failure
     * @throws edu.nyu.cs.javagit.api.JavaGitException Thrown when there is an error executing git-branch.
     */
    public Ref renameBranch(Ref branchFrom, String nameTo, boolean forceRename)
            throws IOException, JavaGitException {
        Ref newBranch = Ref.createBranchRef(nameTo);
        IGitBranch gitBranch = Factory.createGitBranch();
        gitBranch.renameBranch(path, forceRename, branchFrom, newBranch);
        return newBranch;
    }

    /**
     * Gets a list of the branches in the repository.
     *
     * @return The branches in the repository.
     */
    public Iterator<Ref> getBranches() throws IOException, JavaGitException {
        IGitBranch gitBranch = Factory.createGitBranch();
        GitBranchOptions options = new GitBranchOptions();
        GitBranchResponse response = gitBranch.branch(path, options);
        return response.getBranchListIterator();
    }

    public File getPath() {
        return path;
    }

    public String getCanonicalPath() {
        return canonicalPath;
    }

    public WorkingTree getWorkingTree() throws JavaGitException {
        return WorkingTree.getInstance(path);
    }

    /**
     * Show commit logs using given log options.
     *
     * @param options Options to the git log command
     * @throws edu.nyu.cs.javagit.api.JavaGitException
     * @throws java.io.IOException
     * @return List of commits for the working directory
     */
    public List<Commit> getLog(GitLogOptions options) throws JavaGitException, IOException {
        return Factory.createGitLog().log(this.getPath(), options, null);
    }

}
