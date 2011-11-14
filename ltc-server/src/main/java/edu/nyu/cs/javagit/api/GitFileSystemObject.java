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

import edu.nyu.cs.javagit.api.responses.GitAddResponse;
import edu.nyu.cs.javagit.api.responses.GitCommitResponse;
import edu.nyu.cs.javagit.api.responses.GitMvResponse;
import edu.nyu.cs.javagit.api.responses.GitRmResponse;
import edu.nyu.cs.javagit.client.Factory;

import java.io.File;
import java.io.IOException;

/**
 * <code>GitFileSystemObject</code> provides some implementation shared by files and directories
 */
public abstract class GitFileSystemObject {

    public static enum Status {
        // untracked (created but not added to the repository)
        UNTRACKED("?"),
        // new, waiting to commit
        ADDED("A"),
        // deleted, waiting to commit
        DELETED("D"),
        // changed and added to the index
        MODIFIED("M"),
        // in repository and unmodified
        IN_REPOSITORY(""),
        RENAMED("R"),
        COPIED("C"),
        // updated but unmerged
        UPDATED("U");

        private String statusCode;

        private Status(String statusCode) {
            this.statusCode = statusCode;
        }

        public static Status parseCode(String code) {
            for (Status s : Status.values())
                if (s.statusCode.equals(code))
                    return s;
            throw new RuntimeException("Couldn't parse code \"" + code + "\" into status object");
        }
    }

    // underlying Java file object
    protected File file;
    protected final WorkingTree workingTree;

    private String relativePath;

    /**
     * The constructor.
     *
     * @param file underlying <code>java.io.File</code> object
     */
    protected GitFileSystemObject(File file, WorkingTree workingTree) throws JavaGitException {
        this.workingTree = workingTree;
        this.file = file;
        this.relativePath = calculateRelativePath(file, workingTree.getCanonicalPath());
    }

    public String getRelativePath() {
        return relativePath;
    }

    public WorkingTree getWorkingTree() {
        return workingTree;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GitFileSystemObject)) return false;

        GitFileSystemObject that = (GitFileSystemObject) o;

        if (file != null ? !file.equals(that.file) : that.file != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return file != null ? file.hashCode() : 0;
    }

    public File getFile() {
        return file;
    }

    /**
     * Gets parent directory of this <code>GitFileSystemObject</code> object
     *
     * @return parent directory (null if no parent exists)
     * @throws edu.nyu.cs.javagit.api.JavaGitException if {@link edu.nyu.cs.javagit.api.GitDirectory} cannot be created
     */
    public GitDirectory getParent() throws JavaGitException {
        // NOTE: file.getParentFile() returns null if there is no parent.
        if (file.getParentFile() == null)
            return null;

        return new GitDirectory(file.getParentFile(), workingTree);
    }

    // this assumes that the given file is under the repository, thus the canonical paths are substrings
    private static String calculateRelativePath(File file, String repositoryPath) throws JavaGitException {
        try {
            String path = file.getCanonicalPath();
            if (!path.startsWith(repositoryPath))
                throw new JavaGitException("Canonical path \""+path+
                        "\" does not start with given repository \""+repositoryPath+"\"");
            return path.substring(repositoryPath.length()).replaceAll("^"+File.separator+"*","");
        } catch (IOException e) {
            throw new JavaGitException("Couldn't compute relative path for file \""+file+
                    "\" under repository \""+repositoryPath+"\"");
        }
    }

    /**
     * Adds the object to the git index
     *
     * @return response from git add
     * @throws edu.nyu.cs.javagit.api.JavaGitException
     */
    public GitAddResponse add() throws JavaGitException {
        return Factory.createGitAdd().add(workingTree.getPath(), null, getRelativePath());
    }

    /**
     * Commits the file system object
     *
     * @param comment Developer's comment
     * @return response from git commit
     * @throws edu.nyu.cs.javagit.api.JavaGitException
     */
    public GitCommitResponse commit(String comment) throws JavaGitException {
        return Factory.createGitCommit().commitOnly(workingTree.getPath(), comment, getRelativePath());
    }

    /**
     * Moves or renames the object
     *
     * @param destination destination file
     * @return response from git mv
     * @throws edu.nyu.cs.javagit.api.JavaGitException
     */
    public GitMvResponse mv(File destination) throws JavaGitException {
        // perform git-mv
        GitMvResponse response = Factory.createGitMv().mvFile(workingTree.getPath(),
                null,
                getRelativePath(),
                calculateRelativePath(destination, workingTree.getCanonicalPath()));

        // file has changed; update fields
        file = destination;
        relativePath = calculateRelativePath(file, workingTree.getCanonicalPath());

        return response;
    }

    /**
     * Removes the file system object from the working tree and the index
     *
     * @return response from git rm
     * @throws edu.nyu.cs.javagit.api.JavaGitException
     */
    public GitRmResponse rm() throws JavaGitException {
        return Factory.createGitRm().rm(workingTree.getPath(), null, getRelativePath());
    }
}
