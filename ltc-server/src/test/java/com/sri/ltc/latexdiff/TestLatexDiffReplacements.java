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
 * @author linda
 */
public final class TestLatexDiffReplacements extends TestLatexDiff {

    @Test
    public void replacements() throws Exception {
        changes = getChanges(  // case Aa
                "Lorem\\amet.",
                " Lorem, dolor. ");
        assertAddition(0, 6, Lists.newArrayList(new IndexFlagsPair<Integer>(
                13,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case Ab
                "\\amet.",
                "dolor. ");
        assertAddition(0, 0, Lists.newArrayList(new IndexFlagsPair<Integer>(
                5,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 0, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet ",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case Ba
                "Lorem   amet.",
                " Lorem, dolor. ");
        assertAddition(0, 6, Lists.newArrayList(new IndexFlagsPair<Integer>(
                13,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(  // case Bb
                "   amet.",
                "dolor. ");
        assertAddition(0, 0, Lists.newArrayList(new IndexFlagsPair<Integer>(
                5,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 0, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet ",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(  // case C
                "Lorem\\amet .",
                " Lorem, dolor. ");
        assertAddition(0, 6, Lists.newArrayList(new IndexFlagsPair<Integer>(
                13,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet ",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case D
                "Lorem   amet .",
                " Lorem, dolor. ");
        assertAddition(0, 6, Lists.newArrayList(new IndexFlagsPair<Integer>(
                13,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet ",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(  // case E
                "Lorem\\amet.",
                " Lorem ipsum dolor. ");
        assertAddition(0, 6, Lists.newArrayList(new IndexFlagsPair<Integer>(
                18,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case F
                "Lorem   amet.",
                " Lorem ipsum dolor. ");
        assertAddition(0, 6, Lists.newArrayList(new IndexFlagsPair<Integer>(
                18,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(  // case G
                "Lorem\\amet  .",
                " Lorem ipsum dolor. ");
        assertAddition(0, 6, Lists.newArrayList(new IndexFlagsPair<Integer>(
                18,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case H
                "Lorem   amet  .",
                " Lorem ipsum dolor. ");
        assertAddition(0, 6, Lists.newArrayList(new IndexFlagsPair<Integer>(
                18,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(  // case Ia
                "Lorem\\amet.",
                " Lorem, dolor . ");
        assertAddition(0, 6, Lists.newArrayList(new IndexFlagsPair<Integer>(
                14,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case Ib
                "\\amet.",
                "dolor . ");
        assertAddition(0, 0, Lists.newArrayList(new IndexFlagsPair<Integer>(
                6,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 0, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet ",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case Ja
                "Lorem   amet.",
                " Lorem, dolor . ");
        assertAddition(0, 6, Lists.newArrayList(new IndexFlagsPair<Integer>(
                14,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(  // case Jb
                "   amet.",
                "dolor . ");
        assertAddition(0, 0, Lists.newArrayList(new IndexFlagsPair<Integer>(
                6,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 0, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet ",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(  // case K
                "Lorem\\amet  sit.",
                " Lorem, dolor sit. ");
        assertAddition(0, 6, Lists.newArrayList(new IndexFlagsPair<Integer>(
                14,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet  ",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case L
                "Lorem   amet  sit.",
                " Lorem, dolor sit. ");
        assertAddition(0, 6, Lists.newArrayList(new IndexFlagsPair<Integer>(
                14,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet  ",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(  // case M
                "Lorem\\amet.",
                " Lorem ipsum dolor . ");
        assertAddition(0, 6, Lists.newArrayList(new IndexFlagsPair<Integer>(
                19,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case N
                "Lorem   amet.",
                " Lorem ipsum dolor . ");
        assertAddition(0, 6, Lists.newArrayList(new IndexFlagsPair<Integer>(
                19,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(  // case O
                "Lorem\\amet  sit.",
                " Lorem ipsum dolor sit. ");
        assertAddition(0, 6, Lists.newArrayList(new IndexFlagsPair<Integer>(
                19,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case P
                "Lorem   amet  sit.",
                " Lorem ipsum dolor sit. ");
        assertAddition(0, 6, Lists.newArrayList(new IndexFlagsPair<Integer>(
                19,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet",
                EnumSet.of(Change.Flag.DELETION))));
    }
}
