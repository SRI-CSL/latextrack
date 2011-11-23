/**
 ************************ 80 columns *******************************************
 * AbstractReaderWrapper
 *
 * Created on May 21, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

/**
 * @author linda
 */
public class AbstractReaderWrapper {

    private final String location;

    public AbstractReaderWrapper(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }
}
