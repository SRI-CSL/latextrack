/**
 ************************ 80 columns *******************************************
 * TestLatexDiff
 *
 * Created on 12/20/11.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author linda
 */
public class TestLatexDiff {

    private static final LatexDiff latexDiff = new LatexDiff();
    protected List<Change> changes;

    protected static List<Change> getChanges(String text1, String text2) throws IOException {
        return latexDiff.getChanges(
                new StringReaderWrapper(text1),
                new StringReaderWrapper(text2));
    }

    private void renderXML() {
        System.out.println();
        for (Change c : changes)
            System.out.println(c);
        System.out.println();
    }

    protected void assertAddition(int index, int start_position, int end_position) {
        assertTrue("at least "+(index+1)+" changes", changes.size() >= index+1);
        Change change = changes.get(index);
        assertTrue("change is addition", change instanceof Addition);
        assertTrue("start is at "+start_position, change.start_position == start_position);
        assertTrue("end is at " + end_position, ((Addition) change).end_position == end_position);
    }

    protected void assertDeletion(int index, int start_position, int length) {
        assertTrue("at least "+(index+1)+" changes", changes.size() >= index+1);
        Change change = changes.get(index);
        assertTrue("change is deletion", change instanceof Deletion);
        assertTrue("start is at "+start_position, change.start_position == start_position);
        assertTrue("length is "+length, ((Deletion) change).text.length() == length);
    }

    @Ignore
    public void smallAdditions() throws IOException {
        changes = getChanges(
                "Lorem ipsum dolr sit amet. ",
                "Lorem ipsum dolor sit amet.");
        assertTrue("Change is small addition", changes.get(0) instanceof SmallAddition);
        assertTrue("Start position is 15", changes.get(0).start_position == 15);
        assertTrue("Small addition contains no lexemes", ((SmallAddition) changes.get(0)).lexemes.size() == 0);
    }

    @Test(expected = NullPointerException.class)
    public void nullReader() throws IOException {
        changes = getChanges("", null);
    }

    @Test
    public void whitespace() throws IOException {
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
        assertDeletion(0, 12, 3);
        changes = getChanges(
                "Lorem ipsum dolor sit amet.",
                "   Lorem ipsum \n \ndolor sit amet. ");
        assertAddition(0, 15, 18);
    }

    @Ignore
    public void inComment() throws IOException {
        changes = getChanges(
                " \nLorem ipsum %%%  HERE IS A COMMMENT WITH SPACE...\n dolor sit amet. \n ",
                "Lorem ipsum \n%%%  HERE IS A COMMENT WITH SPACE AND MORE %...\n dolor sit amet."
        );
        assertTrue("2 changes", changes.size() == 2);
        Change change = changes.get(0);
        assertTrue("1st change is small deletion", change instanceof SmallDeletion);
        assertTrue("1st change is in comment but not in preamble nor a command",
                !change.inPreamble && !change.isCommand && change.inComment);
        change = changes.get(1);
        assertTrue("2nd change is addition", change instanceof Addition);
        assertTrue("2nd change has 4 lexemes", ((Addition) change).lexemes.size() == 4);
        assertTrue("2nd change is in comment but not in preamble nor a command",
                !change.inPreamble && !change.isCommand && change.inComment);
    }

    @Ignore
    public void inPreamble() throws IOException {
        changes = getChanges(
                " \n\n \\begin{document}  \n \nLorem ipsum \n dolor sit amet. \n ",
                " \n\\usepackage{lipsum}\n \\begin{document}  \n \nLorem ipsum \n dolor sit amet. \n "
        );
        assertTrue("2 changes", changes.size() == 2);
        Change change = changes.get(0);
        assertTrue("1st change is in preamble and a command",
                change.inPreamble && change.isCommand && !change.inComment);
        change = changes.get(1);
        assertTrue("2nd change is in preamble but not command nor a comment",
                change.inPreamble && !change.isCommand && !change.inComment);
        // TODO: Test cases before PREAMBLE, such as ignoring changes there or PARAGRAPHs
    }

    @Ignore
    public void additionsWithMixedTypes() throws IOException {
        changes = getChanges(
                "Lorem \n dlor sit amet.",
                "Lorem ipsum \n%%%  HERE IS A COMMENT WITH SPACE AND MORE %...\n\n\\textbf{dolor} sit amet."
        );
        assertTrue("7 changes", changes.size() == 7);
        // start position of next change equals last lexeme of addition for changes 1-4:
        for (int i=0; i<4; i++) {
            Change change = changes.get(i);
            Change nextChange = changes.get(i+1);
            assertTrue(change instanceof Addition);
            List<Lexeme> lexemes = ((Addition) change).lexemes;
            assertTrue(lexemes.get(lexemes.size()-1).pos == nextChange.start_position);
        }
        renderXML();
        changes = getChanges(
                "Lorem \n dolor   amet.",
                "Lorem ipsum \n%%%  HERE IS A COMMENT\n dlor sit amet.");
        assertTrue("4 changes", changes.size() == 4);
        renderXML();
    }
}
