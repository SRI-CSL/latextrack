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
public final class TestLatexDiffSmall extends TestLatexDiff {

    @Test
    public void smallAdditions() throws Exception {
        changes = getChanges(
                "Lorem ipsum dolr sit amet. ",
                "Lorem ipsum dolor sit amet.");
        assertAddition(0, 15, 16, Lists.newArrayList(new IndexFlagsPair<Integer>(
                16,
                EnumSet.of(Change.Flag.SMALL))));
        changes = getChanges(
                "Lorem  dolr  amet. ",
                "Lorem ipsum dolor sit amet.");
        assertAddition(0, 5, 12, Lists.newArrayList(new IndexFlagsPair<Integer>(
                12,
                EnumSet.noneOf(Change.Flag.class))));
        assertAddition(1, 15, 16, Lists.newArrayList(new IndexFlagsPair<Integer>(
                16,
                EnumSet.of(Change.Flag.SMALL))));
        assertAddition(2, 17, 22, Lists.newArrayList(new IndexFlagsPair<Integer>(
                22,
                EnumSet.noneOf(Change.Flag.class))));
        changes = getChanges(
                "Lorem  ipsum  dolr  amet. ",
                "Lorem ipsum dolor\\sit amet.");
        assertAddition(0, 15, 16, Lists.newArrayList(new IndexFlagsPair<Integer>(
                16,
                EnumSet.of(Change.Flag.SMALL))));
        assertAddition(1, 17, 22, Lists.newArrayList(new IndexFlagsPair<Integer>(
                22,
                EnumSet.of(Change.Flag.COMMAND))));
        changes = getChanges(
                "Lorem  ipsum  dolr  amet. ",
                "Lorem ipsum; dolor\\sit amet.");
        assertAddition(0, 11, 13, Lists.newArrayList(new IndexFlagsPair<Integer>(
                13,
                EnumSet.noneOf(Change.Flag.class))));
        assertAddition(1, 16, 17, Lists.newArrayList(new IndexFlagsPair<Integer>(
                17,
                EnumSet.of(Change.Flag.SMALL))));
        assertAddition(2, 18, 23, Lists.newArrayList(new IndexFlagsPair<Integer>(
                23,
                EnumSet.of(Change.Flag.COMMAND))));
        changes = getChanges(
                "Lorem  ipsum  dolr  amet. ",
                "Lorem; dolor\\sit amet.");
        assertAddition(0, 5, 7, Lists.newArrayList(new IndexFlagsPair<Integer>(
                7,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 5, 9, Lists.newArrayList(new IndexFlagsPair<String>(
                "  ipsum  ",
                EnumSet.of(Change.Flag.DELETION))));
        assertAddition(2, 10, 11, Lists.newArrayList(new IndexFlagsPair<Integer>(
                11,
                EnumSet.of(Change.Flag.SMALL))));
        assertAddition(3, 12, 17, Lists.newArrayList(new IndexFlagsPair<Integer>(
                17,
                EnumSet.of(Change.Flag.COMMAND))));
        changes = getChanges(
                "Lorem  ipsum  dolr\\sit  amet. ",
                "Lorem ipsum dolor amet.");
        assertAddition(0, 15, 16, Lists.newArrayList(new IndexFlagsPair<Integer>(
                16,
                EnumSet.of(Change.Flag.SMALL))));
        assertDeletion(1, 17, 4, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\sit",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
    }

    @Test
    public void smallDeletions() throws Exception {
        changes = getChanges(
                "Lorem ipsum doloer sit amet. ",
                "Lorem ipsum dolor sit amet.");
        assertDeletion(0, 16, 1, Lists.newArrayList(new IndexFlagsPair<String>(
                "e",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.SMALL))));
        changes = getChanges(
                "Lorem ipsum doloer sit amet. ",
                "Lorem   dolor  amet.");
        assertDeletion(0, 5, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                " ipsum",
                EnumSet.of(Change.Flag.DELETION))));
        assertDeletion(1, 12, 1, Lists.newArrayList(new IndexFlagsPair<String>(
                "e",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.SMALL))));
        assertDeletion(2, 13, 4, Lists.newArrayList(new IndexFlagsPair<String>(
                " sit",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem\\ipsum doloer sit amet. ",
                "Lorem   dolor  amet.");
        assertDeletion(0, 5, 6, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\ipsum",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        assertDeletion(1, 12, 1, Lists.newArrayList(new IndexFlagsPair<String>(
                "e",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.SMALL))));
        assertDeletion(2, 13, 4, Lists.newArrayList(new IndexFlagsPair<String>(
                " sit",
                EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem\\ipsum doloer sit amet. ",
                "Lorem;   dolor  amet.");
        assertAddition(0, 5, 9, Lists.newArrayList(new IndexFlagsPair<Integer>(
                9,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(1, 5, 7, Lists.newArrayList(new IndexFlagsPair<String>(
                "\\ipsum ",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
        assertDeletion(2, 13, 1, Lists.newArrayList(new IndexFlagsPair<String>(
                "e",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.SMALL))));
        assertDeletion(3, 14, 4, Lists.newArrayList(new IndexFlagsPair<String>(
                " sit",
                EnumSet.of(Change.Flag.DELETION))));
    }

    @Test
    public void smallReplacements() throws Exception {
        changes = getChanges(
                "Lorem ipsum dloer sit amet. ",
                "Lorem ipsum dolor sit amet.");
        assertAddition(0, 13, 14, Lists.newArrayList(new IndexFlagsPair<Integer>(
                14,
                EnumSet.of(Change.Flag.SMALL))));
        assertDeletion(1, 16, 1, Lists.newArrayList(new IndexFlagsPair<String>(
                "e",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.SMALL))));
        changes = getChanges(
                "Lorem  ipsum dloer  \\fiddle amet. ",
                "Lorem\\ipsum dolor sit amet.");
        assertAddition(0, 5, 12, Lists.newArrayList(new IndexFlagsPair<Integer>(
                12,
                EnumSet.of(Change.Flag.COMMAND))));
        assertDeletion(1, 5, 8, Lists.newArrayList(new IndexFlagsPair<String>(
                "  ipsum ",
                EnumSet.of(Change.Flag.DELETION))));
        assertAddition(2, 13, 14, Lists.newArrayList(new IndexFlagsPair<Integer>(
                14,
                EnumSet.of(Change.Flag.SMALL))));
        assertDeletion(3, 16, 1, Lists.newArrayList(new IndexFlagsPair<String>(
                "e",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.SMALL))));
        assertAddition(4, 17, 22, Lists.newArrayList(new IndexFlagsPair<Integer>(
                22,
                EnumSet.noneOf(Change.Flag.class))));
        assertDeletion(5, 17, 9, Lists.newArrayList(new IndexFlagsPair<String>(
                "  \\fiddle",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND))));
    }
}
