/**
 ************************ 80 columns *******************************************
 * StringReaderWrapper
 *
 * Created on May 21, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import com.sri.ltc.versioncontrol.Commit;

import java.io.*;

/**
 * @author linda
 */
public final class CommitReaderWrapper extends AbstractReaderWrapper<Commit> implements ReaderWrapper {

    public CommitReaderWrapper(Commit commit) {
        super(commit);
    }

    @Override
    public Reader createReader() {
        try {
            return getWrapped().getContents();
        } catch (Exception e) {
            // TODO: log this, better error handling generally
            return null;
        }
    }
}
