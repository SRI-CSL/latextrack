/**
 ************************ 80 columns *******************************************
 * VersionControlException
 *
 * Created on 11/9/12.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.versioncontrol;

/**
 * Just wrapping any exceptions from the underlying implementations.
 *
 * @author linda
 */
public class VersionControlException extends Exception {

    public VersionControlException(Throwable throwable) {
        super(throwable);
    }
}
