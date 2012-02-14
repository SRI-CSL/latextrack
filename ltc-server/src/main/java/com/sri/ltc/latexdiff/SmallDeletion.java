/**
 ************************ 80 columns *******************************************
 * SmallDeletion
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
public final class SmallDeletion extends Deletion {

    public SmallDeletion(int start_position, String text, EnumSet<Flag> flags) {
        super(start_position, text, flags);
    }

    @Override
    public String toString() {
        return "<small_deletion>\n"+toXMLContents()+"</small_deletion>";
    }
}
