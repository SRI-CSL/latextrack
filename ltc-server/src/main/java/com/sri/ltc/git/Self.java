/**
 ************************ 80 columns *******************************************
 * Self
 *
 * Created on Aug 11, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.git;

import com.sri.ltc.filter.Author;
import edu.nyu.cs.javagit.api.GitFile;
import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.commands.IGitConfig;
import edu.nyu.cs.javagit.client.Factory;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author linda
 */
public final class Self {

    private final static Logger LOGGER = Logger.getLogger(Self.class.getName());
    private final File workingTree;
    private final IGitConfig config = Factory.createGitConfig();

    public Self(GitFile gitFile) {
        if (gitFile == null)
            throw new IllegalArgumentException("Cannot create Self with NULL as git file");
        this.workingTree = gitFile.getWorkingTree().getPath();
    }

    public Author getSelf() throws IOException, JavaGitException {
        Author author = new Author(
                config.configGet(workingTree, null, "user.name"),
                config.configGet(workingTree, null, "user.email"),
                null);
        LOGGER.fine("Obtained current author \""+author.name+"\"");
        return author;
    }

    public void setSelf(Author author) throws IOException, JavaGitException {
        resetSelf();
        if (author != null) {
            config.configAdd(workingTree, null, "user.name", author.name);
            config.configAdd(workingTree, null, "user.email", author.email);
            LOGGER.fine("Set current author to \""+author.name+"\"");
        }
    }

    public void resetSelf() throws IOException, JavaGitException {
        config.configUnsetAll(workingTree, null, "user.name");
        config.configUnsetAll(workingTree, null, "user.email");
        LOGGER.fine("Reset current author");
    }
}