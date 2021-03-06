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

import com.sri.ltc.server.LTCserverInterface;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import java.awt.*;
import java.util.*;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author linda
 */
public final class TestAccumulate {

    private final static char[] HTML_STYLES = {'u', 's'};
    private final static Map<Integer,Color> colors = new HashMap<Integer, Color>();
    static {
        colors.put(1, Color.red);
        colors.put(2, Color.blue);
        colors.put(3, Color.green);
        colors.put(4, Color.pink);
    }

    private static Accumulate accumulate = new Accumulate();

    private static Map perform(int caretPosition, String... texts) throws Exception {
        return perform(caretPosition, null, EnumSet.noneOf(Change.Flag.class), texts);
    }

    private static Map perform(int caretPosition, Set<Integer> limitedAuthors, Set<Change.Flag> flagsToHide,
                               String... texts) throws Exception {
        ReaderWrapper[] readers = null;
        if (texts != null) {
            readers = new ReaderWrapper[texts.length];
            for (int i=0; i<texts.length; i++)
                readers[i] = new StringReaderWrapper(texts[i]);
        }
        return accumulate.perform(readers, null, flagsToHide, limitedAuthors, caretPosition);
    }

    @SuppressWarnings("unchecked")
    private void assertMap(String text, int styles, int caretPosition) {
        assertNotNull(map);
        assertTrue("Map has 3 entries", map.size() == 4);
        assertEquals("Text is equal to", text, new String(Base64.decodeBase64((byte[]) map.get(LTCserverInterface.KEY_TEXT))));
        assertEquals("Number of styles", styles, ((List<Integer[]>) map.get(LTCserverInterface.KEY_STYLES)).size());
        assertEquals("Caret position", caretPosition, map.get(LTCserverInterface.KEY_CARET));
    }

    @SuppressWarnings("unchecked")
    private void assertStyle(int[] types, int[][] positions, int[] authors, int[] revisions) {
        styles = (List<Integer[]>) map.get(LTCserverInterface.KEY_STYLES);
        for (int i = 0; i < types.length; i++)
            assertEquals("style ("+i+") type", (long) types[i], (long) styles.get(i)[2]);
        for (int i = 0; i < positions.length; i++) {
            assertEquals("style ("+i+") start position", (long) positions[i][0], (long) styles.get(i)[0]);
            assertEquals("style ("+i+") end position", (long) positions[i][1], (long) styles.get(i)[1]);
        }
        if (authors != null)
            for (int i = 0; i < authors.length; i++)
                assertEquals("style ("+i+") author", (long) authors[i], (long) styles.get(i)[3]);
        if (revisions != null)
            for (int i = 0; i < revisions.length; i++)
                assertEquals("style ("+i+") revision", (long) revisions[i], (long) styles.get(i)[4]);
    }

    Map map;
    List<Integer[]> styles;

    @Test(expected = NullPointerException.class)
    public void twoNullReaders() throws Exception {
        perform(0, (String) null, (String) null);
    }

