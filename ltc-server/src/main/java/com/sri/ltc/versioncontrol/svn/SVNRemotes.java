package com.sri.ltc.versioncontrol.svn;

/**
 * Copyright 2012, SRI International.
 */

import com.sri.ltc.git.Remote;
import com.sri.ltc.versioncontrol.Remotes;

import java.util.HashSet;
import java.util.Set;

public class SVNRemotes extends Remotes<SVNRepository> {
    public SVNRemotes(SVNRepository repository) {
        super(repository);
    }

    @Override
    public Set<Remote> get() {
        // TODO
        return new HashSet<Remote>();
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
        // TODO
    }

    @Override
    public void push(String name) throws Exception {
        // TODO
    }
}
