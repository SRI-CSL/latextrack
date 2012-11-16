/**
 ************************ 80 columns *******************************************
 * TestLatexDiffDeletions
 *
 * Created on 2/8/12.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import com.google.common.collect.Lists;
import org.junit.Ignore;
import org.junit.Test;

import java.util.EnumSet;

/**
 * Testing changes with mixed type lexemes.
 * @author linda
 */
@Ignore
public final class TestLatexDiffMixed extends TestLatexDiff {

    @Test
    public void commentsAdded() throws Exception {
        changes = getChanges(
                "Lorem ipsum dolor sit amet,",
                "Lorem ipsum dolor sit amet, "+
                        "%consectetur adipiscing elit.  ");
        assertAddition(0, 27, 59, Lists.newArrayList(new IndexFlagsPair<Integer>(
                59,
                EnumSet.of(Change.Flag.COMMENT))));
        changes = getChanges(
                "Lorem ipsum dolor sit amet,",
                "Lorem ipsum dolor sit amet, "+
                        "%consectetur adipiscing elit. \n\n"+
                        "  Suspendisse sed sollicitudin orci.  ");
        assertAddition(0, 27, 57, Lists.newArrayList(
                new IndexFlagsPair<Integer>(
                        57,
                        EnumSet.of(Change.Flag.COMMENT)),
                new IndexFlagsPair<Integer>(
                        98,
                        EnumSet.noneOf(Change.Flag.class))));
        changes = getChanges(
                "Lorem ipsum dolor sit amet  ",
                "Lorem ipsum dolor sit amet, "+
                        "%consectetur adipiscing elit. \n");
        assertAddition(0, 26, 27, Lists.newArrayList(
                new IndexFlagsPair<Integer>(
                        28,
                        EnumSet.noneOf(Change.Flag.class)),
                new IndexFlagsPair<Integer>(
                        59,
                        EnumSet.of(Change.Flag.COMMENT))));
        changes = getChanges(
                "Lorem ipsum dolor sit amet \n",
                "Lorem ipsum dolor sit amet, \n\n"+
                        "%consectetur adipiscing elit. \n"+
                        "  Suspendisse sed sollicitudin orci.  ");
        assertAddition(0, 26, 30, Lists.newArrayList(
                new IndexFlagsPair<Integer>(
                        30,
                        EnumSet.noneOf(Change.Flag.class)),
                new IndexFlagsPair<Integer>(
                        59,
                        EnumSet.of(Change.Flag.COMMENT)),
                new IndexFlagsPair<Integer>(
                        99,
                        EnumSet.noneOf(Change.Flag.class))));
    }

    @Test
    public void commandsDeleted() throws Exception {
        changes = getChanges(
                "Lorem ipsum dolor sit amet, \\consectetur \\adipiscing",
                "Lorem ipsum dolor sit amet,");
        assertDeletion(0, 27, 25, Lists.newArrayList(new IndexFlagsPair<String>(
                " \\consectetur \\adipiscing",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(
                "Lorem ipsum dolor sit amet, \\consectetur \\adipiscing    elit. \n",
                "Lorem ipsum dolor sit amet,  elit.\n");
        assertDeletion(0, 27, 25, Lists.newArrayList(new IndexFlagsPair<String>(
                " \\consectetur \\adipiscing",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(
                "Lorem ipsum dolor sit amet, \\consectetur \n\\adipiscing",
                "Lorem ipsum dolor sit amet,  \n");
        assertDeletion(0, 27, 26, Lists.newArrayList(new IndexFlagsPair<String>(
                " \\consectetur \n\\adipiscing",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(
                "Lorem ipsum dolor sit amet,  "+
                        "\\consectetur adipiscing    elit. \n\n",
                "Lorem ipsum dolor sit amet, \n"+
                        "  elit. \n\n");
        assertDeletion(0, 27, 14, Lists.newArrayList(
                new IndexFlagsPair<String>(
                        "  \\consectetur",
                        EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND)),
                new IndexFlagsPair<String>(
                        " adipiscing",
                        EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem ipsum dolor sit amet,  "+
                        "\\consectetur adipiscing   elit. \n\n",
                "Lorem ipsum dolor sit amet  elit. \n\n");
        assertDeletion(0, 26, 1, Lists.newArrayList(
                new IndexFlagsPair<String>(
                        ",  ",
                        EnumSet.of(Change.Flag.DELETION)),
                new IndexFlagsPair<String>(
                        "\\consectetur",
                        EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND)),
                new IndexFlagsPair<String>(
                        " adipiscing",
                        EnumSet.of(Change.Flag.DELETION))));
    }

    @Test
    public void replacementsMixed() throws Exception {
        changes = getChanges(
                "Lorem ipsum dolor sit amet \\consectetur adipiscing  elit. ",
                "Lorem ipsum dolor sit amet, consectetur   elit.");
        assertAddition(0, 26, 42, Lists.newArrayList(new IndexFlagsPair<Integer>(
                42,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 26, 13, Lists.newArrayList(
                new IndexFlagsPair<String>(
                        " \\consectetur",
                        EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND)),
                new IndexFlagsPair<String>(
                        " adipiscing  ",
                        EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "\n\nIf % or should this be ``When''?\nin the Course",
                "\n\nWhen in the Course");
        assertDeletion(0, 2, 25, Lists.newArrayList(
                new IndexFlagsPair<String>(
                        "If % or should this be ``",
                        EnumSet.of(Change.Flag.DELETION))));
        assertDeletion(1, 6, 3, Lists.newArrayList(
                new IndexFlagsPair<String>(
                        "''?",
                        EnumSet.of(Change.Flag.DELETION))));
    }
}
