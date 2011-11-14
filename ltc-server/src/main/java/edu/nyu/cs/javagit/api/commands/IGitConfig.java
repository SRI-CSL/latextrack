/**
 ************************ 80 columns *******************************************
 * IGitConfig
 *
 * Created on Jul 27, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package edu.nyu.cs.javagit.api.commands;

import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.options.GitConfigOptions;

import java.io.File;

/**
 * An interface to the <code>git config</code> command.
 *
 * @author linda
 */
public interface IGitConfig {

    // TODO: add remaining sub-commands...

    public void configAdd(File repositoryPath, GitConfigOptions options, String name, String value)
            throws JavaGitException;

    public String configGet(File repositoryPath, GitConfigOptions options, String name)
            throws JavaGitException;

    public void configUnsetAll(File repositoryPath, GitConfigOptions options, String name)
            throws JavaGitException;
}
