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

import edu.nyu.cs.javagit.api.Ref;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A response data object for the <code>git-reset</code> command.
 */
public final class GitResetResponse extends AbstractResponse {

    /*
    * The list of files left in a dirty state (different than what is in the new HEAD commit) in the
    * working tree.
    */
    private List<File> filesNeedingUpdate = new ArrayList<File>();

    // If the --hard option was given, this is the SHA1 of the new head.
    private Ref newHeadSha1 = null;

    // If the --hard option was given, this is the short message for the commit at the new head.
    private String newHeadShortMessage = null;

    /**
     * Gets the file at the specified index from the list of files needing update.
     *
     * @param index The index of the file to get. It must fall in the range:
     *              <code>0 &lt;= index &lt; getRemovedFilesSize()</code>.
     * @return The file at the specified index.
     */
    public File getFileNeedingUpdate(int index) {
        return filesNeedingUpdate.get(index);
    }

    /**
     * Adds the file to the files needing update list.
     *
     * @param file The file to add to the files needing update list.
     */
    public void addFileToFilesNeedingUpdateList(File file) {
        filesNeedingUpdate.add(file);
    }


    /**
     * Gets an <code>Iterator</code> over the list of files needing update.
     *
     * @return An <code>Iterator<code> over the list of files needing update.
     */
    public Iterator<File> getFilesNeedingUpdateIterator() {
        return (new ArrayList<File>(filesNeedingUpdate)).iterator();
    }

    /**
     * Gets the SHA1 of the new head commit. Only returned when the <code>--hard</code> option is
     * used.
     *
     * @return The SHA1 of the new head commit.
     */
    public Ref getNewHeadSha1() {
        return newHeadSha1;
    }

    /**
     * Gets the short message of the new head commit. Only returned when the <code>--hard</code>
     * option is used.
     *
     * @return The short message of the new head commit.
     */
    public String getNewHeadShortMessage() {
        return newHeadShortMessage;
    }

    /**
     * Gets the number of files needing update (provided that the quiet option was not used).
     *
     * @return The number of files needing update. If the quiet option was used, zero (0) will be
     *         returned.
     */
    public int getRemovedFilesSize() {
        return filesNeedingUpdate.size();
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (null != newHeadSha1) {
            buf.append("HEAD: ");
            buf.append(newHeadSha1);
            buf.append(" ");
            buf.append(newHeadShortMessage);
        }
        if (filesNeedingUpdate.size() > 0) {
            buf.append(filesNeedingUpdate);
        }
        return buf.toString();
    }

}
