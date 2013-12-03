package com.sri.ltc.editor;

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

import articles.showpar.ShowParEditorKit;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sri.ltc.CommonUtils;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * @author linda
 */
@SuppressWarnings("serial")
public final class LatexPane extends JTextPane {

    private static final Logger LOGGER = Logger.getLogger(LatexPane.class.getName());
    private static final String KEY_SHOW_PARAGRAPHS = "showParagraphs";
    private static final String REVISION_ATTR = "revision attribute";
    private static final String DATE_ATTR = "date attribute";
    protected final static String STYLE_PREFIX = "style no. ";

    private final LatexDocumentFilter documentFilter = new LatexDocumentFilter(this);
    protected int last_key_pressed = -1;
    private final boolean editable;
    private Point clickLocation = new Point();

    public LatexPane(boolean editable) {
        this.editable = editable;

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

        // popup menu for move and undo actions (only if editable)
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem item;
        item = popupMenu.add(new MoveToAction("Move to previous change", this, false, clickLocation));
        item = popupMenu.add(new MoveToAction("Move to next change", this, true, clickLocation));
        // if editable, then also allow "Undo" actions:
        if (editable) {
            popupMenu.addSeparator();
            // TODO: implement actions
            popupMenu.add(new JMenuItem("Undo this change"));
            popupMenu.add(new JMenuItem("Undo changes by same author"));
            popupMenu.add(new JMenuItem("Undo all changes in region"));
        }
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }
            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    clickLocation.setLocation(e.getPoint());
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
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
        Object revision = getStyledDocument().getCharacterElement(offset).getAttributes().getAttribute(REVISION_ATTR);
        Object date = getStyledDocument().getCharacterElement(offset).getAttributes().getAttribute(DATE_ATTR);
        return "<html>("+(line+1)+", "+(col+1)+") @ "+offset+
                (revision==null?"":"<br><b>rev:</b> "+revision.toString().substring(0, Math.min(8, revision.toString().length())))+
                (date==null?"":"<br><b>date:</b> "+CommonUtils.serializeDate((Date) date))+
                "</html>";
    }

    @Override
    public int getCaretPosition() {
        return super.getCaretPosition();
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
        setEditable(editable);
    }

    public List<Object[]> stopFiltering() {
        setEditable(false);
        StyledDocument document = getStyledDocument();
        ((AbstractDocument) document).setDocumentFilter(null);
        // collect any deletions
        List<Object[]> deletions = Lists.newArrayList();
        int start = -1;
        for (int i = 0; i < document.getLength(); i++) {
            Object strikethrough = document.getCharacterElement(i).getAttributes().getAttribute(StyleConstants.StrikeThrough);
            if (strikethrough instanceof Boolean && (Boolean) strikethrough) {
                if (start == -1) // first deletion character
                    start = i;
            } else {
                if (start > -1) {
                    deletions.add(new Integer[] {start, i});
                    start = -1;
                }
            }
        }
        if (start > -1) // we ended with a deletion
            deletions.add(new Integer[] {start, document.getLength()});
        return deletions;
    }

    public StyledDocument clearAndGetDocument() throws BadLocationException {
        stopFiltering();
        StyledDocument document = getStyledDocument();
        document.remove(0, document.getLength());
        return document;
    }

    public void updateFromMaps(String text, List<Integer[]> styles, Map<Integer, Color> colors,
                               int caretPosition, List<String> orderedIDs, List<Object[]> commits) {
        try {
            StyledDocument document = clearAndGetDocument();
            if (text != null)
                document.insertString(0, text, null);
            if (styles != null) {
                Style style;
                for (Integer[] tuple : styles) {
                    if (tuple != null && tuple.length >= 5) {
                        style = document.getStyle(STYLE_PREFIX+tuple[2]);
                        StyleConstants.setForeground(style, colors.get(tuple[3]));
                        if (orderedIDs != null) {  // add meta data about this change
                            String revision = orderedIDs.get(tuple[4]);
                            style.addAttribute(REVISION_ATTR, revision);
                            if (commits != null) {
                                for (Object[] commit : commits)
                                    if (revision.equals(commit[0])) {
                                        try {
                                            style.addAttribute(DATE_ATTR,
                                                    CommonUtils.deSerializeDate(commit[4].toString()));
                                        } catch (ParseException e) {
                                            LOGGER.log(Level.SEVERE, "while parsing date for revision", e);
                                        }
                                        break;
                                    }
                            }
                        }
                        document.setCharacterAttributes(tuple[0], tuple[1]-tuple[0],
                                style,
                                true);
                    }
                }
            }
            // set proper caret position and scroll to it
            if (caretPosition < 0)
                caretPosition = 0;
            if (caretPosition > getDocument().getLength())
                caretPosition = getDocument().getLength();
            setCaretPosition(caretPosition);
            scrollRectToVisible(modelToView(getCaret().getDot()));
            requestFocusInWindow();
        } catch (BadLocationException e) {
            LOGGER.log(Level.SEVERE, "while updating text", e);
        }
        startFiltering();
    }

    /**
     * Compare text attributes at given indices whether or not they are equal.
     *
     * If at least one of the given indices is outside the scope of the current document, this
     * test returns false.
     *
     * @param start First position to test against.  If outside of document, will return false
     * @param index Position to compare with first.  If outside of document, will return false
     * @param attrs Set of text attributes to look for.  If <code>null</code> or empty set given,
     *              then the test is always positive (i.e., returns true).  Otherwise, all the
     *              given attributes must match
     * @return true, if all attributes of interest are equal at both indices and false otherwise
     */
    protected boolean areEqualIndices(int start, int index, Set<TextAttribute> attrs) {
        if (attrs == null)
            return true;
        // test that neither position is out of bounds:
        for (int i : Sets.newHashSet(start, index))
            if (i < 0 || i > getDocument().getLength())
                return false;
        // go through all attributes and compare them for the given indices:
        // if any does not match, we can abort and return false
        for (TextAttribute attribute : attrs)
            if (!attribute.isMatch(
                    getStyledDocument().getCharacterElement(start).getAttributes(),
                    getStyledDocument().getCharacterElement(index).getAttributes()))
                return false;
        return true;
    }
    enum TextAttribute {
        Style {
            @Override
            boolean isMatch(AttributeSet as1, AttributeSet as2) {
                String name1 = (String) as1.getAttribute(StyleConstants.NameAttribute);
                String name2 = (String) as2.getAttribute(StyleConstants.NameAttribute);
                return name1 == null?name2 == null:name1.equals(name2);
            }
        },
        Revision {
            @Override
            boolean isMatch(AttributeSet as1, AttributeSet as2) {
                String rev1 = (String) as1.getAttribute(REVISION_ATTR);
                String rev2 = (String) as2.getAttribute(REVISION_ATTR);
                return rev1 == null?rev2 == null:rev1.equals(rev2);
            }
        },
        Author {
            @Override
            boolean isMatch(AttributeSet as1, AttributeSet as2) {
                Color c1 = (Color) as1.getAttribute(StyleConstants.Foreground);
                Color c2 = (Color) as2.getAttribute(StyleConstants.Foreground);
                return c1 == null?c2 == null:c1.equals(c2);
            }
        };
        abstract boolean isMatch(AttributeSet as1, AttributeSet as2);
    }
}
