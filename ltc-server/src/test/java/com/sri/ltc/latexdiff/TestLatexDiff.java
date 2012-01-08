/**
 ************************ 80 columns *******************************************
 * TestLatexDiff
 *
 * Created on 12/20/11.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author linda
 */
public final class TestLatexDiff {

    private static final LatexDiff latexDiff = new LatexDiff();
    List<Change> changes;

    private static List<Change> getChanges(String text1, String text2) throws IOException {
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

    @Test
    public void additions() throws IOException {
        changes = latexDiff.getChanges(
                new StringReaderWrapper("Lorem  sit amet. "),
                new StringReaderWrapper("Lorem ipsum dolor sit amet. "));
        assertTrue("Exactly one change", changes.size() == 1);
        assertTrue("Change is addition", changes.get(0) instanceof Addition);
        assertTrue("Start position is 6", changes.get(0).start_position == 6);
        assertTrue("Addition contains 3 lexemes", ((Addition) changes.get(0)).lexemes.size() == 3);
        changes = latexDiff.getChanges(
                new StringReaderWrapper("Lorem ipsum dolr sit amet. "),
                new StringReaderWrapper("Lorem ipsum dolor sit amet."));
        assertTrue("Change is small addition", changes.get(0) instanceof SmallAddition);
        assertTrue("Start position is 15", changes.get(0).start_position == 15);
        assertTrue("Small addition contains no lexemes", ((SmallAddition) changes.get(0)).lexemes.size() == 0);
        changes = latexDiff.getChanges(
                new StringReaderWrapper("Lorem ipsum dolor sit amet"),
                new StringReaderWrapper("  Lorem ipsum dolor sit amet. "));
        assertTrue("Exactly one change", changes.size() == 1);
        assertTrue("Change is addition", changes.get(0) instanceof Addition);
        assertTrue("Start position is 28", changes.get(0).start_position == 28);
        List<Lexeme> lexemes = ((Addition) changes.get(0)).lexemes;
        assertTrue("Addition contains 2 lexemes", lexemes.size() == 2);
        assertEquals(LexemeType.END_OF_FILE, lexemes.get(1).type);
    }

    @Test(expected = NullPointerException.class)
    public void nullReader() throws IOException {
        latexDiff.getChanges(
                new StringReaderWrapper(""),
                new StringReaderWrapper(null));
    }

    @Test
    public void whitespace() throws IOException {
        changes = latexDiff.getChanges(
                new StringReaderWrapper(""),
                new StringReaderWrapper(" \n   \t"));
        assertTrue("Changes is empty", changes.isEmpty());
        changes = latexDiff.getChanges(
                new StringReaderWrapper("   Lorem ipsum \n dolor sit amet. "),
                new StringReaderWrapper("Lorem ipsum dolor sit amet."));
        assertTrue("Changes is empty", changes.isEmpty());
        changes = latexDiff.getChanges(
                new StringReaderWrapper("   \n "),
                new StringReaderWrapper(" \t  "));
        assertTrue("Changes is empty", changes.isEmpty());
        changes = latexDiff.getChanges(
                new StringReaderWrapper("   Lorem ipsum \n \ndolor sit amet. "),
                new StringReaderWrapper("Lorem ipsum dolor sit amet."));
        assertTrue("Exactly one change", changes.size() == 1);
        assertTrue("Change is deletion", changes.get(0) instanceof Deletion);
        assertTrue("Start position is 12", changes.get(0).start_position == 12);
        changes = latexDiff.getChanges(
                new StringReaderWrapper("Lorem ipsum dolor sit amet."),
                new StringReaderWrapper("   Lorem ipsum \n \ndolor sit amet. "));
        assertTrue("Exactly one change", changes.size() == 1);
        assertTrue("Change is addition", changes.get(0) instanceof Addition);
        assertTrue("Start position is 15", changes.get(0).start_position == 15);
        List<Lexeme> lexemes = ((Addition) changes.get(0)).lexemes;
        assertTrue("Addition contains 2 lexemes", lexemes.size() == 2);
        assertEquals(LexemeType.PARAGRAPH, lexemes.get(0).type);
    }

    @Test
    public void changeInComment() throws IOException {
        changes = latexDiff.getChanges(new StringReaderWrapper(
                " \nLorem ipsum %%%  HERE IS A COMMMENT WITH SPACE...\n dolor sit amet. \n "
        ), new StringReaderWrapper(
                "Lorem ipsum \n%%%  HERE IS A COMMENT WITH SPACE AND MORE %...\n dolor sit amet."
        ));
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

    @Test
    public void changeInPreamble() throws IOException {
        changes = latexDiff.getChanges(new StringReaderWrapper(
                " \n\n \\begin{document}  \n \nLorem ipsum \n dolor sit amet. \n "
        ), new StringReaderWrapper(
                " \n\\usepackage{lipsum}\n \\begin{document}  \n \nLorem ipsum \n dolor sit amet. \n "
        ));
        assertTrue("2 changes", changes.size() == 2);
        Change change = changes.get(0);
        assertTrue("1st change is in preamble and a command",
                change.inPreamble && change.isCommand && !change.inComment);
        change = changes.get(1);
        assertTrue("2nd change is in preamble but not command nor a comment",
                change.inPreamble && !change.isCommand && !change.inComment);
    }

    @Test
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

    @Test
    public void replacements() throws IOException {
        changes = getChanges(
                "  Lorem ipsum dolor sit amet.\n",
                "Lorem ipsum \ndolor sit amet, \n "
        );
        assertTrue("2 changes", changes.size() == 2);
    }
}
