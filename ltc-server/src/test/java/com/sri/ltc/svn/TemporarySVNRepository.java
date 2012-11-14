/**
 ************************ 80 columns *******************************************
 * TemporarySVNRepository
 *
 * Created on 11/14/12.
 *
 * Copyright 2009-2010, SRI International.
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
import java.io.FileWriter;
import java.io.IOException;

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
        assert this.getRoot().exists();

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

        repository.setSelf(new Author("adams", null, null));
    }

    public TrackedFile getTrackedFile() throws IOException {
        if (repository == null)
            throw new RuntimeException("Repository is not initialized");

        return repository.getFile(new File(getRoot() + "/" + TEST_REPO + "/" + FILE_NAME));
    }
}
