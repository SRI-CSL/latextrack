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

import edu.nyu.cs.javagit.api.GitFileSystemObject;
import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.options.GitStatusOptions;
import edu.nyu.cs.javagit.api.responses.GitStatusResponse;

import java.io.File;

/**
 * An interface to represent the &lt;git-status&gt; command.
 */
public interface IGitStatus {

    /**
     * This method returns <code>GitStatusResponse</code> object after parsing the options and then
     * executing the &lt;git-status&gt; command.
     *
     * @param repositoryPath Path to the root of the repository
     * @param options        <code>GitStatusOptions</code> Options passed to &lt;git-status&gt; command
     * @param relativePaths
     * @return <code>GitStatusResponse</code> Response object returned by &lt;git-status&gt;
     *         command.
     * @throws <code>JavaGitException</code> Thrown when there is an error while running the
     *                                       &lt;git-status&gt; command.
     * @throws <code>IOException</code>      There are many reasons for which an <code>IOException</code>
     *                                       may be thrown. Examples include:
     *                                       <ul>
     *                                       <li>a directory doesn't exist</li>
     *                                       <li>access to a file is denied</li>
     *                                       </ul>
     */
    public GitStatusResponse status(File repositoryPath, GitStatusOptions options, String... relativePaths)
            throws JavaGitException;

    /**
     * Return status for a single <code>File</code>
     *
     * @param repositoryPath Directory path to the root of the repository.
     * @param options        Options that are passed to &lt;git-status&gt; command.
     * @param relativePath
     * @return <code>GitStatusResponse</code> object
     * @throws edu.nyu.cs.javagit.api.JavaGitException Exception thrown if the repositoryPath is null
     * @throws java.io.IOException      Exception is thrown if any of the IO operations fail.
     */
    public GitFileSystemObject.Status statusSingleFile(File repositoryPath, GitStatusOptions options, String relativePath)
            throws JavaGitException;

}
