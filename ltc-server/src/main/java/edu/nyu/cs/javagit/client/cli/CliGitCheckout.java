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
import edu.nyu.cs.javagit.api.Ref.RefType;
import edu.nyu.cs.javagit.api.commands.IGitCheckout;
import edu.nyu.cs.javagit.api.options.GitCheckoutOptions;
import edu.nyu.cs.javagit.api.responses.GitCheckoutResponse;
import edu.nyu.cs.javagit.utilities.CheckUtilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Command-line implementation of the <code>IGitCheckout</code> interface.
 */
public class CliGitCheckout implements IGitCheckout {

    /**
     * String pattern for matching files with modified, deleted, added words in the output.
     */
    private enum Pattern {
        MODIFIED("^M\\s+\\w+"), DELETED("^D\\s+\\w+"), ADDED("^A\\s+\\w+");

        String pattern;

        private Pattern(String pattern) {
            this.pattern = pattern;
        }

        public boolean matches(String line) {
            return line.matches(pattern);
        }
    }

    /**
     * Runs <code>git checkout [-q] [-f] [-m] [&lt;branch&gt;]</code>.
     */
    @Override
    public GitCheckoutResponse checkoutBranch(File repositoryPath, GitCheckoutOptions options, Ref branch)
            throws IOException, JavaGitException {
        CheckUtilities.checkFileValidity(repositoryPath);
        if (branch != null && !RefType.BRANCH.equals(branch))
            throw new JavaGitException("Error running git checkout: Given reference is not a branch");

        // TODO: check that no other options than allowed are set?

        GitCheckoutResponse response = ProcessUtilities.runCommand(repositoryPath,
                buildCommand(options, branch.getName(), null),
                new GitCheckoutParser(new GitCheckoutResponse()));
        if (response.isError())
            throw new JavaGitException("Error running git checkout: " + response.getError());

        return response;
    }

    /**
     * Runs <code>git checkout [-q] [-f] [-m] [[-b|-B|--orphan] &lt;new_branch&gt;] [&lt;start_point&gt;]</code>.
     */
    @Override
    public GitCheckoutResponse checkoutNewBranch(File repositoryPath, GitCheckoutOptions options, String newBranch, String startPoint)
            throws IOException, JavaGitException {
        CheckUtilities.checkFileValidity(repositoryPath);
        CheckUtilities.checkNullArgument(newBranch);

        // TODO: check that no other options than allowed are set?

        GitCheckoutResponse response = ProcessUtilities.runCommand(repositoryPath,
                buildCommand(options, newBranch, startPoint),
                new GitCheckoutParser(new GitCheckoutResponse()));
        if (response.isError())
            throw new JavaGitException("Error running git checkout: " + response.getError());

        return response;
    }

    /**
     * Runs <code>git checkout [-f|--ours|--theirs|-m|--conflict=&lt;style&gt;] [&lt;tree-ish&gt;] [--] &lt;paths&gt;...</code>.
     */
    @Override
    public GitCheckoutResponse checkoutPaths(File repositoryPath, GitCheckoutOptions options, Ref treeish, String... paths)
            throws IOException, JavaGitException {
        CheckUtilities.checkFileValidity(repositoryPath);
        CheckUtilities.checkNullArgument(paths);

        // TODO: check that no other options than allowed are set?

        GitCheckoutResponse response = ProcessUtilities.runCommand(repositoryPath,
                buildCommand(options, treeish, paths),
                new GitCheckoutParser(new GitCheckoutResponse()));
        if (response.isError())
            throw new JavaGitException("Error running git checkout: " + response.getError());

        return response;
    }

    /**
     * Runs <code>git checkout --patch [&lt;tree-ish&gt;] [--] [&lt;paths&gt;...]</code>.
     */
    @Override
    public GitCheckoutResponse checkoutPatch(File repositoryPath, Ref treeish, String... paths)
            throws IOException, JavaGitException {
        CheckUtilities.checkFileValidity(repositoryPath);

        // TODO: check that no other options than allowed are set?

        GitCheckoutOptions options = new GitCheckoutOptions();
        options.setOptPatch(true);
        GitCheckoutResponse response = ProcessUtilities.runCommand(repositoryPath,
                buildCommand(options, treeish, paths),
                new GitCheckoutParser(new GitCheckoutResponse()));
        if (response.isError())
            throw new JavaGitException("Error running git checkout: " + response.getError());

        return null;
    }