    @Test
    public void oneOrNoVersions() throws Exception {
        map = perform(0, (String[]) null);
        assertMap("", 0, 0);
        map = perform(0, null, EnumSet.of(Change.Flag.PREAMBLE, Change.Flag.COMMENT, Change.Flag.DELETION), "");
        assertMap("", 0, 0);
        String text = "Hello World.";
        int position = 7;
        map = perform(position, text);
        assertMap(text, 0, position);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void twoVersions() throws Exception {
        // adding paragraph and more at end
        map = perform(22,
                "Lorem ipsum dolor sit    \t",
                "   Lorem ipsum \n \t\n  dolor sit amet. "
        );
        assertMap("   Lorem ipsum \n \t\n  dolor sit amet. ", 2, 22);
        assertStyle(
                new int[] {1, 1}, // all 2 markups are additions
                new int[][] {{14, 21}, {30, 37}},
                null, null);

        // removing paragraph in the middle and more at end
        map = perform(25,
                "   Lorem ipsum \n \ndolor sit amet. ",
                "Lorem ipsum dolor sit    \t"
        );
        assertMap("Lorem ipsum \n \n dolor sit amet.    \t", 2, 35);
        assertStyle(
                new int[] {2, 2}, // all 2 markups are deletions
                new int[][]{{11, 15}, {25, 31}},
                null, null);

        // removing things but hiding deletions
        map = perform(17, null, EnumSet.of(Change.Flag.DELETION),
                "   Lorem ipsum \n \ndolor sit amet. ",
                "Lorem ipsum dolor sit    \t"
        );
        assertMap("Lorem ipsum dolor sit    \t", 0, 17);

        // adding commands but hiding them
        map = perform(18, null, EnumSet.of(Change.Flag.COMMAND),
                "Lorem ipsum dolor sit    \t",
                "   Lorem ipsum \\textbf{dolor} sit amet. "
        );
        assertMap("   Lorem ipsum \\textbf{dolor} sit amet. ", 3, 18);
        assertStyle(
                new int[] {1, 1, 1}, // all 3 markups are additions
                new int[][]{{22, 23}, {28, 30}, {33, 40}},
                null, null);

        // deletion with lots of white space following:
        // think about this behavior: suggest to ignore end position of deletion???  NO.
        map = perform(30,
                "\t  Lorem ipsum dolor sit amet; \nconsectetur adipiscing elit.",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. \n "
        );
        assertMap("Lorem ipsum dolor sit amet; \n, consectetur adipiscing elit. \n ", 2, 33);
        assertStyle(
                new int[] {2, 1}, // one deletion and one addition
                new int[][]{{26, 29}, {29, 31}},
                null, null);

        // small changes:
        map = perform(12,
                "\t  Lorem ippsu dolor sit amet",
                "Lorem ipsum dolor sit amt \n "
        );
        assertMap("Lorem ippsum dolor sit amet \n ", 3, 13);
        assertStyle(
                new int[] {2, 1, 2}, // 1 addition and 2 deletions
                new int[][]{{8, 9}, {11, 12}, {25, 26}},
                null, null);
        // hide small changes...
        map = perform(0, null, EnumSet.of(Change.Flag.SMALL),
                "\t  Lorem ippsu dolor sit amet",
                "Lorem ipsum dolor sit amt \n "
        );
        assertMap("Lorem ipsum dolor sit amt \n ", 0, 0);
        // reproduce tutorial bug
        map = perform(3,
                "\n\nIf % or should this be ``When''?\nin the Course",
                "\n\nWhen in the Course");
        assertMap("\n\nIf % or should this be ``When''? in the Course", 2, 28);
        assertStyle(
                new int[] {2, 2},
                new int[][] {{2, 27}, {31, 34}},
                null, null);
    }

    @Test
    public void threeVersions() throws Exception {
        // small changes accumulate: positioning!
        map = perform(0,
                " Lorem isut",
                "Lorem   isum",
                "  Lorem  ipsum"
        );
        assertMap("  Lorem  ipsutm", 3, 0);
        assertStyle(
                new int[] {1, 2, 1},
                new int[][] {{10, 11}, {13, 14}, {14, 15}},
                new int[] {2, 1, 1},
                null);
        map = perform(0,
                " Lorem iut",
                "Lorem   isu",
                "  Lorem  ipsum"
        );
        assertMap("  Lorem  ipsumt", 4, 0);
        assertStyle(
                new int[] {1, 1, 1, 2},
                new int[][] {{10, 11}, {11, 12}, {13, 14}, {14, 15}},
                new int[] {2, 1, 2, 1},
                null);
        map = perform(0,
                "Lorem ipsum dorstamet,",
                " Lorem ipsum  dorstamt,",
                "Lorem  ipsum  dorsitamt,",
                "Lorem ipsum dolorsitamt,"
        );
        assertMap("Lorem ipsum dolorsitamet,", 3, 0);
        assertStyle(
                new int[] {1, 1, 2},
                new int[][] {{14, 16}, {18, 19}, {22, 23}},
                new int[] {3, 2, 1},
                null);
        // no change in latest version
        map = perform(0,
                "\t  Lorem ipsum; dolor sit amet.\n",
                "\t Lorem   ipsum dolor sit amet,  ",
                "Lorem ipsum dolor sit amet, \n "
        );
        assertMap("Lorem ipsum; dolor sit amet.\n, \n ", 3, 0);
        assertStyle(
                new int[] {2, 2, 1}, // 2 deletions and 1 addition
                new int[][] {{11, 12}, {27, 29}, {29, 33}},
                new int[] {1, 1}, null);
        // no change from first to second version
        map = perform(0,
                "\t Lorem   ipsum dolor sit amet,  ",
                " Lorem ipsum dolor sit amet, \n ",
                "Lorem ipsum; dolor sit amet.\n"
        );
        assertMap("Lorem ipsum; dolor sit amet, \n .\n", 3, 0);
        assertStyle(
                new int[] {1, 2, 1}, // 2 deletions and 1 addition
                new int[][] {{11, 13}, {27, 31}, {31, 33}},
                new int[] {2, 2, 2},
                null);
        // back and forth
        map = perform(0,
                "Lorem\t ipsum dolor",
                "Lorem dolor",
                "Lorem  ipsum"
        );
        assertMap("Lorem\t ipsum dolor  ipsum", 3, 0);
        assertStyle(
                new int[] {2, 2, 1},
                new int[][] {{5, 12}, {12, 18}, {18, 25}},
                new int[] {1, 2, 2},
                null);
        // back and forth but hiding deletions
        map = perform(0, null, EnumSet.of(Change.Flag.DELETION),
                "Lorem\t ipsum dolor",
                "Lorem dolor",
                "Lorem  ipsum"
        );
        assertMap("Lorem  ipsum", 1, 0);
        assertStyle(
                new int[] {1},
                new int[][] {{5, 12}},
                new int[] {2},
                null);
        // small addition and deletion with command
        map = perform(0,
                "Lorem ipm   \\dolor amet",
                "Lorem ipsum \\dolor",
                "Lorem  ipsum"
        );
        assertMap("Lorem  ipsum \\dolor amet", 3, 0);
        assertStyle(
                new int[] {1, 2, 2},
                new int[][] {{9, 11}, {12, 19}, {19, 24}},
                new int[] {1, 2, 1},
                null);
        // small addition and deletion with command; filtering COMMANDS and SMALL
        map = perform(0, null, EnumSet.of(Change.Flag.SMALL, Change.Flag.COMMAND),
                "Lorem ipm   \\dolor amet",
                "Lorem ipsum \\dolor",
                "Lorem  ipsum"
        );
        assertMap("Lorem  ipsum amet", 1, 0);
        assertStyle(
                new int[] {2},
                new int[][] {{12, 17}},
                new int[] {1},
                null);
        // replacement with trailing white space over 3 versions:
        map = perform(0,
                "\t  Lorem ipsum; dolor sit amet.\n",
                "Lorem ipsum dolor \nsit amet",
                "Lorem ipsum dolor sit amet, \n "
        );
        assertMap("Lorem ipsum; dolor sit amet., \n ", 3, 0);
        assertStyle(
                new int[] {2, 2, 1},
                new int[][] {{11, 12}, {27, 28}, {28, 32}},
                new int[] {1, 1, 2},
                null);
        // recreate problem with comment:
        // adding text and then adding comment, but showing everything
        map = perform(0, null, EnumSet.of(Change.Flag.COMMENT),
                "  Lorem ipsum dolor sit amet\n",
                "Lorem ipsum \ndolor sit amet, consectetur adipiscing elit. \n ",
                "Lorem ipsum \ndolor sit amet, consectetur adipiscing elit. \n " +
                        "% ADDING MORE:\n" +
                        "Praesent tempor hendrerit eros, non scelerisque est fermentum nec. "
        );
        assertMap("Lorem ipsum \ndolor sit amet, consectetur adipiscing elit. \n " +
                "% ADDING MORE:\n" +
                "Praesent tempor hendrerit eros, non scelerisque est fermentum nec. ", 3, 0);
        // TODO: once we fix the white-space in front of comment issue by tracking revision keys in character attributes,
        // we need to remove the 2nd tuple entry in all these!!
        assertStyle(
                new int[] {1, 1, 1},
                new int[][] {{27, 57}, {57, 60}, {75, 142}},
                new int[] {1, 2, 2},
                null);
//        renderHTML(map);
    }

    @Test
    public void fourVersions() throws Exception {
        // back and forth
        map = perform(0,
                "Lorem ipsum",
                "Lorem ipsum dolor",
                "Lorem dolor",
                "Lorem ipsum"
        );
        assertMap("Lorem ipsum dolor ipsum", 3, 0);
        assertStyle(
                new int[] {2, 2, 1},
                new int[][] {{5, 11}, {11, 17}, {17, 23}},
                new int[] {2, 3, 3},
                null);

        // delete in 2nd version, add back in 4th version, add in 3rd and 4th version,...
        map = perform(0,
                "  Lorem ipsum dolor sit amet\n \n adipiscing elit. ",
                "Lorem ipsum \n sit amet, consectetur rerum  adipiscing elit. ",
                "\tLorem ipsum  sit amet, consectetur  \t  rerum adipiscing elit, aliquam commodo.\n",
                "Lorem ipsum   dolor sit amet, consectetur adipiscing elit. Aliquam commodo. "
        );
//        renderHTML(map);
    }

    @Test
    public void filtering() throws Exception {
        map = perform(12,
                "old;deleted\ntext\n",
                "% old;added text\n");
        assertMap("% old;deleted\nadded text\n", 3, 20);
        assertStyle(
                new int[] {1, 2, 1},
                new int[][] {{0, 2}, {6, 14}, {14, 20}},
                null,
                null);
        map = perform(18, null, EnumSet.of(Change.Flag.DELETION),
                "deleted\nold\n",
                "% added old more \ntext\n");
        assertMap("% added old more \ntext\n", 2, 18);
        assertStyle(
                new int[] {1, 1},
                new int[][] {{0, 8}, {11, 23}},
                null,
                null);
        map = perform(10,
                "old;deleted\ntext\n",
                "% old;added text more and\nmore");
        assertMap("% old;deleted\nadded text more and\nmore", 4, 18);
        assertStyle(
                new int[] {1, 2, 1, 1},
                new int[][] {{0, 2}, {6, 14}, {14, 20}, {24, 38}},
                null,
                null);
        map = perform(12, null, EnumSet.of(Change.Flag.COMMENT),
                "old;deleted\ntext\n",
                "% old;added text more and\nmore");
        assertMap("% old;added text more and\nmore", 1, 12);
        assertStyle(
                new int[] {1},
                new int[][] {{26, 30}},
                null,
                null);
        map = perform(12, null, EnumSet.of(Change.Flag.DELETION),
                "old;deleted\ntext\n",
                "% old;added text more and\nmore");
        assertMap("% old;added text more and\nmore", 3, 12);
        assertStyle(
                new int[] {1, 1, 1},
                new int[][] {{0, 2}, {6, 12}, {16, 30}},
                null,
                null);
        // deletion in preamble, small deletion and addition of comment
        map = perform(0, null, EnumSet.of(Change.Flag.PREAMBLE, Change.Flag.COMMENT),
                "Lorem ipsumm dolor",
                "pre  \\begin{document}Lorem ipsumm dolor",
                "  \\begin{document}Lorem ipsumm dolor",
                "  \\begin{document}Lorem   ipsum dolor",
                "  \\begin{document}Lorem ipsum  dolor % amet."
        );
        assertMap("  \\begin{document}Lorem ipsumm  dolor % amet.", 3, 0);
        // TODO: fix code so that empty space is not being marked up!
        assertStyle(
                new int[] {1, 2, 1},  // once fixed, remove 3rd typle in this and next line!
                new int[][] {{0, 18}, {29, 30}, {37, 38}},
                new int[] {1, 3},
                null);
        // deletion in preamble, small deletion and addition of preamble
        map = perform(0, null, EnumSet.of(Change.Flag.PREAMBLE, Change.Flag.COMMAND, Change.Flag.DELETION),
                "pra   Lorem ipsumm dolor",
                "pre  Lorem ipsumm dolor",
                "pre  \\begin{document}Lorem ipsumm dolor",
                "pre  \\begin{document}Lorem   ipsum dolor",
                "pre  \\begin{document}Lorem ipsum  dolor \\amet."
        );
        assertMap("pre  \\begin{document}Lorem ipsum  dolor \\amet.", 2, 0);
        assertStyle(
                new int[] {1, 1},
                new int[][] {{11, 21}, {45, 46}},
                new int[] {2, 4},
                null);
        // preamble location
        map = perform(0, null, EnumSet.of(Change.Flag.PREAMBLE),
                " pre2 \\begin{document}Lorem ipsum dolor",
                " pre2 Lorem   ipsum dolor",
                "pre1 pre2 Lorem ipsum  dolor"
        );
        assertMap("pre1 pre2 \\begin{document} Lorem ipsum  dolor", 2, 0);
        assertStyle(
                new int[] {1, 2},
                new int[][] {{0, 5}, {9, 26}},
                new int[] {2, 1},
                null);
        // deletion in preamble, small deletion and addition of comment
        map = perform(0, null, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND),
                "pra   Lorem ipsumm dolor",
                "pre  Lorem ipsumm dolor",
                "pre  \\begin{document}Lorem ipsumm dolor",
                "pre  \\begin{document}Lorem   ipsum dolor",
                "pre  \\begin{document}Lorem ipsum  dolor \\amet."
        );
        assertMap("pre  \\begin{document}Lorem ipsum  dolor \\amet.", 3, 0);
        assertStyle(
                new int[] {1, 1, 1},
                new int[][] {{2, 3}, {11, 21}, {45, 46}},
                new int[] {1, 2, 4},
                null);
        // filtering comments
        map = perform(3, null, EnumSet.of(Change.Flag.COMMENT),
                "\n\nIf % or should this be ``When''?\nin the Course",
                "\n\nWhen in the Course"
        );
        assertMap("\n\nIf % or should this be ``When''? in the Course", 2, 28);
        assertStyle(
                new int[] {2, 2},
                new int[][] {{2, 27}, {31, 34}},
                null,
                null);
    }

