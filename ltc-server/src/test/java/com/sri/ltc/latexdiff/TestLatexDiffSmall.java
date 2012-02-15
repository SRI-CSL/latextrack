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

import static org.junit.Assert.assertTrue;

/**
 * @author linda
 */
public final class TestLatexDiffSmall extends TestLatexDiff {

    @Test
    public void smallAdditions() throws IOException {
        changes = getChanges(
                "Lorem ipsum dolr sit amet. ",
                "Lorem ipsum dolor sit amet.");
        assertAddition(0, 15, 16, EnumSet.of(Change.Flag.SMALL));
        assertTrue("Change is small addition", changes.get(0) instanceof SmallAddition);
        changes = getChanges(
                "Lorem  dolr  amet. ",
                "Lorem ipsum dolor sit amet.");
        assertAddition(0, 5, 12, EnumSet.noneOf(Change.Flag.class));
        assertAddition(1, 15, 16, EnumSet.of(Change.Flag.SMALL));
        assertAddition(2, 17, 22, EnumSet.noneOf(Change.Flag.class));
        changes = getChanges(
                "Lorem  ipsum  dolr  amet. ",
                "Lorem ipsum dolor\\sit amet.");
        assertAddition(0, 15, 16, EnumSet.of(Change.Flag.SMALL));
        assertAddition(1, 17, 22, EnumSet.of(Change.Flag.COMMAND));
        changes = getChanges(
                "Lorem  ipsum  dolr  amet. ",
                "Lorem ipsum; dolor\\sit amet.");
        assertAddition(0, 11, 13, EnumSet.noneOf(Change.Flag.class));
        assertAddition(1, 16, 17, EnumSet.of(Change.Flag.SMALL));
        assertAddition(2, 18, 23, EnumSet.of(Change.Flag.COMMAND));
        changes = getChanges(
                "Lorem  ipsum  dolr  amet. ",
                "Lorem; dolor\\sit amet.");
        assertAddition(0, 5, 7, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(1, 5, 9, EnumSet.of(Change.Flag.DELETION));
        assertAddition(2, 10, 11, EnumSet.of(Change.Flag.SMALL));
        assertAddition(3, 12, 17, EnumSet.of(Change.Flag.COMMAND));
        changes = getChanges(
                "Lorem  ipsum  dolr\\sit  amet. ",
                "Lorem ipsum dolor amet.");
        assertAddition(0, 15, 16, EnumSet.of(Change.Flag.SMALL));
        assertDeletion(1, 17, 4, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND));
    }

    @Test
    public void smallDeletions() throws IOException {
        changes = getChanges(
                "Lorem ipsum doloer sit amet. ",
                "Lorem ipsum dolor sit amet.");
        assertDeletion(0, 16, 1, EnumSet.of(Change.Flag.DELETION, Change.Flag.SMALL));
        assertTrue("Change is small deletion", changes.get(0) instanceof SmallDeletion);
        changes = getChanges(
                "Lorem ipsum doloer sit amet. ",
                "Lorem   dolor  amet.");
        assertDeletion(0, 5, 6, EnumSet.of(Change.Flag.DELETION));
        assertDeletion(1, 12, 1, EnumSet.of(Change.Flag.DELETION, Change.Flag.SMALL));
        assertDeletion(2, 13, 4, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem\\ipsum doloer sit amet. ",
                "Lorem   dolor  amet.");
        assertDeletion(0, 5, 6, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND));
        assertDeletion(1, 12, 1, EnumSet.of(Change.Flag.DELETION, Change.Flag.SMALL));
        assertDeletion(2, 13, 4, EnumSet.of(Change.Flag.DELETION));
        changes = getChanges(
                "Lorem\\ipsum doloer sit amet. ",
                "Lorem;   dolor  amet.");
        assertAddition(0, 5, 9, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(1, 5, 7, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND));
        assertDeletion(2, 13, 1, EnumSet.of(Change.Flag.DELETION, Change.Flag.SMALL));
        assertDeletion(3, 14, 4, EnumSet.of(Change.Flag.DELETION));
    }

    @Test
    public void smallReplacements() throws IOException {
        changes = getChanges(
                "Lorem ipsum dloer sit amet. ",
                "Lorem ipsum dolor sit amet.");
        assertAddition(0, 13, 14, EnumSet.of(Change.Flag.SMALL));
        assertDeletion(1, 16, 1, EnumSet.of(Change.Flag.DELETION, Change.Flag.SMALL));
        changes = getChanges(
                "Lorem  ipsum dloer  \\fiddle amet. ",
                "Lorem\\ipsum dolor sit amet.");
        assertAddition(0, 5, 12, EnumSet.of(Change.Flag.COMMAND));
        assertDeletion(1, 5, 8, EnumSet.of(Change.Flag.DELETION));
        assertAddition(2, 13, 14, EnumSet.of(Change.Flag.SMALL));
        assertDeletion(3, 16, 1, EnumSet.of(Change.Flag.DELETION, Change.Flag.SMALL));
        assertAddition(4, 17, 22, EnumSet.noneOf(Change.Flag.class));
        assertDeletion(5, 17, 9, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND));
    }
}
