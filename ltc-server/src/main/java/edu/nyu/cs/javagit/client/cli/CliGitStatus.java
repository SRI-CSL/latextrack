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

import edu.nyu.cs.javagit.api.GitFileSystemObject;
import edu.nyu.cs.javagit.api.JavaGitConfiguration;
import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.commands.IGitStatus;
import edu.nyu.cs.javagit.api.options.GitStatusOptions;
import edu.nyu.cs.javagit.api.responses.GitStatusResponse;
import edu.nyu.cs.javagit.utilities.CheckUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command-line implementation of the <code>IGitStatus</code> interface.
 */
public class CliGitStatus implements IGitStatus {

    public GitStatusResponse status(File repositoryPath, GitStatusOptions options, String... relativePaths)
            throws JavaGitException {
        CheckUtilities.checkNullArgument(repositoryPath);
        CheckUtilities.checkFileValidity(repositoryPath);

        GitStatusResponse response = ProcessUtilities.runCommand(repositoryPath,
                buildCommandLine(options, relativePaths),
                new GitStatusParser(new GitStatusResponse()));

        if (response.isError())
            throw new JavaGitException("Error running git status: " + response.getError());

        // go through relative paths and add them as unmodified, if they are not in response
        if (relativePaths != null)
            for (String relativePath : relativePaths) {
                if (!response.containsAt(relativePath, 0) && !response.containsAt(relativePath, 1))
                    response.addEntry(relativePath, null, GitFileSystemObject.Status.IN_REPOSITORY);
            }

        return response;
    }

    public GitFileSystemObject.Status statusSingleFile(File repositoryPath, GitStatusOptions options, String relativePath)
            throws JavaGitException {
        return status(repositoryPath, options, relativePath).getAt(relativePath, 0);
    }

    private List<String> buildCommandLine(GitStatusOptions options, String... paths) {
        List<String> command = new ArrayList<String>();

        command.add(JavaGitConfiguration.getGitCommand());
        command.add("status");

        if (options != null) {
            if (options.isBranch())
                command.add("-b");
        }

        command.add("--porcelain"); // makes parsing so much easier

        if (paths != null) {
            command.add("--");
            command.addAll(Arrays.asList(paths));
        }

        return command;
    }

    private static class GitStatusParser extends AbstractParser<GitStatusResponse> {

        // parse something like:
        //  R hello.txt -> "howdie text"
        private static Pattern STATUS_PATTERN =
                Pattern.compile("^\\s?([\\?MADRCU]{0,2})\\s+(\"([^\"]+)\"|(\\S+))(\\s+->\\s+(\"([^\"]+)\"|(\\S+)))?\\s*$");

        int lines = 0;

        private GitStatusParser(GitStatusResponse response) {
            super(response);
        }

        @Override
        public void parseLine(String line, String lineending) {
            lines++;

            // TODO: need to parse branch info if -b given
            Matcher m = STATUS_PATTERN.matcher(line);

            // catch error output first:
            if (response.isError() || !m.matches()) {
                response.addError(line);
                return;
            }

            response.addEntry(
                    getNonNullIfExists(m.group(3), m.group(4)),
                    getNonNullIfExists(m.group(7), m.group(8)),
                    GitFileSystemObject.Status.parseCode("" + m.group(1).charAt(0))
            );
        }

        private String getNonNullIfExists(String s1, String s2) {
            if (s1 == null && s2 == null)
                return null;
            if (s1 != null)
                return s1;
            return s2;
        }
    }
}
