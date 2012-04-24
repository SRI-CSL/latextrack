/**
 ************************ 80 columns *******************************************
 * StringReaderWrapper
 *
 * Created on May 21, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import java.io.Reader;
import java.io.StringReader;

/**
 * @author linda
 */
public final class StringReaderWrapper extends AbstractReaderWrapper<String> implements ReaderWrapper {
    
    public StringReaderWrapper(String text) {
        super(text);
    }

    @Override
    public Reader createReader() {
        return new StringReader(getWrapped());
    }
}
