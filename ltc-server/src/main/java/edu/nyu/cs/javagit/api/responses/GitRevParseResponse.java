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

import java.util.ArrayList;
import java.util.List;

/**
 * A response data object for the git log command.
 */
public final class GitRevParseResponse extends AbstractResponse {

    private final List<String> outputList = new ArrayList<String>();

    public void addOutput(String line) {
        outputList.add(line);
    }

    public List<String> getOutputList() {
        return outputList;
    }
}
