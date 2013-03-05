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
@Ignore
public final class TestLatexDiffAdditions extends TestLatexDiff {

    @Test
    public void additionsAtBeginning() throws Exception {
        changes = getChanges(
                "\\ipsum  ",
                "Lorem\\ipsum");
        assertAddition(0, 0, Lists.newArrayList(new IndexFlagsPair<Integer>(
                5,
                EnumSet.noneOf(Change.Flag.class)))); // case A
        changes = getChanges(
                "\\ipsum  ",
                " Lorem\\ipsum");
        assertAddition(0, 0, Lists.newArrayList(new IndexFlagsPair<Integer>(
                6,
                EnumSet.noneOf(Change.Flag.class)))); // case B
        changes = getChanges(
                "ipsum",
                "Lorem ipsum ");
        assertAddition(0, 0, Lists.newArrayList(new IndexFlagsPair<Integer>(
                6,
                EnumSet.noneOf(Change.Flag.class)))); // case Ca
        changes = getChanges(
                "ipsum  ",
                "Lorem ipsum ");
        assertAddition(0, 0, Lists.newArrayList(new IndexFlagsPair<Integer>(
                6,
                EnumSet.noneOf(Change.Flag.class)))); // case Cb
        changes = getChanges(
                "ipsum",
                "   Lorem ipsum ");
        assertAddition(0, 0, Lists.newArrayList(new IndexFlagsPair<Integer>(
                9,
                EnumSet.noneOf(Change.Flag.class)))); // case D
        changes = getChanges(
                "  \\ipsum  ",
                "Lorem\\ipsum");
        assertAddition(0, 0, Lists.newArrayList(new IndexFlagsPair<Integer>(
                5,
                EnumSet.noneOf(Change.Flag.class)))); // case E
        changes = getChanges(
                "  \\ipsum  ",
                " Lorem\\ipsum");
        assertAddition(0, 0, Lists.newArrayList(new IndexFlagsPair<Integer>(
                6,
                EnumSet.noneOf(Change.Flag.class)))); // case F
        changes = getChanges(
                "  ipsum  ",
                "Lorem ipsum ");
        assertAddition(0, 0, Lists.newArrayList(new IndexFlagsPair<Integer>(
                6,
                EnumSet.noneOf(Change.Flag.class)))); // case G
        changes = getChanges(
                "  ipsum  ",
                "   Lorem ipsum ");
        assertAddition(0, 0, Lists.newArrayList(new IndexFlagsPair<Integer>(
                9,
                EnumSet.noneOf(Change.Flag.class)))); // case Ha
        changes = getChanges(
                "  \\ipsum  ",
                " Lorem \\ipsum");
        assertAddition(0, 0, Lists.newArrayList(new IndexFlagsPair<Integer>(
                7,
                EnumSet.noneOf(Change.Flag.class)))); // case Hb
    }

    @Test
    public void additionsInMiddle() throws Exception {
        changes = getChanges(
                "Lorem  sit amet. ",
                "Lorem ipsum dolor sit amet. ");
        assertAddition(0, 5, Lists.newArrayList(new IndexFlagsPair<Integer>(
                18,
                EnumSet.noneOf(Change.Flag.class))));
        changes = getChanges(
                "Lorem ipsum dolor sit amet",
                "  Lorem ipsum dolor sit amet. ");
        assertAddition(0, 28, Lists.newArrayList(new IndexFlagsPair<Integer>(
                30,
                EnumSet.noneOf(Change.Flag.class))));
    }

    @Test
    public void additionsAtEnd() throws Exception {
        changes = getChanges(
                "Lorem",
                "Lorem ipsum ");
        assertAddition(0, 5, Lists.newArrayList(new IndexFlagsPair<Integer>(
                12,
                EnumSet.noneOf(Change.Flag.class))));
        changes = getChanges(
                "Lorem  ",
                "Lorem ipsum ");
        assertAddition(0, 5, Lists.newArrayList(new IndexFlagsPair<Integer>(
                12,
                EnumSet.noneOf(Change.Flag.class))));
        changes = getChanges(
                " Lorem  ",
                "Lorem ipsum ");
        assertAddition(0, 5, Lists.newArrayList(new IndexFlagsPair<Integer>(
                12,
                EnumSet.noneOf(Change.Flag.class))));
        changes = getChanges(
                " Lorem",
                "Lorem ipsum ");
        assertAddition(0, 5, Lists.newArrayList(new IndexFlagsPair<Integer>(
                12,
                EnumSet.noneOf(Change.Flag.class))));
        changes = getChanges(
                "Lorem  ",
                "Lorem\\ipsum");
        assertAddition(0, 5, Lists.newArrayList(new IndexFlagsPair<Integer>(
                11,
                EnumSet.of(Change.Flag.COMMAND))));
        changes = getChanges(
                " Lorem  ",
                "Lorem\\ipsum ");
        assertAddition(0, 5, Lists.newArrayList(new IndexFlagsPair<Integer>(
                12,
                EnumSet.of(Change.Flag.COMMAND))));
    }
}