    @Test
    public void testAttributionOfChange() throws Exception {
        map = perform(0, null, EnumSet.of(Change.Flag.COMMENT),
                "oldest",
                "pretty \n  \n% oldest comment",
                "% oldest comment with space");
        assertMap("pretty \n  \n% oldest comment with space", 1, 11);
        map = perform(0, null, EnumSet.of(Change.Flag.COMMENT),
                "oldest",
                "% oldest comment",
                " % oldest comment with space");
        assertMap(" % oldest comment with space", 1, 0);
        assertStyle(
                new int[]{1},
                new int[][]{{0, 1}},
                new int[]{1},
                new int[]{0});
        map = perform(0, null, EnumSet.of(Change.Flag.COMMENT),
                "oldest",
                "% oldest comment",
                "pretty % oldest comment with space");
        assertMap("pretty % oldest comment with space", 1, 0);
        assertStyle(
                new int[]{1},
                new int[][]{{0, 7}},
                new int[]{2},
                new int[]{1});
        map = perform(2, null, EnumSet.of(Change.Flag.COMMENT),
                "oldest",
                "pretty % oldest comment",
                " \n% oldest comment with space");
        assertMap("pretty \n% oldest comment with space", 2, 8);
        assertStyle(
                new int[]{2, 1},
                new int[][]{{0, 6}, {6, 8}},
                new int[]{2, 1},
                new int[]{1, 0});
        map = perform(0, null, EnumSet.of(Change.Flag.COMMENT),
                "oldest",
                "pretty \n  % oldest comment",
                "% oldest comment with space");
        assertMap("pretty \n  % oldest comment with space", 1, 10);
        assertStyle(
                new int[]{2},
                new int[][]{{0, 10}},
                new int[]{2},
                new int[]{1}
        );
    }

