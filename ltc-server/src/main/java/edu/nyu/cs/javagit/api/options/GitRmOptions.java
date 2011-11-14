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
package edu.nyu.cs.javagit.api.options;

/**
 * A class to manage passing arguments to the <code>git rm</code> command.
 * <p/>
 * <code>git rm</code> deletes the indicated files/folders from the git repository.
 * <pre>
 * [-f | --force] [-n] [-r] [--cached] [--ignore-unmatch] [--quiet]
 * </pre>
 * <p/>
 * Note: the --ignore-unmatch option is not included because it does not make sense in this setting.
 */
public final class GitRmOptions {

    /*
    * The --cached option. Unstages and removes paths only from the index while leaving the working
    * tree files alone.
    */
    private boolean optCached = false;

    // The -f option. Overrides the up-to-date check.
    private boolean optF = false;

    // The -n (--dry-run) option. Doesn't actually remove anything, it just shows what the command
    // would do.
    private boolean optN = false;

    // The -q (--quiet) option. Suppresses output.
    private boolean optQ = false;

    // The -r option. Recursively remove a directory and its descendant files/directories from the
    // repository.
    private boolean optR = false;

    public boolean isOptCached() {
        return optCached;
    }

    public boolean isOptF() {
        return optF;
    }

    public boolean isOptN() {
        return optN;
    }

    public boolean isOptQ() {
        return optQ;
    }

    public boolean isOptR() {
        return optR;
    }

    public void setOptCached(boolean optCached) {
        this.optCached = optCached;
    }

    public void setOptF(boolean optF) {
        this.optF = optF;
    }

    public void setOptN(boolean optN) {
        this.optN = optN;
    }

    public void setOptQ(boolean optQ) {
        this.optQ = optQ;
    }

    public void setOptR(boolean optR) {
        this.optR = optR;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        if (optCached)
            buf.append("--cached ");

        if (optF)
            buf.append("-f ");

        if (optN)
            buf.append("-n ");

        if (optQ)
            buf.append("--quiet ");

        if (optR)
            buf.append("-r ");

        return buf.toString();
    }

}
