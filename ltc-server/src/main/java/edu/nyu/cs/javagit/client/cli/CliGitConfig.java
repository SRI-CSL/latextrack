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
import edu.nyu.cs.javagit.api.commands.IGitConfig;
import edu.nyu.cs.javagit.api.options.GitConfigOptions;
import edu.nyu.cs.javagit.api.responses.GitConfigResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command-line implementation of the <code>IGitConfig</code> interface.
 */
public class CliGitConfig implements IGitConfig {

    public void configAdd(File repositoryPath, GitConfigOptions options, String name, String value)
            throws JavaGitException {
        GitConfigResponse response = runCommand(repositoryPath, options, "--add", name, value);
        if (response.isError())
            throw new JavaGitException("Error running git config --add: " + response.getError());
    }

    public String configGet(File repositoryPath, GitConfigOptions options, String name)
            throws JavaGitException {
        GitConfigResponse response = runCommand(repositoryPath, options, "--get", name);
        if (response.isError())
            throw new JavaGitException("Error running git config --get: " + response.getError());
        return response.getOutputEntry(0);
    }

    public void configUnsetAll(File repositoryPath, GitConfigOptions options, String name)
            throws JavaGitException {
        GitConfigResponse response = runCommand(repositoryPath, options, "--unset-all", name);
        if (response.isError() && response.getErrorCode() != 5) // ignore error code 5
            throw new JavaGitException("Error running git config --unset-all: " + response.getError());
    }

    private GitConfigResponse runCommand(File repositoryPath, GitConfigOptions options,
                                         String subcommand, String... parameters)
            throws JavaGitException {

        return ProcessUtilities.runCommand(repositoryPath,
                buildCommand(options, subcommand, parameters),
                new GitConfigParser(new GitConfigResponse()));
    }

    private List<String> buildCommand(GitConfigOptions options, String subcommand, String... parameters) {
        List<String> command = new ArrayList<String>();
        command.add(JavaGitConfiguration.getGitCommand());
        command.add("config");
        if (options != null) {
            // TODO: include options before sub-command
        }
        command.add(subcommand);
        if (parameters != null)
            command.addAll(Arrays.asList(parameters));
        if (options != null) {
            // TODO: include options after sub-command
        }
        return command;
    }

    private class GitConfigParser extends AbstractParser<GitConfigResponse> {

        protected GitConfigParser(GitConfigResponse response) {
            super(response);
        }

        @Override
        public void parseLine(String line, String lineending) {
            response.addOutputEntry(line);
        }

        @Override
        public void processExitCode(int code) {
            super.processExitCode(code);

            String message;
            switch (code) {
                case 0:
                    message = "";
                    break;
                case 1:
                    message = "Key not found";
                    break;
                case 2:
                    message = "Multiple keys found";
                    break;
                default:
                    message = "Unkown error occurred";
            }
            response.setErrorDetails(code, message);
        }
    }
}
