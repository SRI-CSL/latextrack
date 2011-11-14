/*
 * ====================================================================
 * Copyright (c) 2008 JavaGit Project.  All rights reserved.
 *
 * This software is licensed using the GNU LGPL v2.1 license.  A copy
 * of the license is included with the distribution of this source
 * code in the LICENSE.txt file.  The text of the license can also
 * be obtained at:
 *
 *   http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *
 * For more information on the JavaGit project, see:
 *
 *   http://www.javagit.com
 * ====================================================================
 */
package edu.nyu.cs.javagit.api.responses;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>GitRmResponse</code> holds the response information returned by the
 * <code>git rm</code> command.
 */
public class GitRmResponse extends AbstractResponse {

    // The list of removed files.
    private final List<File> removedFiles = new ArrayList<File>();

    /**
     * Adds the file to the removed files list.
     *
     * @param file The file to add to the removed files list.
     */
    public void addFileToRemovedFilesList(File file) {
        removedFiles.add(file);
    }

    public List<File> getRemovedFiles() {
        return removedFiles;
    }
}
