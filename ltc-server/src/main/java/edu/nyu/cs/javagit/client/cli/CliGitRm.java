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
import edu.nyu.cs.javagit.api.commands.IGitRm;
import edu.nyu.cs.javagit.api.options.GitRmOptions;
import edu.nyu.cs.javagit.api.responses.GitRmResponse;
import edu.nyu.cs.javagit.utilities.CheckUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command-line implementation of the <code>IGitRm</code> interface.
 */
public final class CliGitRm implements IGitRm {

    @Override
    public GitRmResponse rm(File repository, GitRmOptions options, String... paths)
            throws JavaGitException {
        CheckUtilities.checkNullArgument(repository);
        CheckUtilities.checkNullArgument(paths);

        return ProcessUtilities.runCommand(repository,
                buildCommandLine(options, paths),
                new GitRmParser(new GitRmResponse()));
    }

    private List<String> buildCommandLine(GitRmOptions options, String... paths) {
        List<String> cmdline = new ArrayList<String>();

        cmdline.add(JavaGitConfiguration.getGitCommand());
        cmdline.add("rm");

        if (null != options) {
            cmdline.addAll(Arrays.asList(options.toString().split("\\s+")));
        }

        cmdline.addAll(Arrays.asList(paths));

        return cmdline;
    }

    private class GitRmParser extends AbstractParser<GitRmResponse> {

        GitRmParser(GitRmResponse response) {
            super(response);
        }

        @Override
        public void parseLine(String line, String lineending) {
            // TODO: test parsing...
            if (response.isError()) {
                response.addError(line);
                return;
            }

            if (line.startsWith("rm '")) {
                int locQuote = line.indexOf('\'');
                int locLastQuote = line.lastIndexOf('\'');
                response.addFileToRemovedFilesList(new File(line.substring(locQuote + 1, locLastQuote)));
            } else {
                response.addError(line);
            }
        }
    }

}
