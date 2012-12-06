package com.sri.ltc.latexdiff;

/**
 * @author linda
 */
public enum LexemeType {

    COMMAND (true),
    COMMENT_BEGIN (true),
    PUNCTUATION (true),
    SYMBOL (true),
    WORD (true),
    PARAGRAPH (false),
    WHITESPACE (false),
    END_OF_FILE (false),
    START_OF_FILE (false);
    
    private final boolean isPrintable;
    
    LexemeType(boolean printable) {
        isPrintable = printable;
    }

    public boolean isPrintable() {
        return isPrintable;
    }
}
