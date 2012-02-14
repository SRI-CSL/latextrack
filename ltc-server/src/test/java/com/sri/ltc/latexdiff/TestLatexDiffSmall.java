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
        assertTrue("Change is small addition", changes.get(0) instanceof SmallAddition);
    }

}
