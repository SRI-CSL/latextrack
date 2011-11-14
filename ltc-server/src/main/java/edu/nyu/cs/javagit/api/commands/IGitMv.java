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
package edu.nyu.cs.javagit.api.commands;

import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.options.GitMvOptions;
import edu.nyu.cs.javagit.api.responses.GitMvResponse;

import java.io.File;

/**
 * An interface to represent the git-mv command.
 * <p/>
 * This script is used to move or rename a file, directory or symlink.
 * <pre>
 * git mv [-f] [-n] <source> <destination>
 * git mv [-f] [-n] [-k] <source> ... <destination directory>
 * </pre>
 */
public interface IGitMv {

    public GitMvResponse mvFile(File repositoryPath, GitMvOptions options, String source, String destination) 
            throws JavaGitException;

    public GitMvResponse mvToDir(File repositoryPath, GitMvOptions options, String destinationDir, String... paths)
            throws JavaGitException;

}
