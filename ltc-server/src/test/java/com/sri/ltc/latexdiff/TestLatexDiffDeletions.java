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
import org.junit.Test;

import java.io.IOException;
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
        assertDeletion(0, 0, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                "Lorem", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem ipsum",
                "ipsum"
        );
        assertDeletion(0, 0, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "Lorem ", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                ":ipsum",
                "ipsum"
        );
        assertDeletion(0, 0, 1, Lists.newArrayList(new IndexFlagsPair<String>(
                ":", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                " Lorem ipsum",
                "  ipsum"
        );
        assertDeletion(0, 0, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                " Lorem", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                " Lorem ipsum",
                "ipsum"
        );
        assertDeletion(0, 0, 7, Lists.newArrayList(new IndexFlagsPair<String>(
                " Lorem ", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                " :ipsum",
                "ipsum"
        );
        assertDeletion(0, 0, 2, Lists.newArrayList(new IndexFlagsPair<String>(
                " :", EnumSet.of(Change.Flag.DELETION))));
    }

    @Test
    public void deletionsInMiddle() throws Exception {
        changes = getChanges(
                "Lorem ipsum dolor",
                "Lorem  dolor"
        );
        assertDeletion(0, 5, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                " ipsum", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem; ipsum",
                "Lorem  ipsum"
        );
        assertDeletion(0, 5, 1, Lists.newArrayList(new IndexFlagsPair<String>(
                ";", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem; \\ipsum",
                "Lorem\\ipsum"
        );
        assertDeletion(0, 5, 2, Lists.newArrayList(new IndexFlagsPair<String>(
                "; ", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem :ipsum",
                "Lorem  ipsum"
        );
        assertDeletion(0, 5, 2, Lists.newArrayList(new IndexFlagsPair<String>(
                " :", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem:\\ipsum",
                "Lorem\\ipsum"
        );
        assertDeletion(0, 5, 1, Lists.newArrayList(new IndexFlagsPair<String>(
                ":", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem ipsum dolor",
                "Lorem  dolor"
        );
        assertDeletion(0, 5, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                " ipsum", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem; ipsum",
                "Lorem  ipsum"
        );
        assertDeletion(0, 5, 1, Lists.newArrayList(new IndexFlagsPair<String>(
                ";", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem :\\ipsum",
                "Lorem\\ipsum"
        );
        assertDeletion(0, 5, 2, Lists.newArrayList(new IndexFlagsPair<String>(
                " :", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem:\\ipsum",
                "Lorem  \\ipsum"
        );
        assertDeletion(0, 5, 1, Lists.newArrayList(new IndexFlagsPair<String>(
                ":", EnumSet.of(Change.Flag.DELETION)))); // TODO: or (7, 1)?
    }

    @Test
    public void deletionsAtEnd() throws Exception {
        changes = getChanges(
                "Lorem ipsum",
                "Lorem"
        );
        assertDeletion(0, 5, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                " ipsum", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem ipsum",
                "Lorem  "
        );
        assertDeletion(0, 5, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                " ipsum", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem:",
                "Lorem "
        );
        assertDeletion(0, 5, 1, Lists.newArrayList(new IndexFlagsPair<String>(
                ":", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem: ",
                "Lorem  "
        );
        assertDeletion(0, 5, 1, Lists.newArrayList(new IndexFlagsPair<String>(
                ":", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem ipsum ",
                "Lorem  "
        );
        assertDeletion(0, 5, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                " ipsum", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem ipsum ",
                "Lorem"
        );
        assertDeletion(0, 5, 7, Lists.newArrayList(new IndexFlagsPair<String>(
                " ipsum ", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem  ipsum",
                "Lorem"
        );
        assertDeletion(0, 5, 7, Lists.newArrayList(new IndexFlagsPair<String>(
                "  ipsum", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "  Lorem ",
                ""
        );
        assertDeletion(0, 0, 8, Lists.newArrayList(new IndexFlagsPair<String>(
                "  Lorem ", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem; ",
                "Lorem  "
        );
        assertDeletion(0, 5, 1, Lists.newArrayList(new IndexFlagsPair<String>(
                ";", EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem; ",
                "Lorem"
        );
        assertDeletion(0, 5, 2, Lists.newArrayList(new IndexFlagsPair<String>(
                "; ", EnumSet.of(Change.Flag.DELETION))));
    }

    @Test
    public void examples() throws Exception {
        changes = getChanges(
                " hello\\cite{ABC} world!",
                "hello  world!");
        assertDeletion(0, 5, 10, Lists.newArrayList(
                new IndexFlagsPair<String>("\\cite", EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND)),
                new IndexFlagsPair<String>("{ABC}", EnumSet.of(Change.Flag.DELETION))));
    }
}
