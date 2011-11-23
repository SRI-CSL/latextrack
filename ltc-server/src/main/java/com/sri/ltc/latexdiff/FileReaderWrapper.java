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
public class FileReaderWrapper extends AbstractReaderWrapper implements ReaderWrapper {

    public FileReaderWrapper(String location) {
        super(location);
    }

    public Reader createReader() {
        try {
            return new FileReader(getLocation());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
