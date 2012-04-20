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
import edu.nyu.cs.javagit.api.commands.IGitLog;
import edu.nyu.cs.javagit.api.options.GitLogOptions;
import edu.nyu.cs.javagit.api.responses.GitLogResponse;
import edu.nyu.cs.javagit.utilities.CheckUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command-line implementation of the <code>IGitLog</code> interface.
 */
public class CliGitLog implements IGitLog {

    // patterns to match parsed lines against:
    // ---------------------------------------
    private final static String GRAPH_CHARS = "[\\s\\|/\\\\]*";
    // "* | commit 8e733e75aeea3e3481ca9048f4b552fb5c5aa92b"
    private final static Pattern COMMIT_PATTERN = Pattern.compile("^" + GRAPH_CHARS + "\\*" + GRAPH_CHARS + "commit (\\p{Alnum}+)\\s*$");
    // "|/  Author:     Jeff Haynie <jhaynie@appcelerator.com>"
    private final static Pattern AUTHOR_PATTERN = Pattern.compile("^" + GRAPH_CHARS + "Author:\\s*(.*)\\s*$");
    // "|   AuthorDate: Mon May 3 23:23:00 2010 -0700" or "| | Date:   Thu May 6 17:13:46 2010 -0700"
    private final static Pattern DATE_PATTERN = Pattern.compile("^" + GRAPH_CHARS + "(Author)?Date:\\s*(.*)\\s*$");
    // "| | Commit: Nolan Wright <nwright@appcelerator.com>"
    private final static Pattern COMMITTER_PATTERN = Pattern.compile("^" + GRAPH_CHARS + "Commit:\\s*(.*)\\s*$");
    // "| CommitDate: Thu May 6 23:47:04 2010 -0700"
    private final static Pattern COMMITTER_DATE_PATTERN = Pattern.compile("^" + GRAPH_CHARS + "CommitDate:\\s*(.*)\\s*$");
    // "|\  Merge: c08e51f 467d14c"
    private final static Pattern MERGE_PATTERN = Pattern.compile("^" + GRAPH_CHARS + "Merge:\\s*((\\p{Alnum}+\\s*)*)\\s*$");
    // "| | Parents: c08e51f 467d14c"
    private final static Pattern PARENTS_PATTERN = Pattern.compile("^" + GRAPH_CHARS + "Parents:\\s*((\\p{Alnum}+\\s*)*)\\s*$");
    // "| |     Merge branch 'master' of github.com:appcelerator/titanium_mobile"
    private final static Pattern MESSAGE_PATTERN = Pattern.compile("^" + GRAPH_CHARS + "(.*)\\s*$");

    /**
     * Implementations of &lt;git log&gt; with options and one file to be added to index.
     */
    public List<GitLogResponse.Commit> log(File repositoryPath, GitLogOptions options, String optionalPath)
            throws JavaGitException {
        CheckUtilities.checkFileValidity(repositoryPath);
        GitLogResponse response = ProcessUtilities.runCommand(repositoryPath,
                buildCommand(options, optionalPath),
                new GitLogParser(new GitLogResponse()));
        if (response.isError())
            throw new JavaGitException("Git log error: " + response.getError());
        return response.getLog();
    }

