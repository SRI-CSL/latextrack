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

import edu.nyu.cs.javagit.api.GitFileSystemObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A response data object for &lt;git-status&gt; command
 */
public class GitStatusResponse extends AbstractResponse {

    private final Map<String[], GitFileSystemObject.Status> statusMap =
            new HashMap<String[], GitFileSystemObject.Status>();

    public void addEntry(String f1, String f2, GitFileSystemObject.Status status) {
        statusMap.put(new String[]{f1, f2}, status);
    }

    public boolean containsAt(String f, int i) {
        return getAt(f, i) != null;
    }

    public GitFileSystemObject.Status getAt(String f, int i) {
        if (f == null)
            return null;

        GitFileSystemObject.Status result = null;
        for (Map.Entry<String[], GitFileSystemObject.Status> entry : statusMap.entrySet())
            if (f.equals(entry.getKey()[i])) {
                result = entry.getValue();
                break;
            }
        return result;
    }
}
