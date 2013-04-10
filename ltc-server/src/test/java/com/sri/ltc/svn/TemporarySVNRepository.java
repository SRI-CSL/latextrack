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
package com.sri.ltc.svn;

import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.TrackedFile;
import com.sri.ltc.versioncontrol.svn.SVNRepository;
import org.junit.rules.TemporaryFolder;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.*;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * @author linda
 */
public class TemporarySVNRepository extends TemporaryFolder{

    private final static String TEST_URL = "https://spartan.csl.sri.com/svn/public/LTC/";
    private final static String TEST_REPO = "tutorial-svn";
    private final static String FILE_NAME = "independence.tex";

    private Repository repository = null;

    @Override
    protected void before() throws Throwable {
        super.before();
        assertTrue("root folder exists", this.getRoot().exists());

        // check out SVN test repo
        File svnRoot = new File(this.getRoot().toString() + "/" + TEST_REPO);
        System.out.println("Creating an SVN repo at " + svnRoot.getAbsolutePath());
        SVNClientManager clientManager = SVNClientManager.newInstance(null);
        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        updateClient.doCheckout(SVNURL.parseURIEncoded(TEST_URL + "/" + TEST_REPO),
                svnRoot,
                SVNRevision.UNDEFINED,
                SVNRevision.HEAD,
                SVNDepth.INFINITY,
                false); // there shouldn't be any obstructing files

        // turn into our objects:
        repository = new SVNRepository(svnRoot);

        // TODO: check that .svn exists?
        File svn = new File(getRoot() + "/" + TEST_REPO + "/.svn");
        assertTrue(".svn directory exists", svn.exists());
        assertTrue(".svn is directory", svn.isDirectory());

        repository.setSelf(new Author("adams", null));
    }

    public TrackedFile getTrackedFile() throws IOException {
        if (repository == null)
            throw new RuntimeException("Repository is not initialized");

        return repository.getFile(new File(getRoot() + "/" + TEST_REPO + "/" + FILE_NAME));
    }

    public Repository getRepository() {
        return repository;
    }
}
