/**
 ************************ 80 columns *******************************************
 * Deletion
 *
 * Created on Jan 6, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * @author linda
 */
public class Deletion extends Change<String> {

    public final String text;

    public Deletion(int start_position, String text, List<IndexFlagsPair<String>> flags) {
        super(start_position, flags);
        if (text == null || "".equals(text))
            throw new IllegalArgumentException("Text of deletion cannot be NULL or empty.");
        this.text = text;
    }

    @Override
    public ImmutableList<IndexFlagsPair<String>> getFlags() {
        return super.getFlags();
    }

    @Override
    public String toString() {
        return "<deletion>\n"+toXMLContents()+"</deletion>";
    }
}
