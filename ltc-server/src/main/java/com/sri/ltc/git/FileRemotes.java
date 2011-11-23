/**
 ************************ 80 columns *******************************************
 * Self
 *
 * Created on Aug 11, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.git;

import edu.nyu.cs.javagit.api.GitFile;
import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.commands.IGitPull;
import edu.nyu.cs.javagit.api.commands.IGitPush;
import edu.nyu.cs.javagit.api.commands.IGitRemote;
import edu.nyu.cs.javagit.api.responses.AbstractResponse;
import edu.nyu.cs.javagit.api.responses.GitRemoteResponse;
import edu.nyu.cs.javagit.client.Factory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author linda
 */
public final class FileRemotes {

    private final static Logger LOGGER = Logger.getLogger(FileRemotes.class.getName());
    private final File workingTree;
    private final IGitRemote remote = Factory.createGitRemote();
    private final IGitPush push = Factory.createGitPush();
    private final IGitPull pull = Factory.createGitPull();

    public FileRemotes(GitFile gitFile) {
        if (gitFile == null)
            throw new IllegalArgumentException("Cannot create remotes with NULL as git file");
        this.workingTree = gitFile.getWorkingTree().getPath();
    }

    public Set<Remote> updateAndGetRemotes() throws JavaGitException {
        Set<Remote> remotes = new HashSet<Remote>();
        for (GitRemoteResponse.Remote r : remote.remote(workingTree)) {
            remotes.add(new Remote(r.name, r.url, r.isReadOnly()));
        }
        LOGGER.fine("Obtained current remotes with "+remotes.size()+" entries.");
        return remotes;
    }

    public int addRemote(String name, String url) throws JavaGitException {
        return remote.add(workingTree, null, name, url);
    }

    public int rmRemote(String name) throws JavaGitException {
        return remote.rm(workingTree, null, name);
    }

    public String pull(String repository) throws JavaGitException {
        return errorOrEmpty(pull.master(workingTree, null, repository));
    }

    public String push(String repository) throws JavaGitException {
        return errorOrEmpty(push.master(workingTree, null, repository));
    }

    private String errorOrEmpty(AbstractResponse response) {
        if (response.isError())
            return response.getError();
        return "";
    }
}