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
public abstract class AbstractReaderWrapper<T> {

    private final T wrapped;

    public AbstractReaderWrapper(T location) {
        this.wrapped = location;
    }

    public T getWrapped() {
        return wrapped;
    }

    public Lexeme removeAdditions(Lexeme lexeme) {
        return lexeme;
    }
}
