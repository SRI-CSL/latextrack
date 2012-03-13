/**
 ************************ 80 columns *******************************************
 * Addition
 *
 * Created on Jan 6, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import java.util.*;

/**
 * @author linda
 */
public class Addition extends Change<Integer> {

    public final int end_position;

    public Addition(int start_position, int end_position, List<IndexFlagsPair<Integer>> flags) {
        super(start_position, flags);
        this.end_position = end_position;
    }

    @Override
    public String toString() {
        return "<addition>\n"+toXMLContents()+"</addition>";
    }
}
