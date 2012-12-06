package com.sri.ltc.latexdiff;

import java.util.*;

/**
 * @author linda
 */
public final class Addition extends Change<Integer> {

    public Addition(int start_position, List<IndexFlagsPair<Integer>> flags) {
        super(start_position, flags);
    }

    @Override
    public String toString() {
        return "<addition>\n"+toXMLContents()+"</addition>";
    }
}