    @Test
    public void testPreambleBleeding() throws Exception {
        // in this case, the preamble additions/removals were throwing off a later lexeme
        // by making it appear to be part of the preamble. The original examples came from
        // the tutorial/independence.tex files, hence the naming of the variables.

        String version4Text = "\\documentclass{article}\n" +
                "\n" +
                "\\title{The Declaration of Independence}\n" +
                "\\author{John Adams\\\\\n" +
                "Benjamin   Franklin\\\\\n" +
                "Thomas Jefferson} \n" +
                "\\date{June 28, 1776} \n" +
                "\n" +
                "\\begin{document}\n" +
                "\\maketitle\n" +
                "\n" +
                "When in the Course of human events, it becomes necessary for one\n" +
                "people to dissolve the political bands which have connected them with\n" +
                "another, and to assume among the powers of the earth, the separate and\n" +
                "equal station to which the Laws of Nature and of Nature's God entitle\n" +
                "them, a decent respect to the opinions of mankind requires that they\n" +
                "should declare the causes which impel them to the separation.  We hold\n" +
                "these truths to be self-evident, that all men are created equal, that\n" +
                "they are endowed by their Creator with certain unalienable Rights, that\n" +
                "among these are Life, Liberty and the pursuit of Happiness.\n" +
                "\n" +
                "\n" +
                "\\end{document}";

        String version3Text =
                "\\documentclass{article}\n" +
                        "\n" +
                        "\\title{The Declaration of Independence}\n" +
                        "\\author{John Adams\\\\ \n" +
                        "Benjamin Franklin\\\\\n" +
                        "Thomas Jefferson} \n" +
                        "\\date{June 28, 1776} \n" +
                        "\n" +
                        "\\begin{document}\n" +
                        "\\maketitle\n" +
                        "\n" +
                        "When in the Course of human events, it becomes necessary for one people to dissolve the ppoliticall bands which have connected them with another, and to assume among the powers of the earth, the separate and equal station to which the Laws of Nature entitle them, a decent respect to the opinions of mankind requires that they should declare the causes which impel them to the separation. % More about ``$e = m\\cdot c^2$'':\n" +
                        "We hold these truths to be self-evident, that they are provided by their Creator with certain unalienable Rights, that among these are \n" +
                        "\n" +
                        "Life, Liberty and the pursuit of Happyness.\n" +
                        "\n" +
                        "\\end{document}";

        String version2Text =
                "\\documentclass{article}\n" +
                        "\n" +
                        "\\title{The Declaration of Independence}\n" +
                        "\\author{John Adams\\\\ \n" +
                        "Thomas Jefferson} \n" +
                        "\\date{June 28, 1776} \n" +
                        "\n" +
                        "\\begin{document}\n" +
                        "\\makteitle\n" +
                        "\n" +
                        "If % or should this be ``When''?\n" +
                        "in the Course of human events, it becomes imperative for one people too disolve ppoilticall ties that have connected them with another, and to assume among the powers of the earth, the separate and equal station to which the Laws of Nature entitle  them, a desent respect to the opinions of mankind requires that they should declare the causes which impel them to the\n" +
                        "%Need more about happiness and life.\n" +
                        "\n" +
                        "\\end{document}";

        map = perform(0, null, EnumSet.of(Change.Flag.SMALL, Change.Flag.COMMENT), new String[] { version2Text, version3Text, version4Text });
        int styleCountWithPreamble = ((List<Integer[]>) map.get(LTCserverInterface.KEY_STYLES)).size();

        map = perform(0, null, EnumSet.of(Change.Flag.SMALL, Change.Flag.COMMENT, Change.Flag.PREAMBLE), new String[] { version2Text, version3Text, version4Text });
        int styleCountWithoutPreamble = ((List<Integer[]>) map.get(LTCserverInterface.KEY_STYLES)).size();

        // If this is broken, more than just one change (the change in the preamble) will show up
        assertEquals("Style count without preamble", 1, styleCountWithPreamble - styleCountWithoutPreamble);
    }

