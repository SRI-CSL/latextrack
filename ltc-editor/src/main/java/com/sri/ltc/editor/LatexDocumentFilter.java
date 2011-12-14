/**
 ************************ 80 columns *******************************************
 * LatexDocumentFilter
 *
 * Created on Aug 16, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.editor;

import com.sri.ltc.server.LTCserverInterface;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author linda
 */
public class LatexDocumentFilter extends DocumentFilter {

    private final List<DocumentChange> changes = new ArrayList<DocumentChange>();
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>();
    private final LatexPane textPane;
    private Color color = Color.black;

    public LatexDocumentFilter(LatexPane textPane) {
        this.textPane = textPane;
    }

    public List<String[]> getRecentEdits() {
        List<String[]> edits = new ArrayList<String[]>();
        synchronized (changes) {
            for (DocumentChange change : changes)
                edits.add(new String[] {
                        change.type.name(),
                        ""+change.offset,
                        LTCserverInterface.EditType.REMOVE.equals(change.type)?""+change.length:change.text
                });
            changes.clear();
            for (ChangeListener listener : listeners)
                listener.stateChanged(new ChangeEvent(changes));
        }
        return edits;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        if (length == 0) return;

        // data structure to facilitate recording consecutive changes
        List<DocumentChange> microChanges = new ArrayList<DocumentChange>();

        StyledDocument document = (StyledDocument) fb.getDocument();
        Style style = document.getStyle(LatexPane.STYLE_PREFIX+2);
        StyleConstants.setForeground(style, color);
        // go through each character and check: already removed & addition by current author or not
        int current_offset = 0, current_increment = 1;
        int index = offset;
        for (; index < offset+length+current_offset; index+=current_increment) {
            current_increment = 1;
            if (document.getCharacterElement(index).getAttributes().getAttribute(StyleConstants.StrikeThrough) == null) {
                // character is not strike-through
                Object underline = document.getCharacterElement(index).getAttributes().getAttribute(StyleConstants.Underline);
                Object author = document.getCharacterElement(index).getAttributes().getAttribute(StyleConstants.Foreground);
                if (underline instanceof Boolean && (Boolean) underline &&
                        author instanceof Color && ((Color) author).equals(color)) {
                    // pre-record DELETE
                    microChanges.add(new DocumentChange(LTCserverInterface.EditType.DELETE, index, 1, document.getText(index, 1)));
                    // remove characters added by the same author
                    fb.remove(index, 1);
                    current_offset--;
                    current_increment = 0;
                } else {
                    // pre-record REMOVE
                    microChanges.add(new DocumentChange(LTCserverInterface.EditType.REMOVE, index, 1, document.getText(index, 1)));
                    // strike-through characters that are not-marked up or added by different author
                    fb.replace(index, 1, document.getText(index, 1), style);
                }
            }
        }

        // move caret (this also clears any selection)
        if (KeyEvent.VK_BACK_SPACE == textPane.last_key_pressed)
            textPane.setCaretPosition(offset);
        else
            textPane.setCaretPosition(index);

        // record consecutive REMOVE and DELETE elements
        if (!microChanges.isEmpty()) {
            // keepers:
            LTCserverInterface.EditType currentType = microChanges.get(0).type;
            StringBuffer currentText = new StringBuffer(microChanges.get(0).text);
            int currentOffset = microChanges.get(0).offset;
            int priorOffset = currentOffset;
            synchronized (changes) {
                for (int i=1; i<microChanges.size(); i++) {
                    DocumentChange c = microChanges.get(i);
                    if (c.type.equals(currentType) && c.offset == priorOffset + 1) {
                        currentText.append(c.text);
                        priorOffset = c.offset;
                    } else {
                        // add accumulated change from current keepers
                        changes.add(new DocumentChange(currentType, currentOffset, currentText.length(), currentText.toString()));
                        // reset keepers:
                        currentType = c.type;
                        currentText = new StringBuffer(c.text);
                        currentOffset = c.offset;
                        priorOffset = currentOffset;
                    }
                }
                // add current change
                changes.add(new DocumentChange(currentType, currentOffset, currentText.length(), currentText.toString()));
                for (ChangeListener listener : listeners)
                    listener.stateChanged(new ChangeEvent(changes));
            }
        }
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
        if ("".equals(text)) return;

        Style style = ((StyledDocument) fb.getDocument()).getStyle(LatexPane.STYLE_PREFIX+1);
        StyleConstants.setForeground(style, color);
        fb.insertString(offset, text, style);

        // record INSERT
        synchronized (changes) {
            changes.add(new DocumentChange(LTCserverInterface.EditType.INSERT, offset, text.length(), text));
            for (ChangeListener listener : listeners)
                listener.stateChanged(new ChangeEvent(changes));
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        int last_change = changes.size();
        this.remove(fb, offset, length);
        // calculate current offset for insertion: add up lengths of REMOVEs but ignore DELETEs
        int currentOffset = offset;
        for (int i=last_change; i<changes.size(); i++) {
            DocumentChange change = changes.get(i);
            if (LTCserverInterface.EditType.REMOVE.equals(change.type))
                currentOffset += change.length; 
        }
        this.insertString(fb, currentOffset, text, attrs);
    }

    private class DocumentChange {

        private final LTCserverInterface.EditType type;
        private final int offset;
        private final int length;
        private final String text;

        private DocumentChange(LTCserverInterface.EditType type, int offset, int length, String text) {
            this.type = type;
            this.offset = offset;
            this.length = length;
            this.text = text;
        }

        @Override
        public String toString() {
            return type+", "+offset+", "+text;
        }
    }
}
