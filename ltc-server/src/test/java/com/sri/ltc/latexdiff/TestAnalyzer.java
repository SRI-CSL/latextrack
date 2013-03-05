/*
 * #%L
 * LaTeX Track Changes (LTC) allows collaborators on a version-controlled LaTeX writing project to view and query changes in the .tex documents.
 * %%
 * Copyright (C) 2009 - 2012 SRI International
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
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
        // preamble ended with second lexeme, as paragraphs removed
        assertEquals(false, lexemes.get(0).preambleSeen);
        assertEquals(true, lexemes.get(1).preambleSeen);
        assertEquals(true, lexemes.get(lexemes.size()-1).preambleSeen);
        assertEquals("2nd lexeme starts at", 4, lexemes.get(1).pos);
    }

    @Test
    public void analyzeComments() throws Exception {
        // kind of moot, as we are not detecting comments in analyze phase!
        lexemes = analyze(new StringReaderWrapper(
                " \\begin{document}  \n \nLorem ipsum %%%  HERE IS A COMMENT WITH SPACE AND MORE %...\n dolor sit amet. \n "
        ));
        assertLexemes(28);
        assertEquals(LexemeType.SYMBOL, lexemes.get(8).type);
        lexemes = analyze(new StringReaderWrapper(
                "  %HERE IS A COMMENT WITH SPACE AND MORE %...\n dolor sit amet. \n "
        ));
        assertLexemes(19);
        assertEquals(2, lexemes.get(1).pos);
        assertEquals(LexemeType.SYMBOL, lexemes.get(10).type);
        assertEquals(LexemeType.WORD, lexemes.get(16).type);
        lexemes = analyze(new StringReaderWrapper("\\cite{ABC}"));
        assertLexemes(6);
    }

    @Test
    public void analyzeWhiteSpace() throws Exception {
        lexemes = analyze(new StringReaderWrapper("howdy \n  dowdy \n \r\n \n  man"));
        assertLexemes(6);
        assertEquals("4th lexeme is paragraph", LexemeType.PARAGRAPH, lexemes.get(3).type);
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
