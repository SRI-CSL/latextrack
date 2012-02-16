/**
 ************************ 80 columns *******************************************
 * SmallAddition
 *
 * Created on Jan 6, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import java.util.EnumSet;

/**
 * @author linda
 */
public final class SmallAddition extends Addition {

    public SmallAddition(int start_position, int end_position, EnumSet<Flag> flags) {
        super(start_position, end_position, flags);
    }

    @Override
    public String toString() {
        return "<small_addition>\n"+toXMLContents()+"</small_addition>";
    }
}
