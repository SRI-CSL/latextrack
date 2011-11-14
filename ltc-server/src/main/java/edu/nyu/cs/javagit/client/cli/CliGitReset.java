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
import edu.nyu.cs.javagit.api.Ref;
import edu.nyu.cs.javagit.api.commands.IGitReset;
import edu.nyu.cs.javagit.api.options.GitResetOptions;
import edu.nyu.cs.javagit.api.options.GitResetOptions.ResetType;
import edu.nyu.cs.javagit.api.responses.GitResetResponse;
import edu.nyu.cs.javagit.utilities.CheckUtilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command-line implementation of the <code>IGitReset</code> interface.
 */
public class CliGitReset implements IGitReset {

    @Override
    public GitResetResponse reset(File repository, GitResetOptions options, String... paths)
            throws IOException, JavaGitException {
        CheckUtilities.checkNullArgument(repository);
        CheckUtilities.checkNullArgument(paths);

        return resetProcessor(repository, options, paths);
    }

    @Override
    public GitResetResponse resetPatch(File repository, GitResetOptions options, String... paths)
            throws IOException, JavaGitException {
        CheckUtilities.checkNullArgument(repository);

        if (options == null)
            options = new GitResetOptions();
        options.setPatch(true);

        return resetProcessor(repository, options, paths);
    }

    @Override
    public GitResetResponse resetMode(File repository, GitResetOptions options, ResetType mode)
            throws IOException, JavaGitException {
        CheckUtilities.checkNullArgument(repository);

        if (options == null)
            options = new GitResetOptions();
        options.setResetType(mode);

        return resetProcessor(repository, options, (String[]) null);
    }

    private GitResetResponse resetProcessor(File repository, GitResetOptions options, String... paths)
            throws IOException, JavaGitException {
        // TODO: check for error and throw exception
        return ProcessUtilities.runCommand(repository,
                buildCommand(options, paths),
                new GitResetParser(new GitResetResponse(), repository.getPath()));
    }

    private List<String> buildCommand(GitResetOptions options, String... paths) {
        List<String> cmd = new ArrayList<String>();
        cmd.add(JavaGitConfiguration.getGitCommand());
        cmd.add("reset");

        if (null != options) {
            if (options.getResetType() != null)
                cmd.add(options.getResetType().toString());

            if (options.isPatch())
                cmd.add("--patch");

            if (options.isQuiet()) {
                cmd.add("-q");
            }

            if (options.getCommitName() != null)
                cmd.add(options.getCommitName().toString());
        }

        if (null != paths) {
            cmd.add("--");
            cmd.addAll(Arrays.asList(paths));
        }

        return cmd;
    }

    public class GitResetParser extends AbstractParser<GitResetResponse> {

        // TODO (jhl388): Finish implementing the GitResetParser.

        // The index of the start of the short SHA1 in the HEAD record. Result of the --hard option
        private final int HEAD_RECORD_SHA1_START = 15;

        /*
        * The working directory path set for the command line. Used to generate the correct paths to
        * the files needing update.
        */
        private String workingDirectoryPath;

        // Track the number of lines parsed.
        private int numLinesParsed = 0;

        public GitResetParser(GitResetResponse response, String workingDirectoryPath) {
            super(response);
            this.workingDirectoryPath = workingDirectoryPath;
        }

        @Override
        public void parseLine(String line, String lineending) {

            // catch error output first:
            if (response.isError() || line.trim().startsWith("fatal") || line.trim().startsWith("error")) {
                response.addError(line);
                return;
            }

            if (line.startsWith("HEAD ")) {
                // A record indicating the new HEAD commit resulting from using the --hard option.
                int sha1End = line.indexOf(' ', HEAD_RECORD_SHA1_START);
                Ref sha1 = Ref.createSha1Ref(line.substring(HEAD_RECORD_SHA1_START, sha1End));
            } else if (numLinesParsed > 0 && response.getNewHeadSha1() != null) {
                // No line is expected after getting a HEAD record. Doing nothing for now. Must revisit.

                // TODO (jhl388): Figure out what to do if a line is received after a HEAD record.
            } else if (line.endsWith(": needs update")) {
                // A file needs update record.
                int lastColon = line.lastIndexOf(":");
                File f = new File(workingDirectoryPath + line.substring(0, lastColon));
                response.addFileToFilesNeedingUpdateList(f);
            } else {
                response.addError(line);
            }

            ++numLinesParsed;
        }
    }
}
