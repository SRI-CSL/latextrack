/*
 * #%L
 * LaTeX Track Changes (LTC) allows collaborators on a version-controlled LaTeX writing project to view and query changes in the .tex documents.
 * %%
 * Copyright (C) 2009 - 2013 SRI International
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package com.sri.ltc.latexdiff;

import org.junit.Test;

import java.util.regex.Matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Testing regular expression pattern to match leading white space in front of suppressed COMMENTs.
 * Need to match consecutive white space until either a paragraph (with potential white space in between) or
 * a non-whitespace character.
 *
 * @author linda
 */
public final class TestLeadingWhitespace {

    private void matches(String s, int ending) { // TODO: later add ending index
        Matcher matcher = MarkedUpDocument.LEADING_WHITE.matcher(s);
        assertTrue("String \"" + s + "\" matches regex", matcher.matches());
        assertEquals("ending index is", ending, matcher.end(1));
    }

    @Test
    public void nonMatching() {
        for (String s : new String[] {
                "",
                "sad blub",
                "sad\n  \n\nblub",
                "brr\n\n\n\n",
                "blub \n",
                "blub\t",
            }) {
            Matcher matcher = MarkedUpDocument.LEADING_WHITE.matcher(s);
            assertFalse("String \"" + s + "\" does not match regex", matcher.matches());
        }
    }

    @Test
    public void matchingWithSpaces() {
        matches(" ", 1);
        matches("  bla\n\n", 2);
        matches("    ", 4);
    }

    @Test
    public void matchingWithTabs() {
        matches("\t ", 2);
        matches("\tbla", 1);
        matches(" \t  bla", 4);
    }

    @Test
    public void matchingWithOneNewline() {
        matches("\t  \n", 4);
        matches("\n \t", 3);
        matches("\t \n", 3);
        matches("\nbla", 1);
    }

    @Test
    public void matchingWithMoreNewlines() {
        matches(" \n\n ", 4);
        matches("  \n\t\n", 5);
        matches(" \t\n \nbla", 5);
        matches("\nbla\n", 1);
    }
}
