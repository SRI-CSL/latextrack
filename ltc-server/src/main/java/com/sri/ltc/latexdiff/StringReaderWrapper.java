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
