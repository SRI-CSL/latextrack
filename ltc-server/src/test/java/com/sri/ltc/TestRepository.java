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
package com.sri.ltc;

import com.sri.ltc.versioncontrol.RepositoryFactory;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

/**
 * Some generic tests about the repository factory.
 * @author linda
 */
public class TestRepository {

    @ClassRule
    static public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test(expected = IllegalArgumentException.class)
    public void testNullPath() throws Exception {
        RepositoryFactory.fromPath(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonExisting() throws Exception {
        RepositoryFactory.fromPath(new File("/I/really/hope/this/file/does/not/exist"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotFile() throws Exception {
        RepositoryFactory.fromPath(temporaryFolder.getRoot());
    }

    @Test(expected = RuntimeException.class)
    public void testNotUnderVC() throws Exception {
        // create a new file, then try to create a repo from it:
        RepositoryFactory.fromPath(temporaryFolder.newFile("myFile.txt"));
    }
}