    /**
     * This function builds the git log commands with necessary options as specified by the user.
     *
     * @param options      Options supplied to the git log command using <code>GitLogOptions</code>.
     * @param optionalPath path of any specific files (optional)
     * @return Returns a List of command argument to be applied to git log.
     */
    private List<String> buildCommand(GitLogOptions options, String optionalPath) {
        List<String> command = new ArrayList<String>();
        command.add(JavaGitConfiguration.getGitCommand());
        command.add("log");
        if (options != null) {
            //General Options
            /*
             * Process any <since>..<until> settings
             */
            StringBuilder optSinceUntil = new StringBuilder(); // collect <since> and <until> in one string
            if (options.isOptSince())
                optSinceUntil.append(options.getOptSince() + "..");
            if (options.isOptUntil())
                optSinceUntil.append(options.getOptUntil());
            if (optSinceUntil.length() > 0)
                command.add(optSinceUntil.toString());

            if (options.isOptBreakRewriteChanges())
                command.add("-B");

            if (options.isOptDetectRenames())
                command.add("-M");

            if (options.isOptFindCopies())
                command.add("-C");

            if (options.isOptFindCopiesHarder())
                command.add("--find-copies-harder");

            if (options.isOptFileDetails())
                command.add("--numstat");

            if (options.isOptRelativePath())
                command.add("--relative=\"" + options.getOptRelativePath() + "\"");

            //Limiting Options

            if (options.isOptLimitSince())
                command.add("--since=\"" + options.getOptLimitSince() + "\"");

            if (options.isOptLimitAfter())
                command.add("--after=\"" + options.getOptLimitAfter() + "\"");

            if (options.isOptLimitUntil())
                command.add("--until=\"" + options.getOptLimitUntil() + "\"");

            if (options.isOptLimitBefore())
                command.add("--before=\"" + options.getOptLimitBefore() + "\"");

            if (options.isOptLimitAuthor())
                command.add("--author=\"" + options.getOptLimitAuthor() + "\"");

            if (options.isOptLimitCommitter())
                command.add("--committer=\"" + options.getOptLimitCommitter() + "\"");

            if (options.isOptLimitGrep())
                command.add("--grep=\"" + options.getOptLimitGrep() + "\"");

            if (options.isOptMatchIgnoreCase())
                command.add("-i");

            if (options.isOptEnableExtendedRegex())
                command.add("-E");

            if (options.isOptEnableFixedStrings())
                command.add("-F");

            if (options.isOptRemoveEmpty())
                command.add("--remove-empty");

            if (options.isOptLimitFullHistory())
                command.add("--full-history");

            if (options.isOptMerges())
                command.add("--merges");

            if (options.isOptNoMerges())
                command.add("--no-merges");

            if (options.isOptFirstParent())
                command.add("--first-parent");

            if (options.isOptAll())
                command.add("--all");

            if (options.isOptCherryPick())
                command.add("--cherry-pick");

            if (options.isOptLimitMax())
                command.add("-" + options.getOptLimitMax());

            if (options.isOptLimitSkip())
                command.add("--skip=" + options.getOptLimitSkip());

            // ordering

            if (options.isOptOrderingTopological())
                command.add("--topo-order");

            if (options.isOptOrderingDate())
                command.add("--date-order");

            if (options.isOptOrderingReverse())
                command.add("--reverse");

            // formatting

            if (options.isOptGraph())
                command.add("--graph");

            if (options.isOptFormatDate())
                command.add("--date=" + options.getOptFormatDate());

            if (options.isOptFormat())
                command.add("--format=format:" + options.getOptFormat());
        }

        if (optionalPath != null && !optionalPath.isEmpty()) {
            command.add("--");
            command.add(optionalPath);
        }

        return command;
    }

    /**
     * Parser class to parse the output generated by git log; and return a
     * <code>GitLogResponse</code> object.
     */
    private class GitLogParser extends AbstractParser<GitLogResponse> {

        private CommitBuilder builder = null;

        protected GitLogParser(GitLogResponse response) {
            super(response);
        }

        private void addPriorCommit() {
            if (builder != null)
                response.addCommit(
                        builder.sha1,
                        builder.merges,
                        builder.author,
                        builder.date,
                        builder.committer,
                        builder.committerDate,
                        builder.parents,
                        builder.message.toString()
                );
        }

        @Override
        public void processExitCode(int code) {
            super.processExitCode(code);
            // add last commit if any
            addPriorCommit();
        }

        @Override
        public void parseLine(String line, String lineending) {
            if ("".equals(line))
                return;

            // catch error output first:
            if (response.isError() || line.trim().startsWith("fatal:")) {
                response.addError(line);
                return;
            }

            // try matching various line formats
            Matcher m;

            if ((m = COMMIT_PATTERN.matcher(line)).matches()) {
                addPriorCommit();
                builder = new CommitBuilder(m.group(1));
                return;
            }

            if (builder != null && (m = AUTHOR_PATTERN.matcher(line)).matches()) {
                builder.author = m.group(1);
                return;
            }

            if (builder != null && (m = DATE_PATTERN.matcher(line)).matches()) {
                builder.date = m.group(2);
                return;
            }

            if (builder != null && (m = COMMITTER_PATTERN.matcher(line)).matches()) {
                builder.committer = m.group(1);
                return;
            }

            if (builder != null && (m = COMMITTER_DATE_PATTERN.matcher(line)).matches()) {
                builder.committerDate = m.group(1);
                return;
            }

            if (builder != null && (m = MERGE_PATTERN.matcher(line)).matches()) {
                String[] hashes = m.group(1).split("\\s+");
                builder.merges.addAll(Arrays.asList(hashes));
                return;
            }

            if (builder != null && (m = PARENTS_PATTERN.matcher(line)).matches()) {
                String[] parents = m.group(1).split("\\s+");
                builder.parents.addAll(Arrays.asList(parents));
                return;
            }

            if (builder != null && (m = MESSAGE_PATTERN.matcher(line)).matches()) {
                builder.message.append(m.group(1) + lineending);
            }
        }

        private class CommitBuilder {
            final String sha1;
            String author;
            String date;
            String committer;
            String committerDate;
            StringBuilder message = new StringBuilder();
            List<String> merges = new ArrayList<String>();
            List<String> parents = new ArrayList<String>();

            CommitBuilder(String sha1) {
                this.sha1 = sha1;
            }
        }
    }
}
