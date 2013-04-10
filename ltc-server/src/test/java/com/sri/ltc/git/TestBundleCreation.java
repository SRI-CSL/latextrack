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

import com.sri.ltc.categories.IntegrationTests;
import com.sri.ltc.versioncontrol.TrackedFile;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FilenameFilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * testing various aspects of GIT bundle creation.
 * @author linda
 */
@Ignore
@Category(IntegrationTests.class)
public class TestBundleCreation {

    // a fresh repository for each test:
    @Rule
    public TemporaryGitRepository temporaryGitRepository = new TemporaryGitRepository();

    @Rule
    public TemporaryFolder bundleDir = new TemporaryFolder(); // where to create the bundle

    @Rule
    public TemporaryFolder cloneDir = new TemporaryFolder(); // where to clone the bundle for testing

    @Test
    public void bundleWithOneFile() {
        // commit a few changes to a tracked file and confirm that bundle contains this file
        assertTrue(temporaryGitRepository.getRoot().exists());

        try {
            TrackedFile trackedFile = temporaryGitRepository.createTestFileInRepository("foo", ".txt", "bundleOne", true);
            trackedFile.commit("commit from bundleWithOneFile");

            // create bundle
            File bundle = trackedFile.getRepository().getBundle(bundleDir.getRoot());

            // clone bundle
            // TODO Linda says: Skip, if you know a better way of comparing the bundle file to the original repo...
            assertTrue(cloneDir.getRoot().exists());
            ProcessBuilder pb = new ProcessBuilder("git", "clone", bundle.getAbsolutePath(), "bundle");
            pb.directory(cloneDir.getRoot());
            Process p = pb.start();
            p.waitFor();

            // now compare original and cloned repos:
            File cloneRoot = new File(cloneDir.getRoot(), "bundle");
            assertTrue("root exists", cloneRoot.exists());
            assertTrue("root is directory", cloneRoot.isDirectory());
            File dotGitClone = new File(cloneRoot, ".git");
            assertTrue("root contains .git subdirectory", dotGitClone.exists() && dotGitClone.isDirectory());
            File[] clonedTrackedFiles = cloneRoot.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return s.startsWith("foo") && s.endsWith(".txt");
                }
            });
            assertEquals("one tracked file", 1, clonedTrackedFiles.length);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    // TODO: bundle with subdirs, bundle with 2 files etc.
}
