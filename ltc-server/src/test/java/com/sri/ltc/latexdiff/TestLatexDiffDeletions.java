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
public final class TestLatexDiffDeletions extends TestLatexDiff {

    @Test
    public void deletionsAtBeginning() throws Exception {
        changes = getChanges(
                "Lorem ipsum",
                " ipsum"
        );
        assertDeletion(0, 0, Lists.newArrayList(new IndexFlagsPair<String>(
                "Lorem", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem ipsum",
                "ipsum"
        );
        assertDeletion(0, 0, Lists.newArrayList(new IndexFlagsPair<String>(
                "Lorem ", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                ":ipsum",
                "ipsum"
        );
        assertDeletion(0, 0, Lists.newArrayList(new IndexFlagsPair<String>(
                ":", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                " Lorem ipsum",
                "  ipsum"
        );
        assertDeletion(0, 0, Lists.newArrayList(new IndexFlagsPair<String>(
                " Lorem", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                " Lorem ipsum",
                "ipsum"
        );
        assertDeletion(0, 0, Lists.newArrayList(new IndexFlagsPair<String>(
                " Lorem ", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                " :ipsum",
                "ipsum"
        );
        assertDeletion(0, 0, Lists.newArrayList(new IndexFlagsPair<String>(
                " :", EnumSet.of(Change.Flag.DELETION))));
    }

    @Test
    public void deletionsInMiddle() throws Exception {
        changes = getChanges(
                "Lorem ipsum dolor",
                "Lorem  dolor"
        );
        assertDeletion(0, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                " ipsum", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem; ipsum",
                "Lorem  ipsum"
        );
        assertDeletion(0, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                ";", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem; \\ipsum",
                "Lorem\\ipsum"
        );
        assertDeletion(0, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                "; ", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem :ipsum",
                "Lorem  ipsum"
        );
        assertDeletion(0, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                " :", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem:\\ipsum",
                "Lorem\\ipsum"
        );
        assertDeletion(0, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                ":", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem ipsum dolor",
                "Lorem  dolor"
        );
        assertDeletion(0, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                " ipsum", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem; ipsum",
                "Lorem  ipsum"
        );
        assertDeletion(0, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                ";", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem :\\ipsum",
                "Lorem\\ipsum"
        );
        assertDeletion(0, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                " :", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem:\\ipsum",
                "Lorem  \\ipsum"
        );
        assertDeletion(0, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                ":", EnumSet.of(Change.Flag.DELETION)))); // TODO: or (7, 1)?
    }

    @Test
    public void deletionsAtEnd() throws Exception {
        changes = getChanges(
                "Lorem ipsum",
                "Lorem"
        );
        assertDeletion(0, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                " ipsum", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem ipsum",
                "Lorem  "
        );
        assertDeletion(0, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                " ipsum", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem:",
                "Lorem "
        );
        assertDeletion(0, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                ":", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem: ",
                "Lorem  "
        );
        assertDeletion(0, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                ":", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem ipsum ",
                "Lorem  "
        );
        assertDeletion(0, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                " ipsum", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem ipsum ",
                "Lorem"
        );
        assertDeletion(0, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                " ipsum ", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem  ipsum",
                "Lorem"
        );
        assertDeletion(0, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                "  ipsum", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "  Lorem ",
                ""
        );
        assertDeletion(0, 0, Lists.newArrayList(new IndexFlagsPair<String>(
                "  Lorem ", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem; ",
                "Lorem  "
        );
        assertDeletion(0, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                ";", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem; ",
                "Lorem"
        );
        assertDeletion(0, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                "; ", EnumSet.of(Change.Flag.DELETION))));
    }

    @Test
    public void examples() throws Exception {
        changes = getChanges(
                " hello\\cite{ABC} world!",
                "hello  world!");
        assertDeletion(0, 5, Lists.newArrayList(
                new IndexFlagsPair<String>("\\cite", EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND)),
                new IndexFlagsPair<String>("{ABC}", EnumSet.of(Change.Flag.DELETION))));
    }
}
