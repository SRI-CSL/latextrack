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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javax.swing.text.*;
import java.awt.*;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.List;

/**
 * Document with mark ups concerning additions and deletions, as well as status flags.
 *
 * @author linda
 */
public final class MarkedUpDocument extends DefaultStyledDocument {

    public enum KEYS {TEXT, POSITION}

    private final static String UNADORNED_STYLE = "unadorned";
    private final static String ADDITION_STYLE = "addition";
    private final static String DELETION_STYLE = "deletion";
    private final static String AUTHOR_INDEX = "author index";
    private final static String FLAGS_ATTR = "flag attribute";
    private static final long serialVersionUID = -6945312419206148753L;

    private Position caret = getStartPosition();

    public MarkedUpDocument() {
        // define styles for additions and deletions
        Style style;

        style = addStyle(ADDITION_STYLE, null);
        StyleConstants.setUnderline(style, true);

        style = addStyle(DELETION_STYLE, null);
        StyleConstants.setStrikeThrough(style, true);

        style = addStyle(UNADORNED_STYLE, null);
    }

    public MarkedUpDocument(String initialText, List<Object[]> deletions, int caretPosition) throws BadLocationException {
        this();
        insertString(0, initialText, null);
        // markup deletions:
        if (deletions != null) {
            Style deletionStyle = getStyle(DELETION_STYLE);
            StyleConstants.setForeground(deletionStyle, Color.black); // color doesn't matter at initialization
            for (Object[] pair : deletions) {
                if (pair == null || pair.length != 2)
                    throw new RuntimeException("Cannot create markup document with deletion that is not a pair");
                setCharacterAttributes((Integer) pair[0], ((Integer) pair[1]) - ((Integer) pair[0]), deletionStyle, true);
            }
        }
        caret = createPosition(caretPosition);
    }

    public static Map<KEYS,Object> applyDeletions(String currentText, List<Object[]> deletions, int caretPosition)
            throws BadLocationException {
        Map<KEYS,Object> map = Maps.newHashMap();

        // remove deletions (if any) and adjust caret position
        if (deletions != null && !deletions.isEmpty()) {
            MarkedUpDocument document = new MarkedUpDocument(currentText, deletions, caretPosition);
            document.removeDeletions();
            currentText = document.getText(0, document.getLength());
            caretPosition = document.getCaretPosition();
        }

        map.put(KEYS.TEXT, currentText);
        map.put(KEYS.POSITION, caretPosition);
        return map;
    }

    public int getCaretPosition() {
        return caret.getOffset();
    }

    /**
     * Remove any characters in the document that are marked as deletions.
     * This updates the caret position accordingly.
     */
    public void removeDeletions() throws BadLocationException {
        for (int i = 0; i < getLength(); i++) {
            Object strikethrough = getCharacterElement(i).getAttributes().getAttribute(StyleConstants.StrikeThrough);
            if (strikethrough instanceof Boolean && (Boolean) strikethrough) {
                remove(i, 1);
                i--;
            }
        }
    }

    public void updateAuthor(int authorIndex, Color authorColor) {
        // prepare styles with color and author index
        Style style;
        style = getStyle(DELETION_STYLE);
        StyleConstants.setForeground(style, authorColor);
        style.addAttribute(AUTHOR_INDEX, authorIndex);
        style = getStyle(ADDITION_STYLE);
        StyleConstants.setForeground(style, authorColor);
        style.addAttribute(AUTHOR_INDEX, authorIndex);
    }

    public void insertDeletion(int offset, String text, Set<Change.Flag> flags) throws BadLocationException {
        Style style;
        if (flags.contains(Change.Flag.WHITESPACE)) {
            // we're not interested in marking up spare newlines as deletions - that just confuses the
            // display. Instead, we will mark it, and other whitespace changes, with a special, "unadorned"
            // style to distinguish them from other deletions.
            text = text.replaceAll("[\\r\\n]", " ");
            // SAB: this is causing other problems, like turning a contiguous block of changes
            // into an interrupted set of changes. Turning this off until we can figure out a
            // better approach.
            //style = getStyle(UNADORNED_STYLE);
            style = getStyle(DELETION_STYLE);
        } else {
            style = getStyle(DELETION_STYLE);
        }
        
        style.addAttribute(FLAGS_ATTR, flags);
        insertString(offset, text, style);
    }

    /**
     * markup everything between start_position and end_position except currently marked ADDITIONS and DELETIONS
     *
     * @param start_position
     * @param end_position
     * @param flags
     */
    public void markupAddition(int start_position, int end_position, Set<Change.Flag> flags) {
        Style style;
        for (int i = start_position; i < end_position; i++) {
            Object styleName = getCharacterElement(i).getAttributes().getAttribute(StyleConstants.NameAttribute);
            if (!DELETION_STYLE.equals(styleName) && !ADDITION_STYLE.equals(styleName) && (!UNADORNED_STYLE.equals(styleName))) {
                style = getStyle(ADDITION_STYLE);
                style.addAttribute(FLAGS_ATTR, flags);
                setCharacterAttributes(i, 1, style, true);
            }
        }
    }

