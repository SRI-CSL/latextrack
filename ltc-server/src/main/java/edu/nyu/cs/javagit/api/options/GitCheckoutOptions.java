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
import edu.nyu.cs.javagit.api.Ref.RefType;
import edu.nyu.cs.javagit.utilities.CheckUtilities;

/**
 * Class for managing options for &lt;git-checkout&gt; command.
 */
public class GitCheckoutOptions {

    /**
     * Option for creating a new branch with the name provided.
     */
    private Ref optB = null;

    private boolean optQ = false;

    private boolean optF = false;

    private boolean optTrack = false;

    private boolean optNoTrack = false;

    private boolean optL = false;

    private boolean optM = false;

    private boolean optPatch = false;

    public void setOptB(Ref newBranch) {
        CheckUtilities.validateArgumentRefType(newBranch, RefType.BRANCH, "New Branch Name");
        optB = newBranch;
    }

    public Ref getOptB() {
        return optB;
    }

    public boolean isOptB() {
        return optB != null;
    }

    public boolean isOptPatch() {
        return optPatch;
    }

    public void setOptPatch(boolean optPatch) {
        this.optPatch = optPatch;
    }

    public boolean getOptQ() {
        return optQ;
    }

    public void setOptQ(boolean optQ) {
        this.optQ = optQ;
    }

    public boolean getOptF() {
        return optF;
    }

    public void setOptF(boolean optF) {
        this.optF = optF;
    }

    public boolean getOptTrack() {
        return optTrack;
    }

    /**
     * Sets the track option.
     *
     * @param optTrack True if the track option should be set, else false.
     */
    public void setOptTrack(boolean optTrack) {
        if (optNoTrack && optTrack)
            throw new IllegalArgumentException("Cannot set \"track\" checkout option when \"no-track\" is set.");
        this.optTrack = optTrack;
    }

    public boolean getOptNoTrack() {
        return optNoTrack;
    }

    /**
     * Sets the noTrack option.
     *
     * @param optNoTrack True if noTrack options need to be set, else false.
     */
    public void setOptNoTrack(boolean optNoTrack) {
        if (optTrack && optNoTrack) {
            throw new IllegalArgumentException("Cannot set \"no-track\" checkout option when \"track\" is set.");
        }
        this.optNoTrack = optNoTrack;
    }

    public boolean getOptL() {
        return optL;
    }

    public void setOptL(boolean optL) {
        this.optL = optL;
    }

    public boolean getOptM() {
        return optM;
    }

    public void setOptM(boolean optM) {
        this.optM = optM;
    }
}
