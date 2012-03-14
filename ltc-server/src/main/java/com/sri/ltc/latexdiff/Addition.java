/**
 ************************ 80 columns *******************************************
 * Addition
 *
 * Created on Jan 6, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import com.google.common.collect.ImmutableList;

import java.util.*;

/**
 * @author linda
 */
public class Addition extends Change<Integer> {

    public Addition(int start_position, List<IndexFlagsPair<Integer>> flags) {
        super(start_position, flags);
    }

    @Override
    public ImmutableList<IndexFlagsPair<Integer>> getFlags() {
        return super.getFlags();
    }

    @Override
    public String toString() {
        return "<addition>\n"+toXMLContents()+"</addition>";
    }
}
