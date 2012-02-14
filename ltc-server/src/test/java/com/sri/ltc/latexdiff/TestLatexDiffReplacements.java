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
public final class TestLatexDiffReplacements extends TestLatexDiff {

    @Test
    public void replacements() throws IOException {
        changes = getChanges(  // case A
                "Lorem\\amet.",
                " Lorem, dolor. ");
        assertAddition(0, 6, 13, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(1, 6, 5, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND));
        changes = getChanges(  // case B
                "Lorem   amet.",
                " Lorem, dolor. ");
        assertAddition(0, 6, 13, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(1, 6, 7, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(  // case C
                "Lorem\\amet .",
                " Lorem, dolor. ");
        assertAddition(0, 6, 13, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(1, 6, 6, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND));
        changes = getChanges(  // case D
                "Lorem   amet .",
                " Lorem, dolor. ");
        assertAddition(0, 6, 13, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(1, 6, 8, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(  // case E
                "Lorem\\amet.",
                " Lorem ipsum dolor. ");
        assertAddition(0, 6, 18, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(1, 6, 5, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND));
        changes = getChanges(  // case F
                "Lorem   amet.",
                " Lorem ipsum dolor. ");
        assertAddition(0, 6, 18, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(1, 6, 7, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(  // case G
                "Lorem\\amet  .",
                " Lorem ipsum dolor. ");
        assertAddition(0, 6, 18, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(1, 6, 5, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND));
        changes = getChanges(  // case H
                "Lorem   amet  .",
                " Lorem ipsum dolor. ");
        assertAddition(0, 6, 18, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(1, 6, 7, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(  // case I
                "Lorem\\amet.",
                " Lorem, dolor . ");
        assertAddition(0, 6, 14, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(1, 6, 5, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND));
        changes = getChanges(  // case J
                "Lorem   amet.",
                " Lorem, dolor . ");
        assertAddition(0, 6, 14, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(1, 6, 7, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(  // case K
                "Lorem\\amet  sit.",
                " Lorem, dolor sit. ");
        assertAddition(0, 6, 14, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(1, 6, 7, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND));
        changes = getChanges(  // case L
                "Lorem   amet  sit.",
                " Lorem, dolor sit. ");
        assertAddition(0, 6, 14, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(1, 6, 9, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(  // case M
                "Lorem\\amet.",
                " Lorem ipsum dolor . ");
        assertAddition(0, 6, 19, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(1, 6, 5, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND));
        changes = getChanges(  // case N
                "Lorem   amet.",
                " Lorem ipsum dolor . ");
        assertAddition(0, 6, 19, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(1, 6, 7, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(  // case O
                "Lorem\\amet  sit.",
                " Lorem ipsum dolor sit. ");
        assertAddition(0, 6, 19, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(1, 6, 5, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND));
        changes = getChanges(  // case P
                "Lorem   amet  sit.",
                " Lorem ipsum dolor sit. ");
        assertAddition(0, 6, 19, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(1, 6, 7, EnumSet.of(Change.Flag.DELETION));
    }
}
