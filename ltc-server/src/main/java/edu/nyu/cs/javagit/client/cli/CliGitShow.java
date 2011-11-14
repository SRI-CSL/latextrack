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
import edu.nyu.cs.javagit.api.commands.IGitShow;
import edu.nyu.cs.javagit.api.options.GitShowOptions;
import edu.nyu.cs.javagit.api.responses.AbstractResponse;
import edu.nyu.cs.javagit.api.responses.CommandResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command-line implementation of the <code>IGitShow</code> interface.
 */
public class CliGitShow implements IGitShow {

    public String show(File repositoryPath, GitShowOptions options, String... objects)
            throws JavaGitException {

        CommandResponse response = ProcessUtilities.runCommand(repositoryPath,
                buildCommand(options, objects),
                new AbstractParser<AbstractResponse>(new AbstractResponse() {}) {});

        if (response.isError())
            throw new JavaGitException("Git show error: " + response.getError());

        return response.getOutput();

    }

    private List<String> buildCommand(GitShowOptions options, String... objects) {
        List<String> command = new ArrayList<String>();
        command.add(JavaGitConfiguration.getGitCommand());
        command.add("show");
        if (options != null) {
            // TODO: include options
        }
        if (objects != null)
            command.addAll(Arrays.asList(objects));
        return command;
    }
}