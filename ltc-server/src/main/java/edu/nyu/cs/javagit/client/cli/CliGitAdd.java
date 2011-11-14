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
import edu.nyu.cs.javagit.api.commands.IGitAdd;
import edu.nyu.cs.javagit.api.options.GitAddOptions;
import edu.nyu.cs.javagit.api.responses.GitAddResponse;
import edu.nyu.cs.javagit.utilities.CheckUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command-line implementation of the <code>IGitAdd</code> interface.
 * <p/>
 * TODO (gsd216) - to implement exception chaining.
 */
public class CliGitAdd implements IGitAdd {

    // add 'A/C/foo'
    private final static Pattern ADD_PATTERN = Pattern.compile("^add '(.+)'");

    /**
     * Implementations of &lt;git-add&gt; with options and list of files provided.
     */
    public GitAddResponse add(File repositoryPath, GitAddOptions options, String... pathPatterns)
            throws JavaGitException {
        CheckUtilities.checkFileValidity(repositoryPath);
        return ProcessUtilities.runCommand(repositoryPath,
                buildCommand(options, pathPatterns),
                new GitAddParser(new GitAddResponse()));
    }

    private List<String> buildCommand(GitAddOptions options, String... paths) {
        List<String> command = new ArrayList<String>();
        command.add(JavaGitConfiguration.getGitCommand());
        command.add("add");
        if (options != null) {
            if (options.dryRun()) {
                command.add("-n");
            }
            if (options.verbose()) {
                command.add("-v");
            }
            if (options.force()) {
                command.add("-f");
            }
            if (options.update()) {
                command.add("-u");
            }
            if (options.refresh()) {
                command.add("--refresh");
            }
            if (options.ignoreErrors()) {
                command.add("--ignore-errors");
            }
        }

        if (paths != null) {
            command.add("--");
            command.addAll(Arrays.asList(paths));
        }
        return command;
    }

    private class GitAddParser extends AbstractParser<GitAddResponse> {

        protected GitAddParser(GitAddResponse response) {
            super(response);
        }

        @Override
        public void parseLine(String line, String lineending) {
            super.parseLine(line, lineending);

            Matcher m = ADD_PATTERN.matcher(line);
            if (m.matches())
                response.addFile(m.group(1));
        }
    }
}
