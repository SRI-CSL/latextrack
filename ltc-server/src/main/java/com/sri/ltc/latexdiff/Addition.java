/**
 ************************ 80 columns *******************************************
 * Addition
 *
 * Created on Jan 6, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import java.util.Collections;
import java.util.List;

/**
 * @author linda
 */
public class Addition extends Change {

    public final int end_position;
    public final List<Lexeme> lexemes;

    public Addition(int start_position, int end_position, List<Lexeme> lexemes, boolean inPreamble, boolean inComment, boolean isCommand) {
        super(start_position, inPreamble, inComment, isCommand);
        this.end_position = end_position;
        if (lexemes == null)
            throw new NullPointerException("List of lexemes in addition cannot be NULL.");
        this.lexemes = Collections.unmodifiableList(lexemes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Addition)) return false;
        if (!super.equals(o)) return false;

        Addition addition = (Addition) o;

        if (!lexemes.equals(addition.lexemes)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + lexemes.hashCode();
        return result;
    }

    String toXMLContents() {
        StringBuilder buffer = new StringBuilder(super.toXMLContents());
        buffer.append("  <lexemes size=");
        buffer.append(lexemes.size());
        buffer.append("/>\n");
        return buffer.toString();
    }

    @Override
    public String toString() {
        return "<addition>\n"+toXMLContents()+"</addition>";
    }
}
