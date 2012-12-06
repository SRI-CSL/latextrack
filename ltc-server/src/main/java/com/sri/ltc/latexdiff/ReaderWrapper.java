package com.sri.ltc.latexdiff;

import java.io.Reader;

/**
 * @author linda
 */
public interface ReaderWrapper {

    public Reader createReader() throws Exception;

    public Lexeme removeAdditions(Lexeme lexeme);
}
