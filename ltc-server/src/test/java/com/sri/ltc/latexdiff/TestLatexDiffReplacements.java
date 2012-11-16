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
import org.junit.Ignore;
import org.junit.Test;

import java.util.EnumSet;

/**
 * @author linda
 */
@Ignore
public final class TestLatexDiffReplacements extends TestLatexDiff {

    @Test
    public void replacements() throws Exception {
        changes = getChanges(  // case Aa
                "Lorem\\amet.",
                " Lorem, dolor. ");
        assertAddition(0, 6, 13, Lists.newArrayList(new IndexFlagsPair<Integer>(
                13,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case Ab
                "\\amet.",
                "dolor. ");
        assertAddition(0, 0, 5, Lists.newArrayList(new IndexFlagsPair<Integer>(
                5,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 0, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet ",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case Ba
                "Lorem   amet.",
                " Lorem, dolor. ");
        assertAddition(0, 6, 13, Lists.newArrayList(new IndexFlagsPair<Integer>(
                13,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, 7, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(  // case Bb
                "   amet.",
                "dolor. ");
        assertAddition(0, 0, 5, Lists.newArrayList(new IndexFlagsPair<Integer>(
                5,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 0, 8, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet ",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(  // case C
                "Lorem\\amet .",
                " Lorem, dolor. ");
        assertAddition(0, 6, 13, Lists.newArrayList(new IndexFlagsPair<Integer>(
                13,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet ",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case D
                "Lorem   amet .",
                " Lorem, dolor. ");
        assertAddition(0, 6, 13, Lists.newArrayList(new IndexFlagsPair<Integer>(
                13,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, 8, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet ",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(  // case E
                "Lorem\\amet.",
                " Lorem ipsum dolor. ");
        assertAddition(0, 6, 18, Lists.newArrayList(new IndexFlagsPair<Integer>(
                18,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case F
                "Lorem   amet.",
                " Lorem ipsum dolor. ");
        assertAddition(0, 6, 18, Lists.newArrayList(new IndexFlagsPair<Integer>(
                18,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, 7, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(  // case G
                "Lorem\\amet  .",
                " Lorem ipsum dolor. ");
        assertAddition(0, 6, 18, Lists.newArrayList(new IndexFlagsPair<Integer>(
                18,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case H
                "Lorem   amet  .",
                " Lorem ipsum dolor. ");
        assertAddition(0, 6, 18, Lists.newArrayList(new IndexFlagsPair<Integer>(
                18,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, 7, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(  // case Ia
                "Lorem\\amet.",
                " Lorem, dolor . ");
        assertAddition(0, 6, 14, Lists.newArrayList(new IndexFlagsPair<Integer>(
                14,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case Ib
                "\\amet.",
                "dolor . ");
        assertAddition(0, 0, 6, Lists.newArrayList(new IndexFlagsPair<Integer>(
                6,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 0, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet ",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case Ja
                "Lorem   amet.",
                " Lorem, dolor . ");
        assertAddition(0, 6, 14, Lists.newArrayList(new IndexFlagsPair<Integer>(
                14,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, 7, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(  // case Jb
                "   amet.",
                "dolor . ");
        assertAddition(0, 0, 6, Lists.newArrayList(new IndexFlagsPair<Integer>(
                6,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 0, 8, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet ",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(  // case K
                "Lorem\\amet  sit.",
                " Lorem, dolor sit. ");
        assertAddition(0, 6, 14, Lists.newArrayList(new IndexFlagsPair<Integer>(
                14,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, 7, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet  ",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case L
                "Lorem   amet  sit.",
                " Lorem, dolor sit. ");
        assertAddition(0, 6, 14, Lists.newArrayList(new IndexFlagsPair<Integer>(
                14,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, 9, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet  ",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(  // case M
                "Lorem\\amet.",
                " Lorem ipsum dolor . ");
        assertAddition(0, 6, 19, Lists.newArrayList(new IndexFlagsPair<Integer>(
                19,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case N
                "Lorem   amet.",
                " Lorem ipsum dolor . ");
        assertAddition(0, 6, 19, Lists.newArrayList(new IndexFlagsPair<Integer>(
                19,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, 7, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(  // case O
                "Lorem\\amet  sit.",
                " Lorem ipsum dolor sit. ");
        assertAddition(0, 6, 19, Lists.newArrayList(new IndexFlagsPair<Integer>(
                19,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, 5, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\amet",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        changes = getChanges(  // case P
                "Lorem   amet  sit.",
                " Lorem ipsum dolor sit. ");
        assertAddition(0, 6, 19, Lists.newArrayList(new IndexFlagsPair<Integer>(
                19,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 6, 7, Lists.newArrayList(new IndexFlagsPair<String>(
                "   amet",
                EnumSet.of(Change.Flag.DELETION))));
    }
}
