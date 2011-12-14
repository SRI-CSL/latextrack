/**
 ************************ 80 columns *******************************************
 * LatexPane
 *
 * Created on Jan 11, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.editor;

import articles.showpar.ShowParEditorKit;
import com.sri.ltc.filter.Filtering;
import com.sri.ltc.latexdiff.Accumulate;
import com.sri.ltc.latexdiff.FileReaderWrapper;
import com.sri.ltc.latexdiff.ReaderWrapper;
import com.sri.ltc.server.LTCserverInterface;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * @author linda
 */
@SuppressWarnings("serial")
public final class LatexPane extends JTextPane {

    protected static String KEY_COLORS = "colors";
    private static String KEY_SHOW_PARAGRAPHS = "showParagraphs";
    protected final static String STYLE_PREFIX = "style no. ";
    private final LatexDocumentFilter documentFilter = new LatexDocumentFilter(this);
    protected int last_key_pressed = -1;

    public LatexPane() {
        // to make white-space displayable
        setEditorKit(new ShowParEditorKit());
        getDocument().putProperty("show paragraphs",
                Preferences.userNodeForPackage(this.getClass()).getBoolean(KEY_SHOW_PARAGRAPHS, false)?"":null);
        // miscellaneous stuff:
        getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                last_key_pressed = e.getKeyCode();
            }
        });
        // define styles for additions and deletions
        StyledDocument document = getStyledDocument();
        Style style;
        style = document.addStyle(STYLE_PREFIX+1, null); // addition
        StyleConstants.setUnderline(style, true);
        style = document.addStyle(STYLE_PREFIX+2, null); // deletion
        StyleConstants.setStrikeThrough(style, true);
        // more initialization
        setCaretPosition(0);
        setMargin(new Insets(5,5,5,5));
        setEditable(false);
        Font font = new Font("Monospaced", Font.PLAIN, 12);
        setFont(font);
        setJTextPaneFont(this, font, Color.black);
        setToolTipText(""); // turns on tool tips
        // show tool tips almost immediately
        ToolTipManager.sharedInstance().setInitialDelay(100);
        ToolTipManager.sharedInstance().setReshowDelay(100);
    }

    // show (line, column) location of text in tool tips
    @Override
    public String getToolTipText(MouseEvent event) {
        // convert mouse event location into line and column number
        int offset = viewToModel(event.getPoint());
        if (offset < 0)  // component not yet sized
            return "";
        Element root = getDocument().getDefaultRootElement();
        int line = root.getElementIndex(offset);
        int col = offset - root.getElement(line).getStartOffset();
        return "("+(line+1)+","+(col+1)+")@"+offset;
    }

    /**
     * Utility method for setting the font and color of a JTextPane. The
     * result is roughly equivalent to calling setFont(...) and
     * setForeground(...) on an AWT TextArea.
     * (from: {@link http://javatechniques.com/blog/setting-jtextpane-font-and-color/})
     *
     * @param jtp JTextPane, in which to set font and color
     * @param font Font to be used in JTextPane
     * @param c Color to be used as foreground color in JTextPane
     */
    private static void setJTextPaneFont(JTextPane jtp, Font font, Color c) {
        // Start with the current input attributes for the JTextPane. This
        // should ensure that we do not wipe out any existing attributes
        // (such as alignment or other paragraph attributes) currently
        // set on the text area.
        MutableAttributeSet attrs = jtp.getInputAttributes();

        // Set the font family, size, and style, based on properties of
        // the Font object. Note that JTextPane supports a number of
        // character attributes beyond those supported by the Font class.
        // For example, underline, strike-through, super- and sub-script.
        StyleConstants.setFontFamily(attrs, font.getFamily());
        StyleConstants.setFontSize(attrs, font.getSize());
        StyleConstants.setItalic(attrs, (font.getStyle() & Font.ITALIC) != 0);
        StyleConstants.setBold(attrs, (font.getStyle() & Font.BOLD) != 0);

        // Set the font color
        StyleConstants.setForeground(attrs, c);

        // Retrieve the pane's document object
        StyledDocument doc = jtp.getStyledDocument();

        // Replace the style for the entire document. We exceed the length
        // of the document by 1 so that text entered at the end of the
        // document uses the attributes.
        doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
    }

    public LatexDocumentFilter getDocumentFilter() {
        return documentFilter;
    }

    public boolean getShowParagraphs() {
        return Preferences.userNodeForPackage(this.getClass()).getBoolean(KEY_SHOW_PARAGRAPHS, false);
    }

    public void setShowParagraphs(boolean value) {
        Preferences.userNodeForPackage(this.getClass()).putBoolean(KEY_SHOW_PARAGRAPHS, value);
    }

    public void startFiltering() {
        ((AbstractDocument) getStyledDocument()).setDocumentFilter(documentFilter);
        setEditable(true);
    }

    public List<String[]> stopFiltering() {
        setEditable(false);
        ((AbstractDocument) getStyledDocument()).setDocumentFilter(null);
        return documentFilter.getRecentEdits();
    }

    public StyledDocument clearAndGetDocument() throws BadLocationException {
        if (isEditable())
            stopFiltering();
        StyledDocument document = getStyledDocument();
        document.remove(0, document.getLength());
        return document;
    }

    public void updateFromPaths(Object[] paths) {
        // need at least 2 paths that are not null or empty
        if (paths.length < 2 ||
                paths[0] == null || "".equals(paths[0].toString()) ||
                paths[1] == null || "".equals(paths[1].toString()))
            return; // do nothing

        try {
            // prepare wrappers for readers:
            ReaderWrapper[] readers = new FileReaderWrapper[paths.length];
            for (int i=0; i < paths.length; i++) {
                readers[i] = new FileReaderWrapper(paths[i].toString());
            }

            // reset current document and accumulate changes in it:
            StyledDocument document = clearAndGetDocument();
            Filtering filter = Filtering.getInstance();
            new Accumulate(document).perform(readers, null,
                    filter.getShowingStatus(LTCserverInterface.Show.DELETIONS),
                    filter.getShowingStatus(LTCserverInterface.Show.SMALL),
                    filter.getShowingStatus(LTCserverInterface.Show.PREAMBLE),
                    filter.getShowingStatus(LTCserverInterface.Show.COMMENTS),
                    filter.getShowingStatus(LTCserverInterface.Show.COMMANDS));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        startFiltering();
    }

    public void updateFromMaps(String text, List<Integer[]> styles, Map<Integer,Color> colors) {
        try {
            StyledDocument document = clearAndGetDocument();
            if (text != null)
                document.insertString(0, text, null);
            if (styles != null) {
                Style style;
                for (Integer[] tuple : styles) {
                    if (tuple != null && tuple.length == 4) {
                        style = document.getStyle(STYLE_PREFIX+tuple[2]);
                        StyleConstants.setForeground(style, colors.get(tuple[3]));
                        document.setCharacterAttributes(tuple[0], tuple[1]-tuple[0],
                                style,
                                true);
                    }
                }
            }
            setCaretPosition(0); // scroll to beginning?
            scrollRectToVisible(new Rectangle());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        startFiltering();
    }

    public Map<Integer,Color> getChangeColors() {
        Map<Integer,Color> colors = new HashMap<Integer,Color>();
        // go through document and fill map
        StyledDocument document = getStyledDocument();
        traverse(document.getDefaultRootElement(), colors);
        return colors;
    }

    private void traverse(Element element, Map<Integer,Color> map) {
        if (element.isLeaf()) {
            AbstractDocument.LeafElement leaf = (AbstractDocument.LeafElement) element;
            Color color = (Color) leaf.getAttribute(StyleConstants.Foreground);
            Integer index = (Integer) leaf.getAttribute(Accumulate.AUTHOR_INDEX);
            if (color != null && index != null && !map.containsKey(index))
                map.put(index, color);
        } else {
            for (int i=0; i < element.getElementCount(); i++) {
                traverse(element.getElement(i), map);
            }
        }
    }
}
