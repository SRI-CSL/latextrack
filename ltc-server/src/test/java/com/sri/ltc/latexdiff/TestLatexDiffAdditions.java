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
public final class TestLatexDiffAdditions extends TestLatexDiff {

    @Test
    public void additionsAtBeginning() throws IOException {
        changes = getChanges(
                "\\ipsum  ",
                "Lorem\\ipsum");
        assertAddition(0, 0, 5, EnumSet.noneOf(Change.Flag.class)); // case A
        changes = getChanges(
                "\\ipsum  ",
                " Lorem\\ipsum");
        assertAddition(0, 0, 6, EnumSet.noneOf(Change.Flag.class)); // case B
        changes = getChanges(
                "ipsum",
                "Lorem ipsum ");
        assertAddition(0, 0, 6, EnumSet.noneOf(Change.Flag.class)); // case Ca
        changes = getChanges(
                "ipsum  ",
                "Lorem ipsum ");
        assertAddition(0, 0, 6, EnumSet.noneOf(Change.Flag.class)); // case Cb
        changes = getChanges(
                "ipsum",
                "   Lorem ipsum ");
        assertAddition(0, 0, 9, EnumSet.noneOf(Change.Flag.class)); // case D
        changes = getChanges(
                "  \\ipsum  ",
                "Lorem\\ipsum");
        assertAddition(0, 0, 5, EnumSet.noneOf(Change.Flag.class)); // case E
        changes = getChanges(
                "  \\ipsum  ",
                " Lorem\\ipsum");
        assertAddition(0, 1, 6, EnumSet.noneOf(Change.Flag.class)); // case F
        changes = getChanges(
                "  ipsum  ",
                "Lorem ipsum ");
        assertAddition(0, 0, 5, EnumSet.noneOf(Change.Flag.class)); // case G
        changes = getChanges(
                "  ipsum  ",
                "   Lorem ipsum ");
        assertAddition(0, 3, 9, EnumSet.noneOf(Change.Flag.class)); // case Ha
        changes = getChanges(
                "  \\ipsum  ",
                " Lorem \\ipsum");
        assertAddition(0, 1, 7, EnumSet.noneOf(Change.Flag.class)); // case Hb
    }

    @Test
    public void additionsInMiddle() throws IOException {
        changes = getChanges(
                "Lorem  sit amet. ",
                "Lorem ipsum dolor sit amet. ");
        assertAddition(0, 6, 18, EnumSet.noneOf(Change.Flag.class));
        changes = getChanges(
                "Lorem ipsum dolor sit amet",
                "  Lorem ipsum dolor sit amet. ");
        assertAddition(0, 28, 30, EnumSet.noneOf(Change.Flag.class));
    }

    @Test
    public void additionsAtEnd() throws IOException {
        changes = getChanges(
                "Lorem",
                "Lorem ipsum ");
        assertAddition(0, 5, 12, EnumSet.noneOf(Change.Flag.class));
        changes = getChanges(
                "Lorem  ",
                "Lorem ipsum ");
        assertAddition(0, 6, 12, EnumSet.noneOf(Change.Flag.class));
        changes = getChanges(
                " Lorem  ",
                "Lorem ipsum ");
        assertAddition(0, 6, 12, EnumSet.noneOf(Change.Flag.class));
        changes = getChanges(
                " Lorem",
                "Lorem ipsum ");
        assertAddition(0, 5, 12, EnumSet.noneOf(Change.Flag.class));
        changes = getChanges(
                "Lorem  ",
                "Lorem\\ipsum");
        assertAddition(0, 5, 11, EnumSet.noneOf(Change.Flag.class));
        changes = getChanges(
                " Lorem  ",
                "Lorem\\ipsum ");
        assertAddition(0, 5, 11, EnumSet.noneOf(Change.Flag.class));
    }
}
