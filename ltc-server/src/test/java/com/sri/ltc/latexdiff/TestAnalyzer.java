/**
 ************************ 80 columns *******************************************
 * TestAccumulate
 *
 * Created on 12/21/11.
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
public final class TestAnalyzer {

    private static final LatexDiff latexDiff = new LatexDiff();
    List<Lexeme> lexemes;

    @Test
    public void analyzeSimple() throws IOException {
        // exercise simple analyses
        lexemes = latexDiff.analyze(new StringReaderWrapper("Lorem ipsum dolor sit amet. "), false);
        assertEquals(LexemeType.END_OF_FILE, lexemes.get(lexemes.size() - 1).type); // all analyses end with EOF
        assertTrue("Obtained 7 lexemes", lexemes.size() == 7);
        assertEquals(LexemeType.PUNCTUATION, lexemes.get(5).type);
        lexemes = latexDiff.analyze(new StringReaderWrapper("   \\textbf{Lorem} ipsum dolor sit amet. "), false);
        assertEquals(LexemeType.END_OF_FILE, lexemes.get(lexemes.size() - 1).type); // all analyses end with EOF
        assertTrue("Obtained 10 lexemes", lexemes.size() == 10);
        assertEquals(LexemeType.COMMAND, lexemes.get(0).type);
        lexemes = latexDiff.analyze(new StringReaderWrapper(
                "  \n \n \\textbf{Lorem} ipsum \n dolor sit amet. \n "
        ), false);
        assertEquals(LexemeType.END_OF_FILE, lexemes.get(lexemes.size() - 1).type); // all analyses end with EOF
        assertTrue("Obtained 11 lexemes", lexemes.size() == 11);
        assertEquals(LexemeType.PARAGRAPH, lexemes.get(0).type);
    }

    @Test
    public void analyzePreamble() throws IOException {
        // paragraphs in preamble
        lexemes = latexDiff.analyze(new StringReaderWrapper(
                " \n\n \\begin{document}  \n \nLorem ipsum \n dolor sit amet. \n "
        ), false);
        assertEquals(LexemeType.END_OF_FILE, lexemes.get(lexemes.size() - 1).type); // all analyses end with EOF
        assertTrue("Obtained 9 lexemes", lexemes.size() == 9);
        assertEquals(LexemeType.PREAMBLE, lexemes.get(0).type);
    }

    @Test
    public void analyzeComments() throws IOException {
        // starting with comments etc.
        lexemes = latexDiff.analyze(new StringReaderWrapper(
                " \n\n \\begin{document}  \n \nLorem ipsum %%%  HERE IS A COMMENT WITH SPACE AND MORE %...\n dolor sit amet. \n "
        ), false);
        assertEquals(LexemeType.END_OF_FILE, lexemes.get(lexemes.size() - 1).type); // all analyses end with EOF
        assertTrue("Obtained 24 lexemes", lexemes.size() == 24);
        assertEquals(LexemeType.COMMENT, lexemes.get(4).type);
        assertEquals(LexemeType.COMMENT, lexemes.get(18).type);
        lexemes = latexDiff.analyze(new StringReaderWrapper(
                "  HERE IS A COMMENT WITH SPACE AND MORE %...\n dolor sit amet. \n "
        ), true);
        assertEquals(LexemeType.END_OF_FILE, lexemes.get(lexemes.size() - 1).type); // all analyses end with EOF
        assertTrue("Obtained 17 lexemes", lexemes.size() == 17);
        assertEquals(LexemeType.COMMENT, lexemes.get(0).type);
        assertEquals(LexemeType.COMMENT, lexemes.get(11).type);
        assertEquals(LexemeType.WORD, lexemes.get(12).type);
    }
}
