package com.sri.ltc.latexdiff;

import org.junit.Test;

import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        assertEquals("Number of lexemes", size, lexemes.size());
    }

    private static List<Lexeme> analyze(ReaderWrapper wrapper) throws Exception {
        return latexDiff.analyze(wrapper);
    }

    @Test
    public void analyzeSimple() throws Exception {
        // exercise simple analyses
        lexemes = analyze(new StringReaderWrapper("Lorem ipsum dolor sit amet. "));
        assertLexemes(13);
        assertEquals(LexemeType.PUNCTUATION, lexemes.get(10).type);
        lexemes = analyze(new StringReaderWrapper("   \\textbf{Lorem} ipsum dolor sit amet. "));
        assertLexemes(17);
        assertEquals(LexemeType.COMMAND, lexemes.get(2).type);
        lexemes = analyze(new StringReaderWrapper(
                "  \n \n \\textbf{Lorem} ipsum \n dolor sit amet. \n "
        ));
        assertLexemes(20);
        assertEquals(LexemeType.PARAGRAPH, lexemes.get(1).type);
    }

    @Test
    public void analyzePreamble() throws Exception {
        // paragraphs in preamble
        lexemes = analyze(new StringReaderWrapper(
                " \n\n \\begin{document}  \n \nLorem ipsum \n dolor sit amet. \n "
        ));
        assertLexemes(21);
        // preamble ended with second lexeme, as paragraphs removed
        assertEquals(false, lexemes.get(1).preambleSeen);
        assertEquals(true, lexemes.get(2).preambleSeen);
        assertEquals(true, lexemes.get(lexemes.size()-1).preambleSeen);
        assertEquals("1st lexeme starts at", 4, lexemes.get(2).pos);
    }

    @Test
    public void analyzeComments() throws Exception {
        // starting with comments etc.
        lexemes = analyze(new StringReaderWrapper(
                " \\begin{document}  \n \nLorem ipsum %%%  HERE IS A COMMENT WITH SPACE AND MORE %...\n dolor sit amet. \n "
        ));
        assertLexemes(44);
        assertEquals(LexemeType.COMMENT_BEGIN, lexemes.get(11).type);
        assertEquals(LexemeType.SYMBOL, lexemes.get(29).type); // second % is not beginning a comment
        assertTrue("last lexeme in comment", lexemes.get(33).inComment);
        assertTrue("first lexeme after comment", !lexemes.get(34).inComment);
        lexemes = analyze(new StringReaderWrapper(
                "  %HERE IS A COMMENT WITH SPACE AND MORE %...\n dolor sit amet. \n "
        ));
        assertLexemes(34);
        assertEquals(LexemeType.COMMENT_BEGIN, lexemes.get(2).type);
        assertEquals(2, lexemes.get(2).pos);
        assertEquals(LexemeType.SYMBOL, lexemes.get(19).type);
        assertEquals(LexemeType.WORD, lexemes.get(25).type);
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
        assertLexemes(11);
        assertEquals("8th lexeme starts at", 25, lexemes.get(7).pos);
    }
}
