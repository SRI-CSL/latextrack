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

import com.google.common.collect.Sets;
import com.sri.ltc.server.LTCserverInterface;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Set;

/**
 * @author linda
 */
public final class LatexDocumentFilter extends DocumentFilter {

    private final Set<ChangeListener> listeners = Sets.newHashSet();
    private Boolean firstChange = true;
    private final LatexPane textPane;
    private Color color = Color.black; // also used to synchronize on
    private boolean filter = false; // filtering status

    public LatexDocumentFilter(LatexPane textPane) {
        this.textPane = textPane;
    }

    public void setFilter(boolean filter) {
        this.filter = filter;
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void setColor(Color color) {
        synchronized (this.color) {
            this.color = color;
        }
    }

    public void resetChanges() {
        synchronized (color) {
            firstChange = true;
        }
    }

    private void fireChange() {
        if (firstChange) {
            for (ChangeListener listener : listeners)
                listener.stateChanged(new ChangeEvent(this));
            firstChange = false;
        }
    }

    /**
     * same as {@link #remove(javax.swing.text.DocumentFilter.FilterBypass, int, int)} but
     * returns the number of characters that are left in document.
     */
    private int removeAndCount(FilterBypass fb, int offset, int length) throws BadLocationException {
        if (length == 0) return 0;

        synchronized (color) {
            // prepare deletion style
            StyledDocument document = (StyledDocument) fb.getDocument();
            Style style = document.getStyle(LatexPane.LTCStyle.Deletion.getName());
            StyleConstants.setForeground(style, color);
            style.addAttribute(LatexPane.REVISION_ATTR, LTCserverInterface.MODIFIED);
            style.removeAttribute(LatexPane.DATE_ATTR);

            // go through each character and check: not already removed & addition by current author or not
            int removed = 0;
            int kept = 0; // count how many characters are kept in document
            for (int index = offset; index < offset+length-removed; index++) {
                if (document.getCharacterElement(index).getAttributes().getAttribute(StyleConstants.StrikeThrough) == null) {
                    // character is not strike-through
                    Object underline = document.getCharacterElement(index).getAttributes().getAttribute(StyleConstants.Underline);
                    Object author = document.getCharacterElement(index).getAttributes().getAttribute(StyleConstants.Foreground);
                    if (underline instanceof Boolean && (Boolean) underline &&
                            author instanceof Color && ((Color) author).equals(color)) {
                        // remove characters added by the same author
                        fb.remove(index, 1);
                        removed++;
                        index--; // evaluate next character at the same position
                    } else {
                        // characters that are not-marked up or added by different author
                        fb.replace(index, 1, document.getText(index, 1), style);
                        kept++;
                    }
                } else { // character is already marked as deleted
                    kept++;
                }
            }

            fireChange();

            return kept;
        }
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        if (filter) {
            int kept = removeAndCount(fb, offset, length);
            // move caret (this also clears any selection)
            if (KeyEvent.VK_BACK_SPACE == textPane.last_key_pressed)
                textPane.setCaretPosition(offset);
            else
                textPane.setCaretPosition(offset+kept);
        } else
            super.remove(fb, offset, length);
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
        if ("".equals(text)) return;

        if (filter)
            synchronized (color) {
                Style style = ((StyledDocument) fb.getDocument()).getStyle(LatexPane.LTCStyle.Addition.getName());
                StyleConstants.setForeground(style, color);
                style.addAttribute(LatexPane.REVISION_ATTR, LTCserverInterface.MODIFIED);
                style.removeAttribute(LatexPane.DATE_ATTR);
                fb.insertString(offset, text, style); // TODO: does this move caret to end of insertion?
                fireChange();
            }
        else
            super.insertString(fb, offset, text, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (filter)
            synchronized (color) {
                int kept = this.removeAndCount(fb, offset, length);
                this.insertString(fb, offset+kept, text, attrs);
            }
        else
            super.replace(fb, offset, length, text, attrs);
    }
}
