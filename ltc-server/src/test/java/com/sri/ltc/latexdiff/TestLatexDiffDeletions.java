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
 * @author linda
 */
public final class TestLatexDiffDeletions extends TestLatexDiff {

    @Test
    public void deletionsAtBeginning() throws IOException {
        changes = getChanges(
                "Lorem ipsum",
                " ipsum"
        );
        assertDeletion(0, 0, 5, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem ipsum",
                "ipsum"
        );
        assertDeletion(0, 0, 6, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                ":ipsum",
                "ipsum"
        );
        assertDeletion(0, 0, 1, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                " Lorem ipsum",
                "  ipsum"
        );
        assertDeletion(0, 0, 6, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                " Lorem ipsum",
                "ipsum"
        );
        assertDeletion(0, 0, 7, EnumSet.of(Change.Flag.DELETION)); // TODO: or (0, 6)?
        changes = getChanges(
                " :ipsum",
                "ipsum"
        );
        assertDeletion(0, 0, 2, EnumSet.of(Change.Flag.DELETION)); // TODO: or (0, 1)?
    }

    @Test
    public void deletionsInMiddle() throws IOException {
        changes = getChanges(
                "Lorem ipsum dolor",
                "Lorem  dolor"
        );
        assertDeletion(0, 5, 6, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem; ipsum",
                "Lorem  ipsum"
        );
        assertDeletion(0, 5, 1, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem; \\ipsum",
                "Lorem\\ipsum"
        );
        assertDeletion(0, 5, 2, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem :ipsum",
                "Lorem  ipsum"
        );
        assertDeletion(0, 5, 2, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem:\\ipsum",
                "Lorem\\ipsum"
        );
        assertDeletion(0, 5, 1, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem ipsum dolor",
                "Lorem  dolor"
        );
        assertDeletion(0, 5, 6, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem; ipsum",
                "Lorem  ipsum"
        );
        assertDeletion(0, 5, 1, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem :\\ipsum",
                "Lorem\\ipsum"
        );
        assertDeletion(0, 5, 2, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem:\\ipsum",
                "Lorem  \\ipsum"
        );
        assertDeletion(0, 5, 1, EnumSet.of(Change.Flag.DELETION)); // TODO: or (7, 1)?
    }

    @Test
    public void deletionsAtEnd() throws IOException {
        changes = getChanges(
                "Lorem ipsum",
                "Lorem"
        );
        assertDeletion(0, 5, 6, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem ipsum",
                "Lorem  "
        );
        assertDeletion(0, 5, 6, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem:",
                "Lorem "
        );
        assertDeletion(0, 5, 1, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem: ",
                "Lorem  "
        );
        assertDeletion(0, 5, 1, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem ipsum ",
                "Lorem  "
        );
        assertDeletion(0, 5, 6, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem ipsum ",
                "Lorem"
        );
        assertDeletion(0, 5, 7, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem  ipsum",
                "Lorem"
        );
        assertDeletion(0, 5, 7, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "  Lorem ",
                ""
        );
        assertDeletion(0, 0, 8, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem; ",
                "Lorem  "
        );
        assertDeletion(0, 5, 1, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem; ",
                "Lorem"
        );
        assertDeletion(0, 5, 2, EnumSet.of(Change.Flag.DELETION));
    }

}
