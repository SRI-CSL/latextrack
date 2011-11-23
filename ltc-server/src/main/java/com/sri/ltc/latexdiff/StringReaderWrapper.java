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
public class StringReaderWrapper extends AbstractReaderWrapper implements ReaderWrapper {
    
    public StringReaderWrapper(String location) {
        super(location);
    }

    public Reader createReader() {
        return new StringReader(getLocation());
    }
}
