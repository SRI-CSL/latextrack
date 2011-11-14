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
import edu.nyu.cs.javagit.api.commands.IGitRevParse;
import edu.nyu.cs.javagit.api.options.GitRevParseOptions;
import edu.nyu.cs.javagit.api.responses.GitRevParseResponse;
import edu.nyu.cs.javagit.utilities.CheckUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command-line implementation of the <code>IGitRevParse</code> interface.
 */
public class CliGitRevParse implements IGitRevParse {

    public List<String> revParse(File repositoryPath, GitRevParseOptions options, String... arguments)
            throws JavaGitException {
        CheckUtilities.checkFileValidity(repositoryPath);

        GitRevParseResponse response = ProcessUtilities.runCommand(repositoryPath,
                buildCommand(options, arguments),
                new GitRevParseParser(new GitRevParseResponse()));
        if (response.isError())
            throw new JavaGitException("Git rev-parse error: " + response.getError());

        return response.getOutputList();
    }

    /**
     * This function builds the git rev-parse commands with necessary options as specified by the user.
     *
     * @param options Options supplied to the git rev-parse command.
     * @return Returns a list of commands to be applied to git rev-parse.
     */
    private List<String> buildCommand(GitRevParseOptions options, String... arguments) {
        List<String> command = new ArrayList<String>();
        command.add(JavaGitConfiguration.getGitCommand());
        command.add("rev-parse");
        if (options != null) {
            if (options.isVerify())
                command.add("--verify");

        }

        if (arguments != null)
            command.addAll(Arrays.asList(arguments));

        return command;

    }

    private class GitRevParseParser extends AbstractParser<GitRevParseResponse> {

        public GitRevParseParser(GitRevParseResponse response) {
            super(response);
        }

        @Override
        public void parseLine(String line, String lineending) {
            if (line.length() == 0)
                return;

            // catch error output first:
            if (response.isError() || line.trim().startsWith("fatal:")) {
                response.addError(line);
                return;
            }

            response.addOutput(line);
        }
    }
}
