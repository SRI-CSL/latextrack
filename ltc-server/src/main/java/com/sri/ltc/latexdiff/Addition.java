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

    public final String text;
    public final List<Lexeme> lexemes;

    public Addition(int start_position, String text, List<Lexeme> lexemes, boolean inPreamble, boolean inComment, boolean isCommand) {
        super(start_position, inPreamble, inComment, isCommand);
        if (text == null || "".equals(text))
            throw new IllegalArgumentException("Text of addition cannot be NULL or empty.");
        this.text = text;
        this.lexemes = Collections.unmodifiableList(lexemes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Addition addition = (Addition) o;

        if (lexemes != null ? !lexemes.equals(addition.lexemes) : addition.lexemes != null) return false;
        if (!text.equals(addition.text)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + text.hashCode();
        result = 31 * result + (lexemes != null ? lexemes.hashCode() : 0);
        return result;
    }

    String toXMLContents() {
        StringBuffer buffer = new StringBuffer(super.toXMLContents());
        buffer.append("  <text>");
        buffer.append(escapeText(text));
        buffer.append("</text>\n");        
        return buffer.toString();
    }

    @Override
    public String toString() {
        return "<addition>\n"+toXMLContents()+"</addition>";
    }
}
