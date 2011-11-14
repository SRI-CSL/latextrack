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
import edu.nyu.cs.javagit.api.commands.IGitCommit;
import edu.nyu.cs.javagit.api.options.GitCommitOptions;
import edu.nyu.cs.javagit.api.responses.GitCommitResponse;
import edu.nyu.cs.javagit.utilities.CheckUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command-line implementation of the <code>IGitCommit</code> interface.
 */
public class CliGitCommit implements IGitCommit {

    // patterns for parsing"
    private final static Pattern COMMIT_PATTERN = Pattern.compile("^\\s*\\[(\\w+) (\\p{Alnum}+)\\]\\s+(.*)\\s*$");
    //[master 885ce52] more files
    private final static Pattern STATUS_PATTERN = Pattern.compile("^\\s*(\\d+) files changed,\\s*(\\d+) insertions\\(\\+\\),\\s*(\\d+) deletions\\(-\\)\\s*$");
    // 3 files changed, 4 insertions(+), 1 deletions(-)
    private final static Pattern MODE_PATTERN = Pattern.compile("^\\s*(create|copy|delete|rename) ((mode (\\d+) (\\w+))|((\\w+) => (\\w+) \\((\\d+)%\\)))\\s*$");
    //  rename blub => bla (100%)
    //  create mode 100644 bla
    //  delete mode 100644 bla

    private static enum MODES {
        create, copy, delete, rename
    }

    public GitCommitResponse commitAll(File repository, String message) throws JavaGitException {
        GitCommitOptions options = new GitCommitOptions();
        options.setOptAll(true);
        return commit(repository, options, message, (String[]) null);
    }

    public GitCommitResponse commit(File repository, GitCommitOptions options, String message) throws JavaGitException {
        return commit(repository, options, message, (String[]) null);
    }

    public GitCommitResponse commitOnly(File repository, String message, String... paths) throws JavaGitException {
        GitCommitOptions options = new GitCommitOptions();
        options.setOptOnly(true);
        return commit(repository, options, message, paths);
    }

    public GitCommitResponse commit(File repository, GitCommitOptions options, String message, String... paths)
            throws JavaGitException {
        CheckUtilities.checkNullArgument(repository);
        CheckUtilities.checkStringArgument(message, "message");

        return ProcessUtilities.runCommand(repository,
                buildCommand(options, message, paths),
                new GitCommitParser(new GitCommitResponse(), repository.getAbsolutePath()));
    }

    /**
     * Builds a list of command arguments to pass to <code>ProcessBuilder</code>.
     *
     * @param options The options to include on the command line.
     * @param message The message for the commit.
     * @param paths
     * @return A list of the individual arguments to pass to <code>ProcessBuilder</code>.
     */
    protected List<String> buildCommand(GitCommitOptions options, String message, String... paths) {

        // TODO (jhl388): Add a unit test for this method (CliGitCommit.buildCommand()).

        List<String> cmd = new ArrayList<String>();
        cmd.add(JavaGitConfiguration.getGitCommand());
        cmd.add("commit");

        if (null != options) {
            if (options.isOptAll()) {
                cmd.add("-a");
            }
            if (options.isOptInclude()) {
                cmd.add("-i");
            }
            if (options.isOptNoVerify()) {
                cmd.add("--no-verify");
            }
            if (options.isOptOnly()) {
                cmd.add("-o");
            }
            if (options.isOptSignoff()) {
                cmd.add("-s");
            }
            String author = options.getAuthor();
            if (null != author && author.length() > 0) {
                cmd.add("--author");
                cmd.add(options.getAuthor());
            }
        }

        cmd.add("-m");
        cmd.add(message);

        if (null != paths) {
            cmd.add("--");
            cmd.addAll(Arrays.asList(paths));
        }

        return cmd;
    }

    private class GitCommitParser extends AbstractParser<GitCommitResponse> {

        // The working directory for the command that was run.
        private final String workingDirectory;

        public GitCommitParser(GitCommitResponse response, String workingDirectory) {
            super(response);
            this.workingDirectory = workingDirectory;
        }

        @Override
        public void parseLine(String line, String lineending) {
            if (line.trim().startsWith("#")) {
                response.appendComment(line + lineending);
                return;
            }

            if (response.isError() || line.trim().startsWith("error:") || line.trim().startsWith("fatal:")) {
                response.addError(line);
                return;
            }

            // try matching various line formats
            Matcher m;

            if ((m = COMMIT_PATTERN.matcher(line)).matches()) {
                String branch = m.group(1);
                response.setCommit(m.group(2), m.group(3));
                return;
            }

            if ((m = STATUS_PATTERN.matcher(line)).matches()) {
                response.setNumbers(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(3)), Integer.parseInt(m.group(2)));
                return;
            }

            if ((m = MODE_PATTERN.matcher(line)).matches()) {
                MODES mode = MODES.valueOf(m.group(1));
                switch (mode) {
                    case create:
                    case delete:
                        File path = new File(workingDirectory + File.separator + m.group(5));
                        String modeN = m.group(4);
                        if (MODES.create.equals(mode))
                            response.addAddedFile(path, modeN);
                        if (MODES.delete.equals(mode))
                            response.addDeletedFile(path, modeN);
                        break;
                    case copy:
                    case rename:
                        File path1 = new File(workingDirectory + File.separator + m.group(7));
                        File path2 = new File(workingDirectory + File.separator + m.group(8));
                        int percentage = Integer.parseInt(m.group(9));
                        if (MODES.rename.equals(mode))
                            response.addRenamedFile(path1, path2, percentage);
                        if (MODES.copy.equals(mode)) // TODO: create test case!
                            response.addCopiedFile(path1, path2, percentage);
                        break;
                }
                return;
            }

            response.addOutput(line + lineending);
        }
    }
}
