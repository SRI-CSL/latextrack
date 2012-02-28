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
import java.util.EnumSet;
import java.util.List;

/**
 * @author linda
 */
public class Addition extends Change {

    public final int end_position;

    public Addition(int start_position, int end_position, EnumSet<Flag> flags) {
        super(start_position, flags);
        this.end_position = end_position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Addition)) return false;
        if (!super.equals(o)) return false;

        Addition addition = (Addition) o;

        if (end_position != addition.end_position) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + end_position;
        return result;
    }

    @Override
    String toXMLContents() {
        StringBuilder buffer = new StringBuilder(super.toXMLContents());
        buffer.append("  <end position="+end_position);
        buffer.append(" />\n");
        return buffer.toString();
    }

    @Override
    public String toString() {
        return "<addition>\n"+toXMLContents()+"</addition>";
    }
}
