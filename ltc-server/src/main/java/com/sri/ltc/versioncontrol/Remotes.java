/*
 * #%L
 * LaTeX Track Changes (LTC) allows collaborators on a version-controlled LaTeX writing project to view and query changes in the .tex documents.
 * %%
 * Copyright (C) 2009 - 2012 SRI International
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package com.sri.ltc.versioncontrol;

import java.util.Set;
import java.util.logging.Logger;

/**
 * @author linda
 */

public abstract class Remotes<RepositoryClass extends Repository> {
    private final static Logger LOGGER = Logger.getLogger(Remotes.class.getName());

    private RepositoryClass repository;

    public Remotes(RepositoryClass repository) {
        this.repository = repository;
    }

    protected RepositoryClass getRepository() {
        return repository;
    }

    abstract public Set<Remote> get();

    abstract public int addRemote(String name, String url);

    abstract public int removeRemote(String name);

    abstract public void pull(String name) throws Exception;

    abstract public void push(String name) throws Exception;
}