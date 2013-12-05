/*
 * #%L
 * LaTeX Track Changes (LTC) allows collaborators on a version-controlled LaTeX writing project to view and query changes in the .tex documents.
 * %%
 * Copyright (C) 2009 - 2013 SRI International
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
package com.sri.ltc.editor;

import com.google.common.collect.Sets;
import com.sri.ltc.filter.Author;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.*;
import java.util.SortedSet;

/**
 * A text field that displays a (sorted) set of authors in their color.
 *
 * It allows drag-n-drop and copy-n-paste of both, author and string objects.
 *
 * If editing by hand or dropping a string in there, it auto-completes the name
 * based on the contents of the set of currently known authors.
 *
 * @author linda
 */
public final class AuthorSetField extends JTextField {

    private final AuthorListModel authorModel;
    private final SortedSet<Author> currentAuthors = Sets.newTreeSet();

    public AuthorSetField(AuthorListModel authorModel) {
        this.authorModel = authorModel;
        addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent caretEvent) {
//                System.out.println("Caret event: "+caretEvent);
            }
        });
    }

    @Override
    protected Document createDefaultModel() {
        return new AutoListDocument();
    }

    private String getMatch(String s) {
        for (int i = 0; i < authorModel.getSize(); i++) {
            String s1 = ((AuthorCell) authorModel.getElementAt(i)).label;
            if (s1 != null && s1.toLowerCase().startsWith(s.toLowerCase()))
                return s1;
        }
        return null;
    }

    @Override
    public void replaceSelection(String s) {
        AutoListDocument autoDoc = (AutoListDocument) getDocument();
        if (autoDoc != null)
            try {
                int i = Math.min(getCaret().getDot(), getCaret().getMark());
                int j = Math.max(getCaret().getDot(), getCaret().getMark());
                autoDoc.replace(i, j - i, s, null);
            } catch (Exception exception) {
            }
    }

    private void setSelection(int start, int end) {
        setSelectionStart(start);
        setSelectionEnd(end);
    }

    /**
     * Text model to implement autocompletion based on the current contents of author model.
     *
     * Based on <link>http://www.java2s.com/Code/Java/Swing-JFC/AutocompleteTextField.htm</link>
     */
    class AutoListDocument extends DefaultStyledDocument {
        private static final long serialVersionUID = 5411815738746290051L;

        AutoListDocument() {
            setDocumentFilter(new AuthorSetFilter());
        }

        @Override
        public void insertString(int i, String s, AttributeSet attributeSet) throws BadLocationException {
            if (s == null || "".equals(s))
                return;
            System.out.println("Doc insert: i="+i+" s="+s);
            super.insertString(i, s, attributeSet);

            // TODO: only start at most recent edit!
            String current = getText(0, getLength());
            // start is last ", ":
            int start = current.lastIndexOf(", ");
            if (start > -1)
                start += 2;
            else
                start = 0;
            int end = start+s.length();
            System.out.println("start = "+start+", end = "+end);

            String s1 = getText(start, end);
            String s2 = getMatch(s1);
//            int j = (i + s.length()) - 1;
            if (s2 == null) {
                // no match!
                getToolkit().beep();
                //super.insertString(i, s, attributeSet);
                setSelection(start, end);
                return;
            }
            super.remove(start, end);
            super.insertString(start, s2, attributeSet);
            setSelection(end, getLength());
        }

        @Override
        public void remove(int i, int j) throws BadLocationException {
            System.out.println("Doc remove: i="+i+" j="+j);
//            super.remove(i, j);
            int k = getSelectionStart();
            if (k > 0)
                k--;
            String s = getMatch(getText(0, k));
            if (s == null) {
                super.remove(i, j);
            } else {
                super.remove(0, getLength());
                super.insertString(0, s, null);
            }
            try {
                setSelection(k, getLength());
            } catch (Exception exception) {
            }
        }

        @Override
        public void replace(int i, int j, String s, AttributeSet attributeSet) throws BadLocationException {
            System.out.println("Doc replace: i="+i+" j="+j+" s="+s);
            super.remove(i, j);
            insertString(i, s, attributeSet);
        }
    }

    class AuthorSetFilter extends DocumentFilter {
        private boolean editingStarted = false;

        @Override
        public void insertString(FilterBypass fb, int i, String s, AttributeSet attributeSet) throws BadLocationException {
            System.out.println("Filter insert: i="+i+" s="+s);
//            // TODO: only start at most recent edit!
//            String s1 = getText(0, i);
//            String s2 = getMatch(s1 + s);
//            int j = (i + s.length()) - 1;
//            if (s2 == null) {
//                // no match!
//                getToolkit().beep();
//                fb.insertString(i, s, attributeSet);
//                setSelection(0, s.length());
//                return;
//            }
//            fb.remove(0, fb.getDocument().getLength());
//            fb.insertString(0, s2, attributeSet);
//            setSelection(j + 1, fb.getDocument().getLength());
            // calculate new point of insertion:
            int end = fb.getDocument().getLength();
            if (end > 0 && !editingStarted) {
                fb.insertString(end, ", ", null);
                end = fb.getDocument().getLength();
            }
            fb.insertString(end, s, attributeSet);
            editingStarted = true;
        }

        @Override
        public void remove(FilterBypass fb, int i, int j) throws BadLocationException {
            if (j == 0) return;
            System.out.println("Filter remove: i="+i+" j="+j);
//            int k = getSelectionStart();
//            if (k > 0)
//                k--;
//            String s = getMatch(getText(0, k));
//            if (s == null) {
//                fb.remove(i, j);
//            } else {
//                fb.remove(0, fb.getDocument().getLength());
//                fb.insertString(0, s, null);
//            }
//            try {
//                setSelection(k, fb.getDocument().getLength());
//            } catch (Exception exception) {
//            }
            super.remove(fb, i, j); // TODO: implement!
        }

        @Override
        public void replace(FilterBypass fb, int i, int j, String s, AttributeSet attributeSet) throws BadLocationException {
            System.out.println("Filter replace: i="+i+" j="+j+" s="+s);
            this.remove(fb, i, j);
            this.insertString(fb, i, s, attributeSet);
        }
    }
}
