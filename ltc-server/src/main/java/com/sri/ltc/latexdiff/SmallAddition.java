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

/**
 * @author linda
 */
public class SmallAddition extends Addition {

    public SmallAddition(int start_position, String text, boolean inPreamble, boolean inComment, boolean isCommand) {
        super(start_position, text, Collections.<Lexeme>emptyList(), inPreamble, inComment, isCommand);
    }

    @Override
    public String toString() {
        return "<small_addition>\n"+toXMLContents()+"</small_addition>";
    }
}
