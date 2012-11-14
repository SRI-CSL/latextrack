/**
 ************************ 80 columns *******************************************
 * TestLatexDiff
 *
 * Created on 12/20/11.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author linda
 */
public class TestLatexDiff {

    private static final LatexDiff latexDiff = new LatexDiff();
    protected List<Change> changes;

    protected static List<Change> getChanges(String text1, String text2) throws Exception {
        return latexDiff.getChanges(
                new StringReaderWrapper(text1),
                new StringReaderWrapper(text2));
    }

    protected void assertAddition(int index, int start_position, int end_position, List<IndexFlagsPair<Integer>> flags) {
        assertTrue("at least "+(index+1)+" changes", changes.size() >= index+1);
        Change change = changes.get(index);
        assertTrue("change is addition", change instanceof Addition);
        assertTrue("start is at "+start_position, change.start_position == start_position);
        assertEquals("addition flags", flags, change.getFlags());
    }

    protected void assertDeletion(int index, int start_position, int length, List<IndexFlagsPair<String>> flags) {
        assertTrue("at least "+(index+1)+" changes", changes.size() >= index+1);
        Change change = changes.get(index);
        assertTrue("change is deletion", change instanceof Deletion);
        assertEquals("start position", start_position, change.start_position);
        assertEquals("deletion flags", flags, change.getFlags());
    }

    @Test(expected = NullPointerException.class)
    public void nullReader() throws Exception {
        changes = getChanges("", null);
    }

    @Test
    public void whitespace() throws Exception {
        changes = getChanges("", " \n   \t");
        assertTrue("Changes is empty", changes.isEmpty());
        changes = getChanges(
                "   Lorem ipsum \n dolor sit amet. ",
                "Lorem ipsum dolor sit amet.");
        assertTrue("Changes is empty", changes.isEmpty());
        changes = getChanges("   \n ", " \t  ");
        assertTrue("Changes is empty", changes.isEmpty());
        changes = getChanges(
                "   Lorem ipsum \n \ndolor sit amet. ",
                "Lorem ipsum dolor sit amet.");
        assertDeletion(0, 11, 4,
                Lists.newArrayList(new IndexFlagsPair<String>(
                        " \n \n",
                        EnumSet.of(Change.Flag.DELETION))));
        changes = getChanges(
                "Lorem ipsum dolor sit amet.",
                "   Lorem ipsum \n \ndolor sit amet. ");
        assertAddition(0, 14, 18,
                Lists.newArrayList(new IndexFlagsPair<Integer>(
                        18,
                        EnumSet.noneOf(Change.Flag.class))));
    }

    @Test
    public void inComment() throws Exception {
        changes = getChanges(
                " \nLorem ipsum %%%  HERE IS A COMMMENT WITH SPACE...\n dolor sit amet. \n ",
                "Lorem ipsum \n%%%  HERE IS A COMMENT WITH SPACE AND MORE %...\n dolor sit amet."
        );
        assertDeletion(0, 32, 1, Lists.newArrayList(new IndexFlagsPair<String>(
                "M",
                EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMENT, Change.Flag.SMALL))));
        assertAddition(1, 46, 57, Lists.newArrayList(new IndexFlagsPair<Integer>(
                57,
                EnumSet.of(Change.Flag.COMMENT))));
    }

    @Test
    public void inPreamble() throws Exception {
        changes = getChanges(
                " \n\n \\begin{document}  \n \nLorem ipsum \n dolor sit amet. \n ",
                " \n\\usepackage{lipsum}\n \\begin{document}  \n \nLorem ipsum \n dolor sit amet. \n "
        );
        assertAddition(0, 0, 13,
                Lists.newArrayList(
                        new IndexFlagsPair<Integer>(13, EnumSet.of(Change.Flag.COMMAND, Change.Flag.PREAMBLE)),
                        new IndexFlagsPair<Integer>(23, EnumSet.of(Change.Flag.PREAMBLE))));
        changes = getChanges(
                " \n\\usepackage{lipsum}\n \\begin{document}  \n \nLorem ipsum \n dolor sit amet. \n ",
                " \n\n \\begin{document}  \n \nLorem ipsum \n dolor sit amet. \n "
        );
        assertDeletion(0, 0, 13, Lists.newArrayList(
                new IndexFlagsPair<String>(" \n\\usepackage", EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND, Change.Flag.PREAMBLE)),
                new IndexFlagsPair<String>("{lipsum}", EnumSet.of(Change.Flag.DELETION, Change.Flag.PREAMBLE))));
        changes = getChanges(
                " \n\\usepackage{lipsum}\n \\begin{document}  \n \nLorem ipsum \n dolor sit amet. \n ",
                " \n\n % start doc\n\n\\begin{document}  \n \nLorem ipsum \n dolor sit amet. \n "
        );
        assertAddition(0, 0, 17,
                Lists.newArrayList(new IndexFlagsPair<Integer>(
                        17,
                        EnumSet.of(Change.Flag.PREAMBLE, Change.Flag.COMMENT))));
        assertDeletion(1, 0, 13, Lists.newArrayList(
                new IndexFlagsPair<String>(" \n\\usepackage", EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND, Change.Flag.PREAMBLE)),
                new IndexFlagsPair<String>("{lipsum}", EnumSet.of(Change.Flag.DELETION, Change.Flag.PREAMBLE))));
    }

    @Test
    public void commentThenText() throws Exception {
        changes = getChanges(
                "% Need life\n",
                "% Need \nLife"
        );


        assertAddition(0, 6, 4,
                Lists.newArrayList(new IndexFlagsPair<Integer>(
                        12,
                        EnumSet.noneOf(Change.Flag.class)))
        );

        assertDeletion(1, 6, 6,
                Lists.newArrayList(
                    new IndexFlagsPair<String>(" life", EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMENT))
                )
         );
    }

    @Test
    public void test3Diff() throws Exception {
        MarkedUpDocument document = new MarkedUpDocument();
        document.insertString(0, "  Lorem ipsum   dolor sit. ", null);
        document.markupAddition(7, 16, EnumSet.noneOf(Change.Flag.class));
        document.insertDeletion(25, "   amet", EnumSet.of(Change.Flag.DELETION));
        changes = latexDiff.getChanges(
                new StringReaderWrapper(" Lorem    amet,  consectetur."),
                new DocumentReaderWrapper(document));
        assertEquals("Number of changes", 2, changes.size());
    }
}
