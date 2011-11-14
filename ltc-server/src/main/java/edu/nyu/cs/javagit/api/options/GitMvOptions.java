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
 * A class to manage options to the <code>git mv</code> command.
 */
public class GitMvOptions {
    // boolean variable to set or reset -f option.
    private boolean optF = false;

    // boolean variable to set or reset -n option.
    private boolean optN = false;

    // boolean variable to set or reset -k option.
    private boolean optK = false;

    public boolean isOptF() {
        return optF;
    }

    public void setOptF(boolean optF) {
        this.optF = optF;
    }

    public boolean isOptK() {
        return optK;
    }

    public void setOptK(boolean optK) {
        this.optK = optK;
    }

    public boolean isOptN() {
        return optN;
    }

    public void setOptN(boolean optN) {
        this.optN = optN;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        if (optF)
            buf.append("-f ");

        if (optN)
            buf.append("-n ");

        if (optN)
            buf.append("-k ");

        return buf.toString();
    }
}