    public boolean isAddition(int pos) throws BadLocationException {
        if (pos < 0 || pos >= getLength())
            throw new BadLocationException("Cannot determine whether character is addition", pos);
        Object styleName = getCharacterElement(pos).getAttributes().getAttribute(StyleConstants.NameAttribute);
        return ADDITION_STYLE.equals(styleName);
    }

    /**
     * Apply the flags to hide to filter text markup.  If deletions are to be hidden, this will remove any text
     * that is marked up as a deletion, therefore the caret position needs to be tracked as well.
     *
     * @param flagsToHide set of flags denoting which features to hide
     * @param caretPosition current caret position
     * @return caret position after filters were applied
     * @throws BadLocationException if during the filtering process an unknown text position is encountered
     */
    @SuppressWarnings("unchecked")
    public int applyFiltering(Set<Change.Flag> flagsToHide, int caretPosition) throws BadLocationException {
        caret = createPosition(caretPosition);
        if (!flagsToHide.isEmpty())
            for (int i = 0; i < getLength(); i++) {
                Set<Change.Flag> flags = (Set<Change.Flag>) getCharacterElement(i).getAttributes().getAttribute(FLAGS_ATTR);
                if (flags != null) {
                    Set<Change.Flag> intersection = Sets.intersection(flags, flagsToHide);
                    if (!intersection.isEmpty()) { // matching flags
                        if (flags.contains((Change.Flag.DELETION))) { // change was a deletion, so remove character
                            remove(i, 1);
                            i--;
                        } else { // change was an addition, so remove all attributes to hide it
                            setCharacterAttributes(i, 1, SimpleAttributeSet.EMPTY, true);
                        }
                    }
                }
            }
        return getCaretPosition();
    }

    public Reader getReader() throws BadLocationException {
        return new StringReader(getText(0, getLength()));
    }

    /**
     * Transform this marked up document into the list of 4-tuples that denote the style of a text section.
     *
     * @return List of 4-tuples (Integer array) with start and end indices, type of markup and author key.
     */
    public List<Integer[]> getStyles() {
        List<Integer[]> list = new ArrayList<Integer[]>();
        Chunk c = traverse(getDefaultRootElement(), null, list);
        // handle last chunk and add it if not default style:
        c.end = getLength()+1;
        if (!"default".equals(c.style))
            list.add(c.asList());
        return list;
    }

    private Chunk traverse(Element element, Chunk last, List<Integer[]> list) {
        Chunk chunk = last;
        if (element.isLeaf()) {
            AbstractDocument.LeafElement leaf = (AbstractDocument.LeafElement) element;
            chunk = new Chunk(element.getStartOffset(),
                    (String) leaf.getAttribute(StyleConstants.NameAttribute),
                    (Color) leaf.getAttribute(StyleConstants.Foreground),
                    (Integer) leaf.getAttribute(AUTHOR_INDEX));
            if (chunk.equals(last))
                return last;
            else {
                if (last != null) {
                    last.end = element.getStartOffset();
                    if (!"default".equals(last.style))
                        list.add(last.asList());
                }
                return chunk;
            }
        } else {
            for (int i=0; i < element.getElementCount(); i++) {
                chunk = traverse(element.getElement(i), chunk, list);
            }
        }
        return chunk;
    }

    private final class Chunk {
        final int start;
        int end;
        final String style;
        final Color color;
        final Integer author;

        private Chunk(int start, String style, Color color, Integer author) {
            this.start = start;
            this.style = style;
            this.color = color;
            this.author = author;
        }

        private Integer[] asList() {
            // TODO: remove hardcoded values and replace with constants
            // consider creating an enum and a map to convert that enum to string
            Integer[] values = {start, end, 0, 0};
            if (ADDITION_STYLE.equals(style))
                values[2] = 1;
            if (DELETION_STYLE.equals(style))
                values[2] = 2;
            if (UNADORNED_STYLE.equals(style))
                values[2] = 3;

            if (author != null)
                values[3] = author;
            return values;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Chunk chunk = (Chunk) o;

            if (author != null ? !author.equals(chunk.author) : chunk.author != null) return false;
            if (color != null ? !color.equals(chunk.color) : chunk.color != null) return false;
            if (style != null ? !style.equals(chunk.style) : chunk.style != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = style != null ? style.hashCode() : 0;
            result = 31 * result + (color != null ? color.hashCode() : 0);
            result = 31 * result + (author != null ? author.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "<chunk start="+start+
                    " end="+end+
                    " style=\""+style+"\""+
                    " author="+author+
                    " color=\""+color+"\" />";
        }
    }
}
