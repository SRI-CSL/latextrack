/**
 ************************ 80 columns *******************************************
 * TestLatexDiffDeletions
 *
 * Created on 2/8/12.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import org.junit.Test;

import java.io.IOException;
import java.util.EnumSet;

/**
 * Testing changes with mixed type lexemes.
 * @author linda
 */
public final class TestLatexDiffMixed extends TestLatexDiff {

    @Test
    public void commentsAdded() throws IOException {
        changes = getChanges(
                "Lorem ipsum dolor sit amet,",
                "Lorem ipsum dolor sit amet, "+
                        "%consectetur adipiscing elit.  ");
        assertAddition(0, 27, 59, EnumSet.of(Change.Flag.COMMENT));
        changes = getChanges(
                "Lorem ipsum dolor sit amet,",
                "Lorem ipsum dolor sit amet, "+
                        "%consectetur adipiscing elit. \n\n"+
                        "  Suspendisse sed sollicitudin orci.  ");
        assertAddition(0, 27, 58, EnumSet.of(Change.Flag.COMMENT));
        assertAddition(1, 58, 98, EnumSet.noneOf(Change.Flag.class));
        changes = getChanges(
                "Lorem ipsum dolor sit amet  ",
                "Lorem ipsum dolor sit amet, "+
                        "%consectetur adipiscing elit. \n");
        assertAddition(0, 26, 28, EnumSet.noneOf(Change.Flag.class));
        assertAddition(1, 28, 59, EnumSet.of(Change.Flag.COMMENT));
        changes = getChanges(
                "Lorem ipsum dolor sit amet \n",
                "Lorem ipsum dolor sit amet, \n\n"+
                        "%consectetur adipiscing elit. \n"+
                        "  Suspendisse sed sollicitudin orci.  ");
        assertAddition(0, 26, 30, EnumSet.noneOf(Change.Flag.class));
        assertAddition(1, 30, 63, EnumSet.of(Change.Flag.COMMENT));
        assertAddition(2, 63, 99, EnumSet.noneOf(Change.Flag.class));
    }

    @Test
    public void commandsDeleted() throws IOException {
        changes = getChanges(
                "Lorem ipsum dolor sit amet, \\consectetur \\adipiscing",
                "Lorem ipsum dolor sit amet,");
        assertDeletion(0, 27, 25, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND));
        changes = getChanges(
                "Lorem ipsum dolor sit amet, \\consectetur \\adipiscing    elit. \n",
                "Lorem ipsum dolor sit amet,  elit.\n");
        assertDeletion(0, 27, 25, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND));
        changes = getChanges(
                "Lorem ipsum dolor sit amet, \\consectetur \n\\adipiscing",
                "Lorem ipsum dolor sit amet,  \n");
        assertDeletion(0, 27, 26, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND));
        changes = getChanges(
                "Lorem ipsum dolor sit amet,  "+
                        "\\consectetur adipiscing    elit. \n\n",
                "Lorem ipsum dolor sit amet, \n"+
                        "  elit. \n\n");
        assertDeletion(0, 27, 14, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND));
        assertDeletion(1, 41, 11, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem ipsum dolor sit amet,  "+
                        "\\consectetur adipiscing   elit. \n\n",
                "Lorem ipsum dolor sit amet  elit. \n\n");
        assertDeletion(0, 26, 1, EnumSet.of(Change.Flag.DELETION));
        assertDeletion(1, 27, 14, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND));
        assertDeletion(2, 41, 11, EnumSet.of(Change.Flag.DELETION));
    }

    @Test
    public void replacementsMixed() throws IOException {
        changes = getChanges(
                "Lorem ipsum dolor sit amet \\consectetur adipiscing  elit. ",
                "Lorem ipsum dolor sit amet, consectetur   elit.");
        assertAddition(0, 26, 42, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(1, 26, 13, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND));
        assertDeletion(2, 39, 13, EnumSet.of(Change.Flag.DELETION));
    }
}
