/**
 ************************ 80 columns *******************************************
 * TestAccumulate
 *
 * Created on 12/21/11.
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
public final class TestAnalyzer {

    private static final LatexDiff latexDiff = new LatexDiff();
    List<Lexeme> lexemes;

    private void assertLexemes(int size) {
        assertTrue("At least 2 lexemes", lexemes.size() >= 2);
        assertEquals(LexemeType.START_OF_FILE, lexemes.get(0).type); // all analyses start with SOF
        assertEquals(LexemeType.END_OF_FILE, lexemes.get(lexemes.size() - 1).type); // all analyses end with EOF
        assertTrue("Obtained "+size+" lexemes", lexemes.size() == size);
    }

    @Test
    public void analyzeSimple() throws IOException {
        // exercise simple analyses
        lexemes = latexDiff.analyze(new StringReaderWrapper("Lorem ipsum dolor sit amet. "), false);
        assertLexemes(8);
        assertEquals(LexemeType.PUNCTUATION, lexemes.get(6).type);
        lexemes = latexDiff.analyze(new StringReaderWrapper("   \\textbf{Lorem} ipsum dolor sit amet. "), false);
        assertLexemes(11);
        assertEquals(LexemeType.COMMAND, lexemes.get(1).type);
        lexemes = latexDiff.analyze(new StringReaderWrapper(
                "  \n \n \\textbf{Lorem} ipsum \n dolor sit amet. \n "
        ), false);
        assertLexemes(12);
        assertEquals(LexemeType.PARAGRAPH, lexemes.get(1).type);
    }

    @Ignore // TODO: test once preamble done
    public void analyzePreamble() throws IOException {
        // paragraphs in preamble
        lexemes = latexDiff.analyze(new StringReaderWrapper(
                " \n\n \\begin{document}  \n \nLorem ipsum \n dolor sit amet. \n "
        ), false);
        assertLexemes(10);
        assertEquals(LexemeType.PREAMBLE, lexemes.get(1).type);
    }

    @Test
    public void analyzeComments() throws IOException {
        // starting with comments etc.
        lexemes = latexDiff.analyze(new StringReaderWrapper(
                " \\begin{document}  \n \nLorem ipsum %%%  HERE IS A COMMENT WITH SPACE AND MORE %...\n dolor sit amet. \n "
        ), false);
        assertLexemes(25);
        assertEquals(LexemeType.COMMENT, lexemes.get(5).type);
        assertEquals(LexemeType.COMMENT, lexemes.get(19).type);
        lexemes = latexDiff.analyze(new StringReaderWrapper(
                "  HERE IS A COMMENT WITH SPACE AND MORE %...\n dolor sit amet. \n "
        ), true);
        assertLexemes(18);
        assertEquals(LexemeType.COMMENT, lexemes.get(1).type);
        assertEquals(LexemeType.COMMENT, lexemes.get(12).type);
        assertEquals(LexemeType.WORD, lexemes.get(13).type);
    }
}
