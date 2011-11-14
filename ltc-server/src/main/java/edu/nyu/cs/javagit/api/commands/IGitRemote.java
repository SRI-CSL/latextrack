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
import edu.nyu.cs.javagit.api.options.GitRemoteOptions;
import edu.nyu.cs.javagit.api.responses.GitRemoteResponse;

import java.io.File;
import java.util.Set;

/**
 * An interface to represent the git-remote command.
 */
public interface IGitRemote {

    public Set<GitRemoteResponse.Remote> remote(File repositoryPath)
            throws JavaGitException;

    public int add(File repositoryPath, GitRemoteOptions options, String name, String url)
            throws JavaGitException;

    public int rm(File repositoryPath, GitRemoteOptions options, String name)
            throws JavaGitException;
}
