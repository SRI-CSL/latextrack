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

import edu.nyu.cs.javagit.api.options.GitLogOptions;
import edu.nyu.cs.javagit.api.responses.GitLogResponse.Commit;
import edu.nyu.cs.javagit.client.Factory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * <code>GitFile</code> a file object in a git working tree.
 */
public class GitFile extends GitFileSystemObject {
    /**
     * The constructor. Both arguments are required (i.e. cannot be null).
     *
     * @param file        underlying <code>java.io.File</code> object
     * @param workingTree The <code>WorkingTree</code> that this file falls under.
     * @throws JavaGitException if {@link edu.nyu.cs.javagit.api.GitFileSystemObject}
     *                          throws a JavaGitException
     */
    protected GitFile(File file, WorkingTree workingTree) throws JavaGitException {
        super(file, workingTree);
    }

    /**
     * Show object's status in the working directory
     *
     * @return Object's status in the working directory (untracked, changed but not updated, etc).
     * @throws JavaGitException    if {@link edu.nyu.cs.javagit.api.commands.IGitStatus#statusSingleFile(java.io.File,edu.nyu.cs.javagit.api.options.GitStatusOptions, String)}
     *                             throws a JavaGitException
     */
    public Status getStatus() throws JavaGitException {
        return Factory.createGitStatus().statusSingleFile(workingTree.getPath(), null, getRelativePath());
    }

    /**
     * Obtain commit log for this file
     *
     * @param options Options to the git log command
     * @return List of commits for the working directory
     * @throws JavaGitException if {@link IGitLog#log(java.io.File, edu.nyu.cs.javagit.api.options.GitLogOptions , String)}
     *                          throws a JavaGitException
     * @throws java.io.IOException      if {@link IGitLog#log(java.io.File, edu.nyu.cs.javagit.api.options.GitLogOptions , String)}
     *                          throws an IOException
     */
    public List<Commit> getLog(GitLogOptions options) throws JavaGitException, IOException {
        return Factory.createGitLog().log(workingTree.getPath(), options, getRelativePath());
    }

}
