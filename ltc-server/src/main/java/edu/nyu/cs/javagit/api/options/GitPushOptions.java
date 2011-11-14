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
 *
 * changed by Linda on Jul 24, 2010
 */
package edu.nyu.cs.javagit.api.options;

/**
 * A class for managing options and passing these options to &lt;git-push&gt; command.
 */

public final class GitPushOptions {

    public String gitReceivePack = null;

    public GitPushOptions(String gitReceivePack) {
        this.gitReceivePack = gitReceivePack;
    }
}