    @Test
    public void textWithBackspace() throws Exception {
        // as submitted by Peter Karp on Aug 22
        String version1 = "This application requests continued support for\r\n" +
                "the EcoCyc project.  EcoCyc is a {\\em model-organism database} (MOD)\r\n" +
                "\\cite{MODWork98} for {\\em Escherichia coli} K--12.  \r\n" +
                "The project will be carried out by SRI International";
        String version2 = "This application requests continued support for\r\n" +
                "the EcoCyc project.  EcoCyc is a {\\em model-organism database} (MOD)\r\n" +
                "\\cite{MODWork98} for {\\em Escherichia coli} K--12.  In addition, we propose to extend the scope of the project to cover the Gram-positive model organism Bacillus subtilis.\r\n" +
                "The project will be carried out by SRI International";
        String version3 = "This application requests continued support for\r\n" +
                "the EcoCyc project.  EcoCyc is a {\\em model-organism database} (MOD)\r\n" +
                "\\cite{MODWork98} for {\\em Escherichia coli} K--12.  In addition, we propose to extend the scope of the project to cover the Gram-positive model organism \\bacsub.\r\n" +
                "The project will be carried out by SRI International";

        map = perform(0, version1, version2, version3);
        assertMap("This application requests continued support for\r\n" +
                "the EcoCyc project.  EcoCyc is a {\\em model-organism database} (MOD)\r\n" +
                "\\cite{MODWork98} for {\\em Escherichia coli} K--12.  In addition, we propose to extend the scope of the project to cover the Gram-positive model organism Bacillus subtilis \\bacsub.\r\n" +
                "The project will be carried out by SRI International", 4, 0);
    }
}
