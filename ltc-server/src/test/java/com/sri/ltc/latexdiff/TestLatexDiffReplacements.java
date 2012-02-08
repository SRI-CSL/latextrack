/**
 ************************ 80 columns *******************************************
 * TestLatexDiffDeletions
 *
 * Created on 2/8/12.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * @author linda
 */
public final class TestLatexDiffReplacements extends TestLatexDiff {

    @Test
    public void replacements() throws IOException {
        changes = getChanges(  // case A
                "Lorem\\amet.",
                " Lorem\\ipsum dolor. ");
        assertAddition(0, 6, 18);
        assertDeletion(1, 6, 5);
        changes = getChanges(  // case B
                "Lorem   amet.",
                " Lorem\\ipsum dolor. ");
        assertAddition(0, 6, 18);
        assertDeletion(1, 6, 7);
        changes = getChanges(  // case C
                "Lorem\\amet .",
                " Lorem\\ipsum dolor. ");
        assertAddition(0, 6, 18);
        assertDeletion(1, 6, 6);
        changes = getChanges(  // case D
                "Lorem   amet .",
                " Lorem\\ipsum dolor. ");
        assertAddition(0, 6, 18);
        assertDeletion(1, 6, 8);
        changes = getChanges(  // case E
                "Lorem\\amet.",
                " Lorem ipsum dolor. ");
        assertAddition(0, 6, 18);
        assertDeletion(1, 6, 5);
        changes = getChanges(  // case F
                "Lorem   amet.",
                " Lorem ipsum dolor. ");
        assertAddition(0, 7, 18);
        assertDeletion(1, 7, 4);
        changes = getChanges(  // case G
                "Lorem\\amet  .",
                " Lorem ipsum dolor. ");
        assertAddition(0, 6, 18);
        assertDeletion(1, 6, 5);
        changes = getChanges(  // case H
                "Lorem   amet  .",
                " Lorem ipsum dolor. ");
        assertAddition(0, 7, 18);
        assertDeletion(1, 7, 6);
        changes = getChanges(  // case I
                "Lorem\\amet.",
                " Lorem\\ipsum dolor . ");
        assertAddition(0, 6, 19);
        assertDeletion(1, 6, 5);
        changes = getChanges(  // case J
                "Lorem   amet.",
                " Lorem\\ipsum dolor . ");
        assertAddition(0, 6, 18);
        assertDeletion(1, 6, 7);
        changes = getChanges(  // case K
                "Lorem\\amet  sit.",
                " Lorem\\ipsum dolor sit. ");
        assertAddition(0, 6, 19);
        assertDeletion(1, 6, 7);
        changes = getChanges(  // case L
                "Lorem   amet  sit.",
                " Lorem\\ipsum dolor sit. ");
        assertAddition(0, 6, 18);
        assertDeletion(1, 6, 9);
        changes = getChanges(  // case M
                "Lorem\\amet.",
                " Lorem ipsum dolor . ");
        assertAddition(0, 6, 19);
        assertDeletion(1, 6, 5);
        changes = getChanges(  // case N
                "Lorem   amet.",
                " Lorem ipsum dolor . ");
        assertAddition(0, 7, 19);
        assertDeletion(1, 7, 4);
        changes = getChanges(  // case O
                "Lorem\\amet  sit.",
                " Lorem ipsum dolor sit. ");
        assertAddition(0, 6, 19);
        assertDeletion(1, 6, 5);
        changes = getChanges(  // case P
                "Lorem   amet  sit.",
                " Lorem ipsum dolor sit. ");
        assertAddition(0, 7, 19);
        assertDeletion(1, 7, 6);
    }
}
