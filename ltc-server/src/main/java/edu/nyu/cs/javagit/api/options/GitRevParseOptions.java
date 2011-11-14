/**
 ************************ 80 columns *******************************************
 * GitRevParseOptions
 *
 * Created on Aug 27, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package edu.nyu.cs.javagit.api.options;

/**
 * @author linda
 */
public final class GitRevParseOptions {

    private boolean verify = false;

    public boolean isVerify() {
        return verify;
    }

    public void setVerify(boolean verify) {
        this.verify = verify;
    }
}
