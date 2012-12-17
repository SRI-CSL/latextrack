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
package com.sri.ltc.versioncontrol.git;

import com.sri.ltc.versioncontrol.Remote;
import com.sri.ltc.versioncontrol.Remotes;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.merge.MergeStrategy;

import java.util.HashSet;
import java.util.Set;

public class GitRemotes extends Remotes<GitRepository> {
    public GitRemotes(GitRepository repository) {
        super(repository);
    }

    @Override
    public Set<Remote> get() {
        Set<Remote> remotes = new HashSet<Remote>();

        StoredConfig config = getRepository().getWrappedRepository().getConfig();
        Set<String> subsections = config.getSubsections("remote");
        for (String subsection : subsections) {
            String url = config.getString("remote", subsection, "url");
            // TODO: how to determine if a remote is read-only?
            remotes.add(new Remote(subsection, url, false));
        }

        return remotes;
    }

    @Override
    public int addRemote(String name, String url) {
        StoredConfig config = getRepository().getWrappedRepository().getConfig();
        config.setString("remote", name, "url", url);
        return 0;
    }

    @Override
    public int removeRemote(String name) {
        StoredConfig config = getRepository().getWrappedRepository().getConfig();
        config.unsetSection("remote", name);
        return 0;
    }

    @Override
    public void pull(String name) throws Exception {
        // TODO: this won't work - needs authentication
        Git git = new Git(getRepository().getWrappedRepository());
        git.fetch().setRemote(name).call();
        // TODO: is this good? a git pull is a fetch + a merge
        git.merge().call();
    }

    @Override
    public void push(String name) throws Exception {
        Git git = new Git(getRepository().getWrappedRepository());
        git.push().setRemote(name).call();
    }
}
