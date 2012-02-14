/**
 ************************ 80 columns *******************************************
 * SmallAddition
 *
 * Created on Jan 6, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import java.util.Collections;
import java.util.EnumSet;

/**
 * @author linda
 */
public final class SmallAddition extends Addition {

    public final String text;

    public SmallAddition(int start_position, String text, EnumSet<Flag> flags) {
        super(start_position, start_position+text.length(), Collections.<Lexeme>emptyList(), flags);
        if (text == null || "".equals(text))
            throw new IllegalArgumentException("Text of small addition cannot be NULL or empty.");
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SmallAddition that = (SmallAddition) o;

        if (!text.equals(that.text)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + text.hashCode();
        return result;
    }

    @Override
    String toXMLContents() {
        StringBuilder buffer = new StringBuilder(super.toXMLContents());
        buffer.append("  <text>");
        buffer.append(escapeText(text));
        buffer.append("</text>\n");
        return buffer.toString();
    }

    @Override
    public String toString() {
        return "<small_addition>\n"+toXMLContents()+"</small_addition>";
    }
}
