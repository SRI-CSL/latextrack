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

import com.sri.ltc.versioncontrol.git.GitRepository;
import com.sri.ltc.versioncontrol.svn.SVNRepository;

import java.io.File;
import java.io.FilenameFilter;

public class RepositoryFactory {

    public final static FilenameFilter GIT_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            return file.isDirectory() && ".git".equals(s);
        }
    };

    public final static FilenameFilter SVN_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            return file.isDirectory() && ".svn".equals(s);
        }
    };

    public static Repository fromPath(File path) throws Exception {
        if (path == null)
            throw new IllegalArgumentException("Cannot create repository from NULL");
        if (!path.exists() || !path.isFile())
            throw new IllegalArgumentException("Cannot create repository if given file \""+
                    path.getName()+"\" does not exist or is not a file");

        // walk up the parent dirs and look for .git or .svn directory
        File testPath = path.getParentFile();
        while (testPath != null && testPath.isDirectory()) {
            if (testPath.listFiles(GIT_FILTER).length == 1)
                return new GitRepository(path);
            else if (testPath.listFiles(SVN_FILTER).length == 1)
                return new SVNRepository(path);
            else
                testPath = testPath.getParentFile();
        }

        throw new RuntimeException("Could not create repository from given file "+path);
    }
}
