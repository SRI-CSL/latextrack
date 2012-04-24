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
public final class Deletion extends Change<String> {

    public Deletion(int start_position, List<IndexFlagsPair<String>> flags) {
        super(start_position, flags);
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
