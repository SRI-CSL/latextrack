package com.sri.ltc.latexdiff;

import com.sri.ltc.server.LTCserverInterface;
import org.junit.Ignore;
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
@Ignore
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
        return perform(caretPosition, EnumSet.noneOf(Change.Flag.class), texts);
    }

    private static Map perform(int caretPosition, Set<Change.Flag> flagsToHide, String... texts) throws Exception {
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
        try {
            perform(0, (String) null, (String) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void oneOrNoVersions() throws Exception {
        try {
            map = perform(0, (String[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertMap("", 0, 0);
        map = perform(0, EnumSet.of(Change.Flag.PREAMBLE, Change.Flag.COMMENT, Change.Flag.DELETION), "");
        assertMap("", 0, 0);
        String text = "Hello World.";
        int position = 7;
        try {
            map = perform(position, text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertMap(text, 0, position);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void twoVersions() throws Exception {
        // adding paragraph and more at end
        try {
            map = perform(22,
                    "Lorem ipsum dolor sit    \t",
                    "   Lorem ipsum \n \t\n  dolor sit amet. "
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertMap("   Lorem ipsum \n \t\n  dolor sit amet. ", 2, 22);
        assertStyle(
                new int[] {1, 1}, // all 2 markups are additions
                new int[][] {{14, 21}, {30, 37}},
                null);

        // removing paragraph in the middle and more at end
        try {
            map = perform(25,
                    "   Lorem ipsum \n \ndolor sit amet. ",
                    "Lorem ipsum dolor sit    \t"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        try {
            map = perform(30,
                    "\t  Lorem ipsum dolor sit amet; \nconsectetur adipiscing elit.",
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. \n "
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertMap("Lorem ipsum dolor sit amet; \n, consectetur adipiscing elit. \n ", 2, 33);
        assertStyle(
                new int[] {2, 1}, // one deletion and one addition
                new int[][]{{26, 29}, {29, 31}},
                null);

        // small changes:
        try {
            map = perform(12,
                    "\t  Lorem ippsu dolor sit amet",
                    "Lorem ipsum dolor sit amt \n "
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        try {
            map = perform(3,
                    "\n\nIf % or should this be ``When''?\nin the Course",
                    "\n\nWhen in the Course");
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertMap("\n\nIf % or should this be ``When''?\nWhen in the Course", 2, 36);
        assertStyle(
                new int[] {2, 1},
                new int[][] {{2, 35}, {35, 40}},
                null);
    }

    // TODO: exercise various filters with small and large changes (2 and 3 versions)

    @Test
    public void threeVersions() throws Exception {
        // small changes accumulate: positioning!
        try {
            map = perform(0,
                    " Lorem isut",
                    "Lorem   isum",
                    "  Lorem  ipsum"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertMap("  Lorem  ipsutm", 3, 0);
        assertStyle(
                new int[] {1, 2, 1},
                new int[][] {{10, 11}, {13, 14}, {14, 15}},
                new int[] {2, 1, 1}
        );
        try {
            map = perform(0,
                    " Lorem iut",
                    "Lorem   isu",
                    "  Lorem  ipsum"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertMap("  Lorem  ipsumt", 4, 0);
        assertStyle(
                new int[] {1, 1, 1, 2},
                new int[][] {{10, 11}, {11, 12}, {13, 14}, {14, 15}},
                new int[] {2, 1, 2, 1}
        );
        try {
            map = perform(0,
                    "Lorem ipsum dorstamet,",
                    " Lorem ipsum  dorstamt,",
                    "Lorem  ipsum  dorsitamt,",
                    "Lorem ipsum dolorsitamt,"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertMap("Lorem ipsum dolorsitamet,", 3, 0);
        assertStyle(
                new int[] {1, 1, 2},
                new int[][] {{14, 16}, {18, 19}, {22, 23}},
                new int[] {3, 2, 1}
        );
        // no change in latest version
        try {
            map = perform(0,
                    "\t  Lorem ipsum; dolor sit amet.\n",
                    "\t Lorem   ipsum dolor sit amet,  ",
                    "Lorem ipsum dolor sit amet, \n "
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertMap("Lorem ipsum; dolor sit amet.\n, \n ", 3, 0);
        assertStyle(
                new int[] {2, 2, 1}, // 2 deletions and 1 addition
                new int[][] {{11, 12}, {27, 29}, {29, 33}},
                new int[] {1, 1});
        // no change from first to second version
        try {
            map = perform(0,
                    "\t Lorem   ipsum dolor sit amet,  ",
                    " Lorem ipsum dolor sit amet, \n ",
                    "Lorem ipsum; dolor sit amet.\n"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertMap("Lorem ipsum; dolor sit amet, \n .\n", 3, 0);
        assertStyle(
                new int[] {1, 2, 1}, // 2 deletions and 1 addition
                new int[][] {{11, 13}, {27, 31}, {31, 33}},
                new int[] {2, 2, 2}
        );
        // back and forth
        try {
            map = perform(0,
                    "Lorem\t ipsum dolor",
                    "Lorem dolor",
                    "Lorem  ipsum"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        try {
            map = perform(0,
                    "Lorem ipm   \\dolor amet",
                    "Lorem ipsum \\dolor",
                    "Lorem  ipsum"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        try {
            map = perform(0,
                    "\t  Lorem ipsum; dolor sit amet.\n",
                    "Lorem ipsum dolor \nsit amet",
                    "Lorem ipsum dolor sit amet, \n "
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertMap("Lorem ipsum; dolor sit amet., \n ", 3, 0);
        assertStyle(
                new int[] {2, 2, 1},
                new int[][] {{11, 12}, {27, 28}, {28, 32}},
                new int[] {1, 1, 2}
        );
        // recreate problem with comment:
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
    public void fourVersions() throws Exception {
        // back and forth
        try {
            map = perform(0,
                    "Lorem ipsum",
                    "Lorem ipsum dolor",
                    "Lorem dolor",
                    "Lorem ipsum"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertMap("Lorem ipsum dolor ipsum", 3, 0);
        assertStyle(
                new int[] {2, 2, 1},
                new int[][] {{5, 11}, {11, 17}, {17, 23}},
                new int[] {2, 3, 3}
        );

        // delete in 2nd version, add back in 4th version, add in 3rd and 4th version,...
        try {
            map = perform(0,
                    "  Lorem ipsum dolor sit amet\n \n adipiscing elit. ",
                    "Lorem ipsum \n sit amet, consectetur rerum  adipiscing elit. ",
                    "\tLorem ipsum  sit amet, consectetur  \t  rerum adipiscing elit, aliquam commodo.\n",
                    "Lorem ipsum   dolor sit amet, consectetur adipiscing elit. Aliquam commodo. "
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
//        renderHTML(map);
    }

    @Test
    public void filtering() throws Exception {
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
        // deletion in preamble, small deletion and addition of preamble
        map = perform(0, EnumSet.of(Change.Flag.PREAMBLE, Change.Flag.COMMAND, Change.Flag.DELETION),
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
                new int[] {2, 4}
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
        assertMap("pre  \\begin{document}Lorem ipsum  dolor \\amet.", 3, 0);
        assertStyle(
                new int[] {1, 1, 1},
                new int[][] {{2, 3}, {11, 21}, {45, 46}},
                new int[] {1, 2, 4}
        );
        // filtering comments
        map = perform(3, EnumSet.of(Change.Flag.COMMENT),
                "\n\nIf % or should this be ``When''?\nin the Course",
                "\n\nWhen in the Course"
        );
        assertMap("\n\nIf % or should this be ``When''? in the Course", 2, 28);
        assertStyle(
                new int[] {2, 2},
                new int[][] {{2, 27}, {31, 34}},
                null
        );
    }


    @Test
    public void testPreambleBleeding() throws Exception {
        // in this case, the preamble additions/removals were throwing off a later lexeme
        // by making it appear to be part of the preamble. The original examples came from
        // the tutorial/independence.tex files, hence the naming of the variables.

        MarkedUpDocument document = new MarkedUpDocument();

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

        map = perform(0, EnumSet.of(Change.Flag.SMALL, Change.Flag.COMMENT), new String[] { version2Text, version3Text, version4Text });
        int styleCountWithPreamble = ((List<Integer[]>) map.get(LTCserverInterface.KEY_STYLES)).size();

        map = perform(0, EnumSet.of(Change.Flag.SMALL, Change.Flag.COMMENT, Change.Flag.PREAMBLE), new String[] { version2Text, version3Text, version4Text });
        int styleCountWithoutPreamble = ((List<Integer[]>) map.get(LTCserverInterface.KEY_STYLES)).size();

        // If this is broken, more than just one change (the change in the preamble) will show up
        assertEquals("Style count without preamble", 1, styleCountWithPreamble - styleCountWithoutPreamble);
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
