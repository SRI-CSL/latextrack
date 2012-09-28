/**
 * Copyright 2012, SRI International.
 */

package com.sri.ltc.versioncontrol.git;

import com.sri.ltc.versioncontrol.Remote;
import com.sri.ltc.versioncontrol.Remotes;
import org.eclipse.jgit.api.Git;

import java.util.HashSet;
import java.util.Set;

public class GitRemotes extends Remotes<GitRepository> {
    public GitRemotes(GitRepository repository) {
        super(repository);
    }

    @Override
    public Set<Remote> get() {
        // TODO:
        //Remote remote = new Remote("default", "", false);
        Set<Remote> remotes = new HashSet<Remote>();
        //remotes.add(remote);
        return remotes;
    }

    @Override
    public int addRemote(String name, String url) {
        // TODO
        return 0;
    }

    @Override
    public int removeRemote(String name) {
        // TODO
        return 0;
    }

    @Override
    public void pull(String name) throws Exception {
        // TODO: set the repository!
        Git git = new Git(repository.getWrappedRepository());
        git.pull().call();
    }

    @Override
    public void push(String name) throws Exception {
        // TODO: set the repository!
        Git git = new Git(repository.getWrappedRepository());
        // TODO: Not yet implemented to avoid doing harm before this is tested, since the repo is being ignored!
        // git.push().call();
    }
}
