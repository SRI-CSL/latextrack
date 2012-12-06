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

import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.Remotes;
import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.TrackedFile;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.BundleWriter;
import sun.security.pkcs11.wrapper.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class GitRepository implements Repository {
    private final static Logger LOGGER = Logger.getLogger(GitRepository.class.getName());
    private org.eclipse.jgit.lib.Repository repository = null;

    public GitRepository(File localPath) throws IOException {
        this(localPath, false);
    }
    
    public GitRepository(File localPath, boolean create) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        if (create) {
            builder.setWorkTree(localPath);
        } else {
            builder.findGitDir(localPath);
        }

        builder.readEnvironment();

        repository = builder.build();

        if (create) {
            repository.create();
        }
    }

    @Override
    public void addFile(File file) throws Exception {
        Git git = new Git(repository);
        git.add().addFilepattern(file.getName()).call();
    }

    public org.eclipse.jgit.lib.Repository getWrappedRepository() {
        return repository;
    }

    @Override
    public TrackedFile getFile(File file) throws IOException {
        return new GitTrackedFile(this, file);
    }

    @Override
    public Remotes getRemotes() {
        return new GitRemotes(this);
    }

    @Override
    public File getBundle(File outputDirectory) throws IOException {
        LOGGER.fine("Creating "+outputDirectory+"/bundle.git file");

        File bundle = new File(outputDirectory, "bundle.git");

        BundleWriter bundleWriter = new BundleWriter(repository);
        for (Ref ref : repository.getAllRefs().values()) {
            if (!ref.isSymbolic()) {
                bundleWriter.include(ref);
            }
        }

        FileOutputStream fos = new FileOutputStream(bundle);
        bundleWriter.writeBundle(null, fos);
        fos.close();

        return bundle;
    }

    @Override
    public Author getSelf() {
        String name = repository.getConfig().getString("user", null, "name");
        String email = repository.getConfig().getString("user", null, "email");

        return new Author(name, email, null);
    }

    @Override
    public void setSelf(Author author) {
        repository.getConfig().setString("user", null, "name", author.name);
        repository.getConfig().setString("user", null, "email", author.email);
        LOGGER.fine("Set current author to \""+author.name+"\"");
    }

    @Override
    public void resetSelf() {
        repository.getConfig().unset("user", null, "name");
        repository.getConfig().unset("user", null, "email");
        LOGGER.fine("Reset current author");
    }
}
