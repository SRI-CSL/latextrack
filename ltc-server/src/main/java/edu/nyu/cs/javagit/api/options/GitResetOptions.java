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

import edu.nyu.cs.javagit.api.Ref;

/**
 * A class to manage passing arguments to the <code>git reset</code> command.
 * <p/>
 * <code>git reset</code> resets the HEAD of the current checked out branch to the state of a
 * previous commit.
 */
public class GitResetOptions {

    /**
     * An enumeration of the types of resets that can be performed.
     */
    public static enum ResetType {
        MIXED("mixed"),
        SOFT("soft"),
        HARD("hard"),
        MERGE("merge"),
        KEEP("keep");

        private String cliOpt;

        private ResetType(String cliOpt) {
            this.cliOpt = cliOpt;
        }

        public String toString() {
            return "--" + cliOpt;
        }
    }

    // The name of the commit to reset to.
    private Ref commitName;

    // The type of reset to perform.
    private ResetType resetType;

    // Suppress feedback.
    private boolean quiet = false;

    private boolean patch = false;

    public Ref getCommitName() {
        return commitName;
    }

    public ResetType getResetType() {
        return resetType;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public boolean isPatch() {
        return patch;
    }

    public void setCommitName(Ref commitName) {
        this.commitName = commitName;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public void setResetType(ResetType resetType) {
        this.resetType = resetType;
    }

    public void setPatch(boolean patch) {
        this.patch = patch;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(resetType.toString());
        buf.append(" ");

        if (quiet) {
            buf.append("-q ");
        }

        buf.append(commitName.toString());
        return buf.toString();
    }
}
