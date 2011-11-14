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
package edu.nyu.cs.javagit.client.cli;

import edu.nyu.cs.javagit.api.JavaGitConfiguration;
import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.commands.IGitPull;
import edu.nyu.cs.javagit.api.options.GitPullOptions;
import edu.nyu.cs.javagit.api.responses.AbstractResponse;
import edu.nyu.cs.javagit.utilities.CheckUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Command-line implementation of the <code>IGitPull</code> interface.
 */
public class CliGitPull implements IGitPull {

    @Override
    public AbstractResponse master(File repositoryPath, GitPullOptions options, String remoteRepository) throws JavaGitException {
        CheckUtilities.checkFileValidity(repositoryPath);
        CheckUtilities.checkStringArgument(remoteRepository, "remote repository");

        return ProcessUtilities.runCommand(repositoryPath,
                buildCommand(options, remoteRepository, "master"),
                new AbstractParser<AbstractResponse>(new AbstractResponse() {}) {});
    }

    private List<String> buildCommand(GitPullOptions options, String repository, String refspec) {
        List<String> command = new ArrayList<String>();
        command.add(JavaGitConfiguration.getGitCommand());
        command.add("pull");

        if (options != null) {
            // TODO: implement if more options are supported
        }

        command.add(repository);
        command.add(refspec);
        
        return command;
    }
}
