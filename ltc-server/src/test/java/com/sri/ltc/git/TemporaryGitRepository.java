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
package com.sri.ltc.git;

import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.TrackedFile;
import com.sri.ltc.versioncontrol.git.GitRepository;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class TemporaryGitRepository extends TemporaryFolder {
    private Repository repository = null;

    public Repository getRepository() {
        return repository;
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        assert this.getRoot().exists();

        System.out.println("Creating a git repo at " + this.getRoot().toString());
        repository = new GitRepository(new File(this.getRoot().toString()), true);

        File testGitDir = new File(this.getRoot().toString() + File.separatorChar + ".git");
        assertTrue(testGitDir.exists());
    }

    public TrackedFile createTestFileInRepository(String prefix, String suffix, String contents, boolean add)
            throws Exception {
        if (repository == null)
            throw new RuntimeException("Repository is not initialized");

        File file = File.createTempFile(prefix, suffix, getRoot());
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.append(contents);
        fileWriter.flush();
        fileWriter.close();

        if (add) {
            getRepository().addFile(file);
        }

        return getRepository().getFile(file);
    }

    public void modifyTestFileInRepository(TrackedFile file, String text, boolean append) throws IOException {
        if (repository == null)
            throw new RuntimeException("Repository is not initialized");

        // check that file is in our repository
        if (!repository.equals(file.getRepository()))
            throw new RuntimeException("given file \""+file+"\" is not tracked in this repository");

        // append text
        FileWriter fileWriter = new FileWriter(file.getFile(), append);
        fileWriter.append(text);
        fileWriter.flush();
        fileWriter.close();
    }
}
