/**
 ************************ 80 columns *******************************************
 * Self
 *
 * Created on Aug 11, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.versioncontrol;

import com.sri.ltc.git.Remote;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author linda
 */

public abstract class Remotes<RepositoryClass extends Repository> {
    private final static Logger LOGGER = Logger.getLogger(Remotes.class.getName());

    protected RepositoryClass repository;

    public Remotes(RepositoryClass repository) {
        this.repository = repository;
    }

    abstract public Set<Remote> get();

    abstract public int addRemote(String name, String url);

    abstract public int removeRemote(String name);

    abstract public void pull(String name) throws Exception;

    abstract public void push(String name) throws Exception;
}