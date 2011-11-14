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
import edu.nyu.cs.javagit.api.options.GitAddOptions;
import edu.nyu.cs.javagit.api.responses.GitAddResponse;

import java.io.File;

/**
 * An interface to represent the git-add command.
 */
public interface IGitAdd {

    /**
     * Adds list of files to the index.
     *
     * @param repositoryPath File path pointing to the root of the repository
     * @param options        Object containing all the options that need to be passed to &lt;git-add&gt; command.
     * @param pathPatterns
     * @return GitAddResponse object.
     * @throws edu.nyu.cs.javagit.api.JavaGitException
     */
    public GitAddResponse add(File repositoryPath, GitAddOptions options, String... pathPatterns)
            throws JavaGitException;
}
