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

import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author linda
 */
@Ignore
public final class TestAnalyzer {

    private static final LatexDiff latexDiff = new LatexDiff();
    List<Lexeme> lexemes;

    private void assertLexemes(int size) {
        assertTrue("At least 2 lexemes", lexemes.size() >= 2);
        assertEquals(LexemeType.START_OF_FILE, lexemes.get(0).type); // all analyses start with SOF
        assertEquals(LexemeType.END_OF_FILE, lexemes.get(lexemes.size() - 1).type); // all analyses end with EOF
        assertEquals("Number of lexemes", size, lexemes.size());
    }

    private static List<Lexeme> analyze(ReaderWrapper wrapper) throws Exception {
        return latexDiff.analyze(wrapper);
    }

    @Test
    public void analyzeSimple() throws Exception {
        // exercise simple analyses
        lexemes = analyze(new StringReaderWrapper("Lorem ipsum dolor sit amet. "));
        assertLexemes(8);
        assertEquals(LexemeType.PUNCTUATION, lexemes.get(6).type);
        lexemes = analyze(new StringReaderWrapper("   \\textbf{Lorem} ipsum dolor sit amet. "));
        assertLexemes(11);
        assertEquals(LexemeType.COMMAND, lexemes.get(1).type);
        lexemes = analyze(new StringReaderWrapper(
                "  \n \n \\textbf{Lorem} ipsum \n dolor sit amet. \n "
        ));
        assertLexemes(12);
        assertEquals(LexemeType.PARAGRAPH, lexemes.get(1).type);
    }

    @Test
    public void analyzePreamble() throws Exception {
        // paragraphs in preamble
        lexemes = analyze(new StringReaderWrapper(
                " \n\n \\begin{document}  \n \nLorem ipsum \n dolor sit amet. \n "
        ));
        assertLexemes(13);
        assertEquals(true, lexemes.get(1).preambleSeen); // preamble ended with first lexeme, as paragraphs removed
        assertEquals(true, lexemes.get(lexemes.size()-1).preambleSeen);
        assertEquals("1st lexeme starts at", 4, lexemes.get(1).pos);
    }

    @Test
    public void analyzeComments() throws Exception {
        // starting with comments etc.
        lexemes = analyze(new StringReaderWrapper(
                " \\begin{document}  \n \nLorem ipsum %%%  HERE IS A COMMENT WITH SPACE AND MORE %...\n dolor sit amet. \n "
        ));
        assertLexemes(26);
        assertEquals(LexemeType.COMMENT_BEGIN, lexemes.get(8).type);
        assertEquals(LexemeType.SYMBOL, lexemes.get(17).type); // second % is not beginning a comment
        lexemes = analyze(new StringReaderWrapper(
                "  %HERE IS A COMMENT WITH SPACE AND MORE %...\n dolor sit amet. \n "
        ));
        assertLexemes(19);
        assertEquals(LexemeType.COMMENT_BEGIN, lexemes.get(1).type);
        assertEquals(2, lexemes.get(1).pos);
        assertEquals(LexemeType.SYMBOL, lexemes.get(10).type);
        assertEquals(LexemeType.WORD, lexemes.get(14).type);
        lexemes = analyze(new StringReaderWrapper("\\cite{ABC}"));
        assertLexemes(6);
    }

    @Test
    public void analyzeDoc() throws Exception {
        MarkedUpDocument document = new MarkedUpDocument();
        document.insertString(0, "Lorem ipsum  dolor sit  amet. ", null);
        document.insertDeletion(7, "s", EnumSet.of(Change.Flag.SMALL, Change.Flag.DELETION));
        document.markupAddition(9, 10, EnumSet.of(Change.Flag.SMALL));
        document.markupAddition(17, 18, EnumSet.of(Change.Flag.SMALL));
        document.markupAddition(20, 25, EnumSet.noneOf(Change.Flag.class));
        lexemes = analyze(new DocumentReaderWrapper(document));
        assertLexemes(7);
        assertEquals("5th lexeme starts at", 25, lexemes.get(4).pos);
    }
}