    //    /**
//     * Git checkout with options and base branch information provided to &lt;git-checkout&gt; command.
//     */
//    public GitCheckoutResponse checkout(File repositoryPath, GitCheckoutOptions options, Ref ref)
//            throws JavaGitException, IOException {
//        CheckUtilities.checkFileValidity(repositoryPath);
//        checkRefAgainstRefType(ref, RefType.HEAD);
//        List<String> command = buildCommand(options, ref);
//        GitCheckoutParser parser = new GitCheckoutParser();
//        GitCheckoutResponse response = ProcessUtilities.runCommand(repositoryPath,
//                command, parser);
//        return response;
//    }
//
//    /**
//     * Checks out a list of file from repository, with &lt;tree-ish&gt; options provided.
//     */
//    public GitCheckoutResponse checkout(File repositoryPath, GitCheckoutOptions options, Ref ref, List<File> paths)
//            throws JavaGitException, IOException {
//        CheckUtilities.checkFileValidity(repositoryPath);
//
//        if (ref != null && RefType.HEAD.equals(ref.getRefType()))
//            throw new IllegalArgumentException("Invalid ref type passed as argument to checkout");
//
//        return (GitCheckoutResponse) ProcessUtilities.runCommand(repositoryPath,
//                buildCommand(options, ref, paths),
//                new GitCheckoutParser(new GitCheckoutResponse()));
//    }
//
//    /**
//     * Checks out a list of files from a given branch
//     */
//    public GitCheckoutResponse checkout(File repositoryPath, Ref branch, List<File> paths)
//            throws JavaGitException, IOException {
//        CheckUtilities.checkFileValidity(repositoryPath);
//        GitCheckoutParser parser = new GitCheckoutParser();
//        List<String> command = buildCommand(null, branch, paths);
//        GitCheckoutResponse response = (GitCheckoutResponse) ProcessUtilities.runCommand(repositoryPath,
//                command, parser);
//        return response;
//    }

    private List<String> buildCommand(GitCheckoutOptions options, Ref treeIsh, String... paths)
            throws JavaGitException {
        List<String> command = new ArrayList<String>();
        command.add(JavaGitConfiguration.getGitCommand());
        command.add("checkout");
        if (options != null) {
            processOptions(command, options);
        }
        if (treeIsh != null) {
            command.add(treeIsh.getName());
        }
        if (paths != null) {
            command.add("--");
            command.addAll(Arrays.asList(paths));
        }
        return command;
    }

    private List<String> buildCommand(GitCheckoutOptions options, String branchName, String startPoint) throws JavaGitException {
        List<String> command = new ArrayList<String>();
        command.add(JavaGitConfiguration.getGitCommand());
        command.add("checkout");
        if (options != null) {
            processOptions(command, options);
        }
        if (branchName != null) {
            command.add(branchName);
        }
        if (startPoint != null) {
            command.add(startPoint);
        }
        return command;
    }

    private void processOptions(List<String> command, GitCheckoutOptions options)
            throws JavaGitException {
        if (options.getOptQ())
            command.add("-q");

        if (options.getOptF())
            command.add("-f");

        // --track and --no-track options are valid only with -b option
        Ref newBranch;
        if ((newBranch = options.getOptB()) != null) {
            if (options.getOptNoTrack() && options.getOptTrack()) {
                throw new JavaGitException("Both --notrack and --track options are set");
            }
            if (options.getOptTrack())
                command.add("--track");
            if (options.getOptNoTrack())
                command.add("--no-track");
            command.add("-b");
            command.add(newBranch.getName());
        }

        if (options.getOptL())
            command.add("-l");

        if (options.getOptM())
            command.add("-m");

        if (options.isOptPatch())
            command.add("--patch");
    }

    private static class GitCheckoutParser extends AbstractParser<GitCheckoutResponse> {

        private int lineNum = 0;

        private GitCheckoutParser(GitCheckoutResponse response) {
            super(response);
        }

        @Override
        public void parseLine(String line, String lineending) {
            if (line.length() == 0)
                return;

            ++lineNum;

            // catch error output first:
            if (response.isError() || line.trim().startsWith("fatal") || line.trim().startsWith("error")) {
                response.addError(line);
                return;
            }

            parseSwitchedToBranchLine(line);
            parseFilesInfo(line);
        }

        public void parseSwitchedToBranchLine(String line) {
            if (line.startsWith("Switched to branch")) {
                getSwitchedToBranch(line);
            } else if (line.startsWith("Switched to a new branch")) {
                getSwitchedToNewBranch(line);
            }
        }

        private void getSwitchedToBranch(String line) {
            String branchName = extractBranchName(line);
            Ref branch = Ref.createBranchRef(branchName);
            response.setBranch(branch);
        }

        private void getSwitchedToNewBranch(String line) {
            String newBranchName = extractBranchName(line);
            Ref newBranch = Ref.createBranchRef(newBranchName);
            response.setNewBranch(newBranch);
        }

        private String extractBranchName(String line) {
            int startIndex = line.indexOf('"');
            int endIndex = line.indexOf('"', startIndex + 1);
            return line.substring(startIndex, endIndex + 1);
        }

        private void parseFilesInfo(String line) {
            if (Pattern.MODIFIED.matches(line)) {
                File file = new File(extractFileName(line));
                response.addModifiedFile(file);
                return;
            }
            if (Pattern.DELETED.matches(line)) {
                File file = new File(extractFileName(line));
                response.addDeletedFile(file);
                return;
            }
            if (Pattern.ADDED.matches(line)) {
                File file = new File(extractFileName(line));
                response.addAddedFile(file);
            }
        }

        private String extractFileName(String line) {
            String filename = null;
            Scanner scanner = new Scanner(line);
            while (scanner.hasNext()) {
                filename = scanner.next();
            }
            return filename;
        }
    }

}
