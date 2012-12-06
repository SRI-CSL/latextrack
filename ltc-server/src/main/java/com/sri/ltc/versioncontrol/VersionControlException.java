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
