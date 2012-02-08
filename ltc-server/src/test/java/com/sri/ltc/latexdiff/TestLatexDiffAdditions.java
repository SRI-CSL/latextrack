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

/**
 * @author linda
 */
public final class TestLatexDiffAdditions extends TestLatexDiff {

    @Test
    public void additionsAtBeginning() throws IOException {
        changes = getChanges(
                "\\ipsum  ",
                "Lorem\\ipsum");
        assertAddition(0, 0, 5); // case A
        changes = getChanges(
                "\\ipsum  ",
                " Lorem\\ipsum");
        assertAddition(0, 0, 6); // case B
        changes = getChanges(
                "ipsum",
                "Lorem ipsum ");
        assertAddition(0, 0, 6); // case Ca
        changes = getChanges(
                "ipsum  ",
                "Lorem ipsum ");
        assertAddition(0, 0, 6); // case Cb
        changes = getChanges(
                "ipsum",
                "   Lorem ipsum ");
        assertAddition(0, 0, 9); // case D
        changes = getChanges(
                "  \\ipsum  ",
                "Lorem\\ipsum");
        assertAddition(0, 0, 5); // case E
        changes = getChanges(
                "  \\ipsum  ",
                " Lorem\\ipsum");
        assertAddition(0, 1, 6); // case F
        changes = getChanges(
                "  ipsum  ",
                "Lorem ipsum ");
        assertAddition(0, 0, 5); // case G
        changes = getChanges(
                "  ipsum  ",
                "   Lorem ipsum ");
        assertAddition(0, 3, 9); // case Ha
        changes = getChanges(
                "  \\ipsum  ",
                " Lorem \\ipsum");
        assertAddition(0, 1, 7); // case Hb
    }

    @Test
    public void additionsInMiddle() throws IOException {
        changes = getChanges(
                "Lorem  sit amet. ",
                "Lorem ipsum dolor sit amet. ");
        assertAddition(0, 6, 18);
        changes = getChanges(
                "Lorem ipsum dolor sit amet",
                "  Lorem ipsum dolor sit amet. ");
        assertAddition(0, 28, 30);
    }

    @Test
    public void additionsAtEnd() throws IOException {
        changes = getChanges(
                "Lorem",
                "Lorem ipsum ");
        assertAddition(0, 5, 12);
        changes = getChanges(
                "Lorem  ",
                "Lorem ipsum ");
        assertAddition(0, 6, 12);
        changes = getChanges(
                " Lorem  ",
                "Lorem ipsum ");
        assertAddition(0, 6, 12);
        changes = getChanges(
                " Lorem",
                "Lorem ipsum ");
        assertAddition(0, 5, 12);
        changes = getChanges(
                "Lorem  ",
                "Lorem\\ipsum");
        assertAddition(0, 5, 11);
        changes = getChanges(
                " Lorem  ",
                "Lorem\\ipsum ");
        assertAddition(0, 5, 11);
    }
}
