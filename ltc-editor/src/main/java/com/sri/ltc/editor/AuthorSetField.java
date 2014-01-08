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

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A text field that displays a (sorted) set of selected authors.
 *
 * It allows drag-n-drop and copy-n-paste of both, author and string objects.
 *
 * If editing by hand or dropping a string in there, it auto-completes the name
 * based on the contents of the set of currently known authors.
 *
 * @author linda
 */
public final class AuthorSetField extends JTextField {

    static final Logger LOGGER = Logger.getLogger(AuthorSetField.class.getName());
    private static final String COMPLETE_ACTION = "complete";

    private final AuthorListModel authorModel;
    private AuthorPanel authorPanel = null;

    public AuthorSetField(AuthorListModel authorModel) {
        this.authorModel = authorModel;

        final AutoDocument autoDocument = new AutoDocument();
        setDocument(autoDocument);

        // TAB key to "commit" autocomplete
        setFocusTraversalKeysEnabled(false); // Without this, cursor always leaves text field
        // Maps the tab key to the action, which cycles through possible completions
        getInputMap().put(KeyStroke.getKeyStroke("TAB"), COMPLETE_ACTION);
        getActionMap().put(COMPLETE_ACTION, new AbstractAction() {
            private static final long serialVersionUID = -6920396834222894988L;

            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    if (!autoDocument.nextCompletion(getCaretPosition()))
                        AuthorSetField.this.replaceSelection("");
                } catch (BadLocationException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        });
    }

    public void installAuthorPanel(AuthorPanel authorPanel) {
        this.authorPanel = authorPanel;
    }

    private class AutoDocument extends PlainDocument {
        private static final long serialVersionUID = 7317665916116652860L;

        private Iterator<String> choiceIterator = Iterators.cycle(); // iterate over matching strings

        @Override
        public void replace(int offset, int length, String s, AttributeSet a) throws BadLocationException {
            super.remove(offset, length);
            insertString(offset, s, a);
        }

        @Override
        public void insertString(int offset, String s, AttributeSet a) throws BadLocationException {
            if (s == null || "".equals(s))
                return;
            String content = getText(0, offset) + s;
            if (findMatches(content.toLowerCase()))
                nextCompletion(offset + s.length());
            else
                super.insertString(offset, s, a);
        }

        private boolean findMatches(String prefix) {
            SortedSet<String> matches = Sets.newTreeSet(String.CASE_INSENSITIVE_ORDER);

            for (int i = 0; i < authorModel.getSize(); i++) {
                String s1 = ((AuthorCell) authorModel.getElementAt(i)).author.toString();
                if (s1 != null && s1.toLowerCase().startsWith(prefix))
                    matches.add(s1);
            }

            List<String> choices = Lists.newArrayList( // translate sorted matches without already selected into list
                    Sets.difference(matches,
                            authorPanel == null ? Sets.newHashSet() : authorPanel.dataAsStrings()));

            synchronized (choiceIterator) {
                choiceIterator = Iterators.cycle(choices);
            }

            return choiceIterator.hasNext();
        }

        boolean nextCompletion(int position) throws BadLocationException {
            synchronized (choiceIterator) {
                if (choiceIterator.hasNext()) {
                    String completion = choiceIterator.next();
                    super.remove(0, getLength());
                    super.insertString(0, completion, null);
                    setSelectionStart(position);
                    setSelectionEnd(getLength());
                    return true;
                }
            }
            return false;
        }
    }
}
