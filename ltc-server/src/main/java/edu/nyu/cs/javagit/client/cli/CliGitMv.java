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
import edu.nyu.cs.javagit.api.commands.IGitMv;
import edu.nyu.cs.javagit.api.options.GitMvOptions;
import edu.nyu.cs.javagit.api.responses.GitMvResponse;
import edu.nyu.cs.javagit.utilities.CheckUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command-line implementation of the <code>git mv</code> command.
 * <p/>
 * <pre>
 * git mv [-f] [-n] <source> <destination>
 * git mv [-f] [-n] [-k] <source> ... <destination directory>
 * </pre>
 */
public class CliGitMv implements IGitMv {

    @Override
    public GitMvResponse mvFile(File repositoryPath, GitMvOptions options, String source, String destination)
            throws JavaGitException {
        CheckUtilities.checkNullArgument(repositoryPath);
        CheckUtilities.checkNullArgument(source);
        CheckUtilities.checkNullArgument(destination);

        // test that not option -k is set
        if (options != null && options.isOptK())
            throw new JavaGitException("Cannot call git-mv with option -k when moving single file.");

        GitMvResponse response = ProcessUtilities.runCommand(repositoryPath,
                buildCommand(options, destination, source),
                new GitMvParser(new GitMvResponse()));
        if (response.isError())
            throw new JavaGitException("An error occurred while running git-mv: " + response.getError());
        return response;
    }

    @Override
    public GitMvResponse mvToDir(File repositoryPath, GitMvOptions options, String destinationDir, String... paths)
            throws JavaGitException {
        CheckUtilities.checkNullArgument(repositoryPath);
        CheckUtilities.checkNullArgument(destinationDir);
        CheckUtilities.checkNullArgument(paths);

        GitMvResponse response = ProcessUtilities.runCommand(repositoryPath,
                buildCommand(options, destinationDir, paths),
                new GitMvParser(new GitMvResponse()));
        if (response.isError())
            throw new JavaGitException("An error occurred while running git-mv: " + response.getError());
        return response;
    }

    private List<String> buildCommand(GitMvOptions options, String destination, String... sources) {
        List<String> cmd = new ArrayList<String>();

        cmd.add(JavaGitConfiguration.getGitCommand());
        cmd.add("mv");

        if (null != options) {
            cmd.addAll(Arrays.asList(options.toString().split("\\s+")));
        }

        cmd.addAll(Arrays.asList(sources));
        cmd.add(destination);

        return cmd;
    }

    private class GitMvParser extends AbstractParser<GitMvResponse> {

        protected GitMvParser(GitMvResponse response) {
            super(response);
        }

        @Override
        public void parseLine(String line, String lineending) {
            // catch error output first:
            if (response.isError() || line.trim().startsWith("fatal") || line.trim().startsWith("error")) {
                response.addError(line);
                return;
            }

            if (line.contains("Warning:")) {
                response.addComment(line);
            }
            if (line.contains("Adding") || line.contains("Changed")) {
                response.setDestination(new File(line.substring(11)));
            }
            if (line.contains("Deleting")) {
                response.setSource(new File(line.substring(11)));
            }
        }
    }
}
