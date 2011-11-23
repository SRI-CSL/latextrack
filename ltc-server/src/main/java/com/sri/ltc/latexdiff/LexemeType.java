/**
 ************************ 80 columns *******************************************
 * LexemeType
 *
 * Created on Dec 29, 2009.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

/**
 * @author linda
 */
public enum LexemeType {

    COMMAND (true),
    PREAMBLE (false),
    COMMENT (true),
    PUNCTUATION (true),
    SYMBOL (true),
    WORD (true),
    PARAGRAPH (false),
    WHITESPACE (false),
    END_OF_FILE (false);

    private final boolean isPrintable;
    LexemeType(boolean printable) {
        isPrintable = printable;
    }

    public boolean isPrintable() {
        return isPrintable;
    }
}
