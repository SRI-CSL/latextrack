/**
 ************************ 80 columns *******************************************
 * FileReaderWrapper
 *
 * Created on May 21, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

/**
 * @author linda
 */
public final class FileReaderWrapper extends AbstractReaderWrapper<String> implements ReaderWrapper {

    public FileReaderWrapper(String file) {
        super(file);
    }

    @Override
    public Reader createReader() {
        try {
            return new FileReader(getWrapped());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
