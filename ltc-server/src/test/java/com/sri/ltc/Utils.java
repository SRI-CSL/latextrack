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
package com.sri.ltc;

import com.sri.ltc.filter.Author;
import com.sri.ltc.git.TemporaryGitRepository;
import com.sri.ltc.versioncontrol.TrackedFile;

import java.io.File;

/**
 * Some utilities for various tests.
 *
 * @author linda
 */
public class Utils {

    public static void deleteFolder(File folder) {
        if (!folder.isDirectory())
            return;
        File[] files = folder.listFiles();
        if (files!=null)  //some JVMs return null for empty dirs
            for (File f: files) {
                if (f.isDirectory())
                    deleteFolder(f);
                else
                if (!f.delete())
                    throw new RuntimeException("Cannot delete file "+f.getAbsolutePath());
            }
        if (!folder.delete())
            throw new RuntimeException("Cannot delete folder "+folder.getAbsolutePath());
    }

    public static File createGitRepository(TemporaryGitRepository temporaryGitRepository, String[] contentArray, String[] authorNames) throws Exception {
        if (contentArray == null)
            throw new IllegalArgumentException("cannot create temporary git repository without contents");
        if (authorNames == null || "".equals(authorNames[0]))
            throw new IllegalArgumentException("cannot create temporary git repository without authors");

        // create git repository with first author and first contents; commit file
        Author firstAuthor = new Author(authorNames[0], "");
        temporaryGitRepository.setAuthor(firstAuthor);
        TrackedFile file = temporaryGitRepository.createTestFileInRepository("file-", ".txt", contentArray[0], true);
        file.commit("first commit");

        // now go through the rest of the contents and possibly authors (otherwise use first author)
        for (int i = 1; i < contentArray.length; i++) {
            // modify and commit file:
            if (i < authorNames.length)
                temporaryGitRepository.setAuthor(new Author(authorNames[i], ""));
            else
                temporaryGitRepository.setAuthor(firstAuthor);
            temporaryGitRepository.modifyTestFileInRepository(file, contentArray[i], false);
            file.commit("commit of version "+(i+1));
        }

        return file.getFile();
    }
}
