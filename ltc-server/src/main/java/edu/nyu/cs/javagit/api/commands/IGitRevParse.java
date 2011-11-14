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
import edu.nyu.cs.javagit.api.options.GitRevParseOptions;

import java.io.File;
import java.util.List;

/**
 * An interface to the <code>git rev-parse</code> command.
 *
 * @author linda
 */
public interface IGitRevParse {

    public List<String> revParse(File repositoryPath, GitRevParseOptions options, String... arguments)
            throws JavaGitException;

}
