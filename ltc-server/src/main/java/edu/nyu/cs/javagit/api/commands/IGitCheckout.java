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
package edu.nyu.cs.javagit.api.commands;

import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.Ref;
import edu.nyu.cs.javagit.api.options.GitCheckoutOptions;
import edu.nyu.cs.javagit.api.responses.GitCheckoutResponse;

import java.io.File;
import java.io.IOException;

/**
 * An interface to represent the &lt;git-checkout&gt; command.
 * <p/>
 * <pre>
 * git checkout [-q] [-f] [-m] [<branch>]
 * git checkout [-q] [-f] [-m] [[-b|-B|--orphan] <new_branch>] [<start_point>]
 * git checkout [-f|--ours|--theirs|-m|--conflict=<style>] [<tree-ish>] [--] <paths>...
 * git checkout --patch [<tree-ish>] [--] [<paths>...]
 * </pre>
 */
public interface IGitCheckout {

    /**
     * Runs <code>git checkout [-q] [-f] [-m] [&lt;branch&gt;]</code>.
     *
     * @param repositoryPath Path to the root of the repository
     * @param options        Options for this call to git checkout
     * @return response of this call to git checkout
     * @throws java.io.IOException
     * @throws edu.nyu.cs.javagit.api.JavaGitException
     */
    public GitCheckoutResponse checkoutBranch(File repositoryPath, GitCheckoutOptions options, Ref branch)
            throws IOException, JavaGitException;

    /**
     * Runs <code>git checkout [-q] [-f] [-m] [[-b|-B|--orphan] &lt;new_branch&gt;] [&lt;start_point&gt;]</code>.
     *
     * @param repositoryPath Path to the root of the repository
     * @param options        Options for this call to git checkout
     * @return response of this call to git checkout
     * @throws java.io.IOException
     * @throws edu.nyu.cs.javagit.api.JavaGitException
     */
    public GitCheckoutResponse checkoutNewBranch(File repositoryPath, GitCheckoutOptions options,
                                                 String newBranch, String startPoint)
            throws IOException, JavaGitException;

    /**
     * Runs <code>git checkout [-f|--ours|--theirs|-m|--conflict=&lt;style&gt;] [&lt;tree-ish&gt;] [--] &lt;paths&gt;...</code>.
     *
     * @param repositoryPath Path to the root of the repository
     * @param options        Options for this call to git checkout
     * @return response of this call to git checkout
     * @throws java.io.IOException
     * @throws edu.nyu.cs.javagit.api.JavaGitException
     */
    public GitCheckoutResponse checkoutPaths(File repositoryPath, GitCheckoutOptions options,
                                             Ref treeish, String... paths)
            throws IOException, JavaGitException;

    /**
     * Runs <code>git checkout --patch [&lt;tree-ish&gt;] [--] [&lt;paths&gt;...]</code>.
     *
     * @param repositoryPath Path to the root of the repository
     * @param options        Options for this call to git checkout
     * @return response of this call to git checkout
     * @throws java.io.IOException
     * @throws edu.nyu.cs.javagit.api.JavaGitException
     */
    public GitCheckoutResponse checkoutPatch(File repositoryPath, Ref treeish, String... paths)
            throws IOException, JavaGitException;

//
//
//
//    /**
//     * Checks out either an existing branch or new branch from the repository.
//     *
//     * @param repositoryPath
//     *          Path to the root of the repository
//     * @param options
//     *          <code>GitCheckoutOptions</code> object used for passing options to
//     *          &lt;git-checkout&gt;
//     * @param branch
//     *          Name of the base branch that need to be checked out or if the new branch is being
//     *          checkout based on this base branch.
//     * @param paths
//     *          <code>List</code> of files that are specifically to be checked out.
//     * @return GitCheckoutResponse object
//     * @throws JavaGitException thrown if -
//     *           <ul>
//     *           <li>if options passed are not correct.</li>
//     *           <li>if the output for &lt;git-checkout&gt; command generated an error.</li>
//     *           <li>if processBuilder not able to run the command.</li>
//     *           </ul>
//     * @throws IOException thrown if -
//     *           <ul>
//     *           <li>paths given do not have proper permissions.</li>
//     *           <li>paths given do not exist at all.</li>
//     *           </ul>
//     */
//    public GitCheckoutResponse checkout(File repositoryPath, GitCheckoutOptions options, Ref branch,
//                                        List<File> paths) throws JavaGitException, IOException;
//
//    /**
//     * Checks out either an existing branch or new branch from the repository.
//     *
//     * @param repositoryPath
//     *          Path to the root of the repository
//     * @param options
//     *          <code>GitCheckoutOptions</code> object used for passing options to
//     *          &lt;git-checkout&gt;
//     * @param branch
//     *          Name of the base branch that need to be checked out or if the new branch is being
//     *          checkout based on this base branch.
//     * @return GitCheckoutResponse< object
//   * @throws JavaGitException thrown if -
//   *           <ul>
//     *           <li>if options passed are not correct.</li>
//     *           <li>if the output for &lt;git-checkout&gt; command generated an error.</li>
//     *           <li>if processBuilder not able to run the command.</li>
//     *           </ul>
//     * @throws IOException thrown if -
//     *           <ul>
//     *           <li>paths given do not have proper permissions.</li>
//     *           <li>paths given do not exist at all.</li>
//     *           </ul>
//     */
//    public GitCheckoutResponse checkout(File repositoryPath, GitCheckoutOptions options, Ref branch)
//            throws JavaGitException, IOException;
//
//    /**
//     * &lt;git-checkout&gt; where a list of files is given to be checked out with tree-ish option set.
//     *
//     * @param repositoryPath
//     *          path to the root of the repository
//     * @param treeIsh
//     *          RefType object
//     * @param paths
//     *          List of files to be checked out.
//     * @return GitCheckoutResponse object
//     * @throws JavaGitException thrown if -
//     *           <li>if the output for &lt;git-checkout&gt; command generated an error.</li>
//     *           <li>if processBuilder not able to run the command.</li>
//     *           </ul>
//     * @throws IOException thrown if -
//     *           <ul>
//     *           <li>paths given do not have proper permissions.</li>
//     *           <li>paths given do not exist at all.</li>
//     *           </ul>
//     */
//    public GitCheckoutResponse checkout(File repositoryPath, Ref treeIsh, List<File> paths)
//            throws JavaGitException, IOException;
}
