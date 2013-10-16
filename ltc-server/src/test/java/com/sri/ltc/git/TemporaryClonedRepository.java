/*
 * #%L
 * LaTeX Track Changes (LTC) allows collaborators on a version-controlled LaTeX writing project to view and query changes in the .tex documents.
 * %%
 * Copyright (C) 2009 - 2013 SRI International
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
package com.sri.ltc.git;

import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.TrackedFile;
import com.sri.ltc.versioncontrol.git.GitRepository;
import org.eclipse.jgit.api.Git;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * A cloned GIT repository in a temporary folder for tests against fixed dates and revisions.
 *
 * @author linda
 */
public class TemporaryClonedRepository extends TemporaryFolder {
    private Repository repository = null;

    public Repository getRepository() {
        return repository;
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        assert this.getRoot().exists();
        System.out.println("Cloning git repo to directory " + this.getRoot().toString());

        // cloning from resource file:
        assertNotNull("Repo to clone from is missing", getClass().getResource("/independence.bundle"));
        Git.cloneRepository()
                .setURI(getClass().getResource("/independence.bundle").toURI().toString())
                .setDirectory(this.getRoot())
                .call();
        File testGitDir = new File(this.getRoot().toString() + File.separatorChar + ".git");
        assertTrue(testGitDir.exists());

        repository = new GitRepository(new File(this.getRoot().toString()), false);
    }

    public TrackedFile getTrackedFile() throws IOException {
        if (repository == null)
            throw new RuntimeException("Repository is not initialized");
        return repository.getFile(new File(getRoot() + "/independence.tex"));
    }
}
