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
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static Map perform(String... texts) throws IOException, BadLocationException {
        return perform(true, true, true, true, true, texts);
    }

    private static Map perform(boolean showDeletions,
                               boolean showSmallChanges,
                               boolean showPreambleChanges,
                               boolean showCommentChanges,
                               boolean showCommandChanges,
                               String... texts) throws IOException, BadLocationException {
        ReaderWrapper[] readers = null;
        if (texts != null) {
            readers = new ReaderWrapper[texts.length];
            for (int i=0; i<texts.length; i++)
                readers[i] = new StringReaderWrapper(texts[i]);
        }
        return accumulate.perform2(readers, null, showDeletions, showSmallChanges, showPreambleChanges, showCommentChanges, showCommandChanges);
    }

    @SuppressWarnings("unchecked")
    private void assertMap(String text, int styles) {
        assertNotNull(map);
        assertTrue("Map has 2 entries", map.size() == 2);
        assertEquals("Text is equal to", map.get(LTCserverInterface.KEY_TEXT), text);
        assertTrue("Number of styles", ((List<Integer[]>) map.get(LTCserverInterface.KEY_STYLES)).size() == styles);
    }

    @SuppressWarnings("unchecked")
    private void assertStyle(int[][] types, int[][] positions) {
        styles = (List<Integer[]>) map.get(LTCserverInterface.KEY_STYLES);
        for (int i = 0; i < types.length; i++)
            assertTrue("style type", styles.get(i)[2] == types[i][1]);
        for (int i = 0; i < positions.length; i++) {
            assertTrue("style start position", styles.get(i)[0] == positions[i][1]);
            assertTrue("style end position", styles.get(i)[1] == positions[i][2]);
        }
    }

    Map map;
    List<Integer[]> styles;

    @Test(expected = NullPointerException.class)
    public void twoNullReaders() throws IOException, BadLocationException {
        perform(null, null);
    }

    @Test
    public void oneOrNoVersions() throws IOException, BadLocationException {
        map = perform((String[]) null);
        assertMap("", 0);
        map = perform("");
        assertMap("", 0);
        String text = "Hello World.";
        map = perform(text);
        assertMap(text, 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void twoVersions() throws IOException, BadLocationException {
        // adding paragraph and more at end
        map = perform(
                "Lorem ipsum dolor sit    \t",
                "   Lorem ipsum \n \t\n  dolor sit amet. "
        );
        assertMap("   Lorem ipsum \n \t\n  dolor sit amet. ", 2);
        assertStyle(
                new int[][] {{0, 1}, {1, 1}}, // all 2 markups are additions 
                new int[][] {{0, 15, 21}, {1, 31, 37}} // 1. = [15 21], 2. = [31 37]
        );

        // removing paragraph in the middle and more at end
        map = perform(
                "   Lorem ipsum \n \ndolor sit amet. ",
                "Lorem ipsum dolor sit    \t"
        );
        assertMap("Lorem ipsum \n \ndolor sit    \tamet. ", 2);
        assertStyle(
                new int[][]{{0, 2}, {1, 2}}, // all 2 markups are deletions
                new int[][]{{0, 12, 15}, {1, 29, 35}} // 1. = [12 15], 2. = [29 35]
        );

        // removing things but hiding deletions
        map = perform(false, true, true, true, true,
                "   Lorem ipsum \n \ndolor sit amet. ",
                "Lorem ipsum dolor sit    \t"
        );
//        assertMap("Lorem ipsum dolor sit    \t", 0);

        // adding commands but hiding them
        map = perform(true, true, true, true, false,
                "Lorem ipsum dolor sit    \t",
                "   Lorem ipsum \\textbf{dolor} sit amet. "
        );
//        assertMap("   Lorem ipsum \\textbf{dolor} sit amet. ", 3);
//        assertStyle(
//                new int[][]{{0, 1}, {1, 1}, {2, 1}}, // all 3 markups are additions
//                new int[][]{{0, 22, 23}, {1, 28, 30}, {2, 34, 40}}
//        );

        // deletion with lots of white space following:
        // think about this behavior: suggest to ignore end position of deletion???  NO.
        map = perform(
                "\t  Lorem ipsum dolor sit amet; \nconsectetur adipiscing elit.",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. \n "
        );
        assertMap("Lorem ipsum dolor sit amet; \n, consectetur adipiscing elit. \n ", 2);
        assertStyle(
                new int[][]{{0, 2}, {1, 1}}, // one deletion and one addition
                new int[][]{{0, 26, 29}, {1, 29, 31}}
        );

        // small changes:
        map = perform(
                "\t  Lorem ippsu dolor sit amet",
                "Lorem ipsum dolor sit amt \n "
        );
        assertMap("Lorem ippsum dolor sit amet \n ", 3);
        assertStyle(
                new int[][]{{0, 2}, {1, 1}, {2, 2}}, // 1 addition and 2 deletions
                new int[][]{{0, 8, 9}, {1, 11, 12}, {2, 25, 26}}
        );

        // TODO: hide small changes...
        map = perform(true, false, true, true, true,
                "\t  Lorem ippsu dolor sit amet",
                "Lorem ipsum dolor sit amt \n "
        );
    }

    @Ignore
    public void filtering() {
        // TODO: exercise various filters with small and large changes (2 and 3 versions)
    }

    @Test
    public void threeVersions() throws IOException, BadLocationException {
        // no change in latest version
        map = perform(
                "\t  Lorem ipsum; dolor sit amet.\n",
                "\t Lorem   ipsum dolor sit amet,  ",
                "Lorem ipsum dolor sit amet, \n "
        );
        assertMap("Lorem ipsum; dolor sit amet.\n, \n ", 3);
        assertStyle(
                new int[][] {{0, 2}, {1, 2}, {2, 1}}, // 2 deletions and 1 addition
                new int[][] {{0, 11, 13}, {1, 27, 29}, {2, 29, 33}}
        );

        // small replacement with trailing white space over 3 versions:
        map = perform(
                "\t  Lorem ipsum; dolor sit amet.\n",
                "Lorem ipsum dolor \nsit amet",
                "Lorem ipsum dolor sit amet, \n "
        );
//        assertMap("Lorem ipsum dolor sit amet.\n, \n ", 2);
//        assertStyle(
//                new int[][] {{0, 2}, {1, 1}}, // one deletion and one addition
//                new int[][] {{0, 26, 28}, {1, 28, 32}}
//        );
//        renderHTML(map);

        // TODO: recreate problem with comment:
        // adding text and then adding comment, but showing everything
        map = perform(true, true, true, false, true,
                "  Lorem ipsum dolor sit amet\n",
                "Lorem ipsum \ndolor sit amet, consectetur adipiscing elit. \n ",
                "Lorem ipsum \ndolor sit amet, consectetur adipiscing elit. \n " +
                        "% ADDING MORE:\n" +
                        "Praesent tempor hendrerit eros, non scelerisque est fermentum nec. "
        );
//        renderHTML(map);
    }

    @Ignore
    public void fourVersions() throws IOException, BadLocationException {
        // delete in 2nd version, add back in 4th version, add in 3rd and 4th version,...
        map = perform(
                "  Lorem ipsum dolor sit amet\n \n adipiscing elit. ",
                "Lorem ipsum \n sit amet, consectetur rerum  adipiscing elit. ",
                "\tLorem ipsum  sit amet, consectetur  \t  rerum adipiscing elit, aliquam commodo.\n",
                "Lorem ipsum   dolor sit amet, consectetur adipiscing elit. Aliquam commodo. "
        );
        renderHTML(map);
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
