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
import edu.nyu.cs.javagit.api.options.GitRmOptions;
import edu.nyu.cs.javagit.api.responses.GitRmResponse;

import java.io.File;

/**
 * An interface implementing functionality of the git-rm command.
 * <p/>
 * <pre>
 * git rm [-f | --force] [-n] [-r] [--cached] [--ignore-unmatch] [--quiet] [--] <file>
 * </pre>
 */
public interface IGitRm {

    public GitRmResponse rm(File repository, GitRmOptions options, String... paths)
            throws JavaGitException;

}
