/**
 ************************ 80 columns *******************************************
 * Deletion
 *
 * Created on Jan 6, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

/**
 * @author linda
 */
public class Deletion extends Change {

    public final String text;

    public Deletion(int start_position, String text, boolean inPreamble, boolean inComment, boolean isCommand) {
        super(start_position, inPreamble, inComment, isCommand);
        if (text == null || "".equals(text))
            throw new IllegalArgumentException("Text of deletion cannot be NULL or empty.");
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Deletion deletion = (Deletion) o;

        if (!text.equals(deletion.text)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + text.hashCode();
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
        return "<deletion>\n"+toXMLContents()+"</deletion>";
    }
}
