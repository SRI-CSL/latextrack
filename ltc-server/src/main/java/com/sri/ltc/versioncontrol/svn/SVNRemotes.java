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
package com.sri.ltc.versioncontrol.svn;

import com.sri.ltc.versioncontrol.Remote;
import com.sri.ltc.versioncontrol.Remotes;
import org.tmatesoft.svn.core.SVNException;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class SVNRemotes extends Remotes<SVNRepository> {
    private final static Logger LOGGER = Logger.getLogger(SVNRemotes.class.getName());

    public SVNRemotes(SVNRepository repository) {
        super(repository);
    }

    @Override
    public Set<Remote> get() {
        Set<Remote> remotes = new HashSet<Remote>();

        try {
            String url = getRepository().getURL();
            remotes.add(new Remote("default", url, false));
        }
        catch (SVNException e) {
            LOGGER.warning("SVNRemotes could not get url for repository");
        }

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
        // TODO
    }

    @Override
    public void push(String name) throws Exception {
        // TODO
    }
}
