/*
 * #%L
 * LaTeX Track Changes (LTC) allows collaborators on a version-controlled LaTeX writing project to view and query changes in the .tex documents.
 * %%
 * Copyright (C) 2009 - 2012 SRI International
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
        assertAddition(0, 27, Lists.newArrayList(new IndexFlagsPair<Integer>(
                59,
                EnumSet.of(Change.Flag.COMMENT))));
        changes = getChanges(
                "Lorem ipsum dolor sit amet,",
                "Lorem ipsum dolor sit amet, "+
                        "%consectetur adipiscing elit. \n\n"+
                        "  Suspendisse sed sollicitudin orci.  ");
        assertAddition(0, 27, Lists.newArrayList(
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
        assertAddition(0, 26, Lists.newArrayList(
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
        assertAddition(0, 26, Lists.newArrayList(
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
        assertDeletion(0, 27, Lists.newArrayList(new IndexFlagsPair<String>(
                " \\consectetur \\adipiscing",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(
                "Lorem ipsum dolor sit amet, \\consectetur \\adipiscing    elit. \n",
                "Lorem ipsum dolor sit amet,  elit.\n");
        assertDeletion(0, 27, Lists.newArrayList(new IndexFlagsPair<String>(
                " \\consectetur \\adipiscing",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(
                "Lorem ipsum dolor sit amet, \\consectetur \n\\adipiscing",
                "Lorem ipsum dolor sit amet,  \n");
        assertDeletion(0, 27, Lists.newArrayList(new IndexFlagsPair<String>(
                " \\consectetur \n\\adipiscing",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(
                "Lorem ipsum dolor sit amet,  "+
                        "\\consectetur adipiscing    elit. \n\n",
                "Lorem ipsum dolor sit amet, \n"+
                        "  elit. \n\n");
        assertDeletion(0, 27, Lists.newArrayList(
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
        assertDeletion(0, 26, Lists.newArrayList(
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
        assertAddition(0, 26, Lists.newArrayList(new IndexFlagsPair<Integer>(
                42,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 26, Lists.newArrayList(
                new IndexFlagsPair<String>(
                        " \\consectetur",
                        EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND)),
                new IndexFlagsPair<String>(
                        " adipiscing  ",
                        EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "\n\nIf % or should this be ``When''?\nin the Course",
                "\n\nWhen in the Course");
        assertDeletion(0, 2, Lists.newArrayList(
                new IndexFlagsPair<String>(
                        "If % or should this be ``",
                        EnumSet.of(Change.Flag.DELETION))));
        assertDeletion(1, 6, Lists.newArrayList(
                new IndexFlagsPair<String>(
                        "''?",
                        EnumSet.of(Change.Flag.DELETION))));
    }
}
