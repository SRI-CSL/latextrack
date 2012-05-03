/**
 ************************ 80 columns *******************************************
 * TestAccumulate
 *
 * Created on 12/21/11.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import com.sri.ltc.server.LTCserverInterface;
import org.junit.Test;

import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.IOException;
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

    private static Accumulate accumulate;
    static {
        try {
            accumulate = new Accumulate("");
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private static Map perform(int caretPosition, String... texts) throws IOException, BadLocationException {
        return perform(caretPosition, EnumSet.noneOf(Change.Flag.class), texts);
    }

    private static Map perform(int caretPosition, Set<Change.Flag> flagsToHide, String... texts) throws IOException, BadLocationException {
        ReaderWrapper[] readers = null;
        if (texts != null) {
            readers = new ReaderWrapper[texts.length];
            for (int i=0; i<texts.length; i++)
                readers[i] = new StringReaderWrapper(texts[i]);
        }
        return accumulate.perform(readers, null, flagsToHide, caretPosition);
    }

    @SuppressWarnings("unchecked")
    private void assertMap(String text, int styles, int caretPosition) {
        assertNotNull(map);
        assertTrue("Map has 3 entries", map.size() == 3);
        assertEquals("Text is equal to", text, map.get(LTCserverInterface.KEY_TEXT));
        assertEquals("Number of styles", styles, ((List<Integer[]>) map.get(LTCserverInterface.KEY_STYLES)).size());
        assertEquals("Caret position", caretPosition, map.get(LTCserverInterface.KEY_CARET));
    }

    @SuppressWarnings("unchecked")
    private void assertStyle(int[] types, int[][] positions, int[] authors) {
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
    }

    Map map;
    List<Integer[]> styles;

    @Test(expected = NullPointerException.class)
    public void twoNullReaders() throws IOException, BadLocationException {
        perform(0, (String) null, (String) null);
    }

    @Test
    public void oneOrNoVersions() throws IOException, BadLocationException {
        map = perform(0, (String[]) null);
        assertMap("", 0, 0);
        map = perform(0, EnumSet.of(Change.Flag.PREAMBLE, Change.Flag.COMMENT, Change.Flag.DELETION), "");
        assertMap("", 0, 0);
        String text = "Hello World.";
        int position = 7;
        map = perform(position, text);
        assertMap(text, 0, position);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void twoVersions() throws IOException, BadLocationException {
        // adding paragraph and more at end
        map = perform(22,
                "Lorem ipsum dolor sit    \t",
                "   Lorem ipsum \n \t\n  dolor sit amet. "
        );
        assertMap("   Lorem ipsum \n \t\n  dolor sit amet. ", 2, 22);
        assertStyle(
                new int[] {1, 1}, // all 2 markups are additions
                new int[][] {{14, 21}, {30, 37}},
                null);

        // removing paragraph in the middle and more at end
        map = perform(25,
                "   Lorem ipsum \n \ndolor sit amet. ",
                "Lorem ipsum dolor sit    \t"
        );
        assertMap("Lorem ipsum \n \n dolor sit amet.    \t", 2, 35);
        assertStyle(
                new int[] {2, 2}, // all 2 markups are deletions
                new int[][]{{11, 15}, {25, 31}},
                null);

        // removing things but hiding deletions
        map = perform(17, EnumSet.of(Change.Flag.DELETION),
                "   Lorem ipsum \n \ndolor sit amet. ",
                "Lorem ipsum dolor sit    \t"
        );
        assertMap("Lorem ipsum dolor sit    \t", 0, 17);

        // adding commands but hiding them
        map = perform(18, EnumSet.of(Change.Flag.COMMAND),
                "Lorem ipsum dolor sit    \t",
                "   Lorem ipsum \\textbf{dolor} sit amet. "
        );
        assertMap("   Lorem ipsum \\textbf{dolor} sit amet. ", 3, 18);
        assertStyle(
                new int[] {1, 1, 1}, // all 3 markups are additions
                new int[][]{{22, 23}, {28, 30}, {33, 40}},
                null);

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
                null);

        // small changes:
        map = perform(12,
                "\t  Lorem ippsu dolor sit amet",
                "Lorem ipsum dolor sit amt \n "
        );
        assertMap("Lorem ippsum dolor sit amet \n ", 3, 13);
        assertStyle(
                new int[] {2, 1, 2}, // 1 addition and 2 deletions
                new int[][]{{8, 9}, {11, 12}, {25, 26}},
                null);
        // hide small changes...
        map = perform(0, EnumSet.of(Change.Flag.SMALL),
                "\t  Lorem ippsu dolor sit amet",
                "Lorem ipsum dolor sit amt \n "
        );
        assertMap("Lorem ipsum dolor sit amt \n ", 0, 0);

        // reproduce tutorial bug
        map = perform(3,
                "\n\nIf % or should this be ``When''?\nin the Course",
                "\n\nWhen in the Course");
        assertMap("\n\nIf % or should this be ``When''?\nWhen in the Course", 2, 36);
        assertStyle(
                new int[] {2, 1},
                new int[][] {{2, 35}, {35, 40}},
                null);
    }

    // TODO: exercise various filters with small and large changes (2 and 3 versions)

    @Test
    public void threeVersions() throws IOException, BadLocationException {
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
                new int[] {2, 1, 1}
        );
        map = perform(0,
                " Lorem iut",
                "Lorem   isu",
                "  Lorem  ipsum"
        );
        assertMap("  Lorem  ipsumt", 4, 0);
        assertStyle(
                new int[] {1, 1, 1, 2},
                new int[][] {{10, 11}, {11, 12}, {13, 14}, {14, 15}},
                new int[] {2, 1, 2, 1}
        );
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
                new int[] {3, 2, 1}
        );
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
                new int[] {1, 1});
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
                new int[] {2, 2, 2}
        );
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
                new int[] {1, 2, 2}
        );
        // back and forth but hiding deletions
        map = perform(0, EnumSet.of(Change.Flag.DELETION),
                "Lorem\t ipsum dolor",
                "Lorem dolor",
                "Lorem  ipsum"
        );
        assertMap("Lorem  ipsum", 1, 0);
        assertStyle(
                new int[] {1},
                new int[][] {{5, 12}},
                new int[] {2}
        );
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
                new int[] {1, 2, 1}
        );
        // small addition and deletion with command; filtering COMMANDS and SMALL
        map = perform(0, EnumSet.of(Change.Flag.SMALL, Change.Flag.COMMAND),
                "Lorem ipm   \\dolor amet",
                "Lorem ipsum \\dolor",
                "Lorem  ipsum"
        );
        assertMap("Lorem  ipsum amet", 1, 0);
        assertStyle(
                new int[] {2},
                new int[][] {{12, 17}},
                new int[] {1}
        );
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
                new int[] {1, 1, 2}
        );
        // TODO: recreate problem with comment:
        // adding text and then adding comment, but showing everything
        map = perform(0, EnumSet.of(Change.Flag.COMMENT),
                "  Lorem ipsum dolor sit amet\n",
                "Lorem ipsum \ndolor sit amet, consectetur adipiscing elit. \n ",
                "Lorem ipsum \ndolor sit amet, consectetur adipiscing elit. \n " +
                        "% ADDING MORE:\n" +
                        "Praesent tempor hendrerit eros, non scelerisque est fermentum nec. "
        );
        assertMap("Lorem ipsum \ndolor sit amet, consectetur adipiscing elit. \n " +
                "% ADDING MORE:\n" +
                "Praesent tempor hendrerit eros, non scelerisque est fermentum nec. ", 2, 0);
        assertStyle(
                new int[] {1, 1},
                new int[][] {{27, 57}, {74, 142}},
                new int[] {1, 2}
        );
//        renderHTML(map);
    }

    @Test
    public void fourVersions() throws IOException, BadLocationException {
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
                new int[] {2, 3, 3}
        );

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
    public void filtering() throws IOException, BadLocationException {
        // deletion in preamble, small deletion and addition of comment
        map = perform(0, EnumSet.of(Change.Flag.PREAMBLE, Change.Flag.COMMENT),
                "Lorem ipsumm dolor",
                "pre  \\begin{document}Lorem ipsumm dolor",
                "  \\begin{document}Lorem ipsumm dolor",
                "  \\begin{document}Lorem   ipsum dolor",
                "  \\begin{document}Lorem ipsum  dolor % amet."
        );
        assertMap("  \\begin{document}Lorem ipsumm  dolor % amet.", 2, 0);
        assertStyle(
                new int[] {1, 2},
                new int[][] {{0, 18}, {29, 30}},
                new int[] {1, 3}
        );
        // deletion in preamble, small deletion and addition of comment
        map = perform(0, EnumSet.of(Change.Flag.PREAMBLE, Change.Flag.COMMAND, Change.Flag.DELETION),
                "pra   Lorem ipsumm dolor",
                "pre  Lorem ipsumm dolor",
                "pre  \\begin{document}Lorem ipsumm dolor",
                "pre  \\begin{document}Lorem   ipsum dolor",
                "pre  \\begin{document}Lorem ipsum  dolor \\amet."
        );
        assertMap("pre  \\begin{document}Lorem ipsum  dolor \\amet.", 1, 0);
        assertStyle(
                new int[] {1},
                new int[][] {{45, 46}},
                new int[] {4}
        );
        // preamble location
        map = perform(0, EnumSet.of(Change.Flag.PREAMBLE),
                " pre2 \\begin{document}Lorem ipsum dolor",
                " pre2 Lorem   ipsum dolor",
                "pre1 pre2 Lorem ipsum  dolor"
        );
        assertMap("pre1 pre2 \\begin{document} Lorem ipsum  dolor", 2, 0);
        assertStyle(
                new int[] {1, 2},
                new int[][] {{0, 5}, {9, 26}},
                new int[] {2, 1}
        );
        // deletion in preamble, small deletion and addition of comment
        map = perform(0, EnumSet.of(Change.Flag.DELETION, Change.Flag.COMMAND),
                "pra   Lorem ipsumm dolor",
                "pre  Lorem ipsumm dolor",
                "pre  \\begin{document}Lorem ipsumm dolor",
                "pre  \\begin{document}Lorem   ipsum dolor",
                "pre  \\begin{document}Lorem ipsum  dolor \\amet."
        );
        assertMap("pre  \\begin{document}Lorem ipsum  dolor \\amet.", 2, 0);
        assertStyle(
                new int[] {1, 1},
                new int[][] {{2, 3}, {45, 46}},
                new int[] {1, 4}
        );
    }

    // render given text as HTML, so as to cut and paste into a browser
    @SuppressWarnings("unchecked")
    private static void renderHTML(Map map) {
        String text = (String) map.get(LTCserverInterface.KEY_TEXT);
        List<Integer[]> styles = (List<Integer[]>) map.get(LTCserverInterface.KEY_STYLES);

        System.out.format("\n<html><head></head><body>\n");

        int start = 0;

        if (styles.isEmpty())
            System.out.format("%s", text.replaceAll("\n", "<br>"));
        else {
            for (Integer[] tuple : styles) {
                if (tuple[0] > start)
                    System.out.format("%s",text.substring(start, tuple[0]).replaceAll("\n","<br>"));
                start = tuple[1];
                System.out.format("<font color=\"#%s\"><%c>%s</%c></font>",
                        Integer.toHexString((colors.get(tuple[3]).getRGB() & 0xffffff) | 0x1000000).substring(1),
                        HTML_STYLES[tuple[2]-1],
                        text.substring(tuple[0], tuple[1]).replaceAll("\n", "<br>"),
                        HTML_STYLES[tuple[2]-1]);
            }
            System.out.format("%s",text.substring(Math.min(start, text.length())).replaceAll("\n","<br>")); // any remaining unstyled text
        }

        System.out.format("\n</body></html>\n\n");
    }
}
