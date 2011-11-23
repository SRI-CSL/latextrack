/**
 ************************ 80 columns *******************************************
 * Lexeme
 *
 * Created on Dec 29, 2009.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

/**
 * @author linda
 */
public class Lexeme {

    final LexemeType type;
    final String contents;
    final int pos, line, column, length;

    public Lexeme(LexemeType type, String contents, int pos, int line, int column, int length) {
        if (type == null)
            throw new IllegalArgumentException("Cannot create Lexeme of type NULL");
        if (contents == null)
            throw new IllegalArgumentException("Cannot create Lexeme with contents NULL");
        this.type = type;
        this.contents = contents;
        this.pos = pos;
        this.line = line;
        this.column = column;
        this.length = length;
    }

    public String displayContents() {
        return type.isPrintable()?contents:"";
    }

    @Override
    public String toString() {
        return type+" "+displayContents()+"  ("+line+","+column+")@"+pos+" "+length;
    }
}
