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
        return accumulate.perform(readers, null, showDeletions, showSmallChanges, showPreambleChanges, showCommentChanges, showCommandChanges);
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
        assertNotNull("Map of null texts", map);
        map = perform("");
        assertNotNull("Map for one empty text", map);
        map = perform("Hello World.");
        assertNotNull("Map for one text", map);
        assertTrue("Map has 2 entries", map.size() == 2);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void twoVersions() throws IOException, BadLocationException {
        // adding paragraph and more at end
        map = perform(
                "Lorem ipsum dolor sit    \t",
                "   Lorem ipsum \n \ndolor sit amet. "
        );
        assertNotNull(map);
        styles = (List<Integer[]>) map.get(LTCserverInterface.KEY_STYLES);
        assertNotNull(styles);
        assertTrue("2 markups", styles.size() == 2);

        // all markups are additions
        for (Integer[] style : styles)
            assertTrue(style[2] == 1);

        // removing paragraph and more at end
        map = perform(
                "   Lorem ipsum \n \ndolor sit amet. ",
                "Lorem ipsum dolor sit    \t"
        );
        assertNotNull(map);
        styles = (List<Integer[]>) map.get(LTCserverInterface.KEY_STYLES);
        assertNotNull(styles);
        assertTrue("2 markups", styles.size() == 2);
        // all markups are deletions
        for (Integer[] style : styles)
            assertTrue(style[2] == 2);

        // removing things but hiding deletions
        map = perform(false, true, true, true, true,
                "   Lorem ipsum \n \ndolor sit amet. ",
                "Lorem ipsum dolor sit    \t"
        );
        assertNotNull(map);
        assertTrue("styles are empty", ((List<Integer[]>) map.get(LTCserverInterface.KEY_STYLES)).isEmpty());

        // adding commands but hiding them
        map = perform(true, true, true, true, false,
                "Lorem ipsum dolor sit    \t",
                "   Lorem ipsum \\textbf{dolor} sit amet. "
        );
        assertNotNull(map);
        styles = (List<Integer[]>) map.get(LTCserverInterface.KEY_STYLES);
        assertNotNull(styles);
        assertTrue("3 markups", styles.size() == 3);

        // small replacement at end of text TODO: think about this behavior...
        map = perform(
                "  Lorem ipsum dolor sit amet.\n",
                "Lorem ipsum \ndolor sit amet, \n "
        );
        assertNotNull(map);
        renderHTML(map);
    }

    @Test
    public void threeVersions() throws IOException, BadLocationException, InterruptedException {
        // small replacement with trailing white space over 3 versions:
        map = perform(
                "\t  Lorem ipsum dolor sit amet.\n",
                "Lorem ipsum \ndolor sit amet",
                "Lorem ipsum dolor sit amet, \n "
        );
        assertNotNull(map);
        renderHTML(map);

        // TODO: recreate problem with comment:
        // adding text and then adding comment, but showing everything
        map = perform(
                "  Lorem ipsum dolor sit amet.\n",
                "Lorem ipsum \ndolor sit amet, consectetur adipiscing elit. \n ",
                "Lorem ipsum \ndolor sit amet, consectetur adipiscing elit. \n\n " +
                        "% ADDING MORE:\n\n" +
                        "Praesent tempor hendrerit eros, non scelerisque est fermentum nec. "
        );
        assertNotNull(map);
//        renderHTML(map);
    }

    // render given text as HTML, so as to cut and paste into a browser
    @SuppressWarnings("unchecked")
    private static void renderHTML(Map map) {
        String text = (String) map.get(LTCserverInterface.KEY_TEXT);
        List<Integer[]> styles = (List<Integer[]>) map.get(LTCserverInterface.KEY_STYLES);

        System.out.format("\n<html><head></head><body>\n");

        int start = 0;

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

        System.out.format("\n</body>\n\n");
    }
}
