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
import com.sri.ltc.filter.Author;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.event.ActionEvent;
import java.util.*;

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

    private static final String COMMIT_ACTION = "commit";
    private static enum Mode {
        INSERT,
        COMPLETION
    };

    private final AuthorListModel authorModel;
    private final SortedSet<Author>
            currentAuthors = Sets.newTreeSet(),
            possibleAuthors = Sets.newTreeSet();

    public AuthorSetField(AuthorListModel authorModel) {
        this.authorModel = authorModel;

        // TODO: remove after testing:
        List<String> keywords = Lists.newArrayList(
                "Roger Sherman <sherman@usa.gov>",
                "rogeR BLA",
                "autocomplete",
                "stackabuse",
                "java", "Jauvp");
        Autocomplete autoComplete = new Autocomplete(keywords);
        getDocument().addDocumentListener(autoComplete);

        // TAB key to "commit" autocomplete
        setFocusTraversalKeysEnabled(false); // Without this, cursor always leaves text field
        // Maps the tab key to the commit action, which finishes the autocomplete when given a suggestion
        getInputMap().put(KeyStroke.getKeyStroke("TAB"), COMMIT_ACTION);
        getActionMap().put(COMMIT_ACTION, autoComplete.new CompleteAction());
    }

    private class Autocomplete implements DocumentListener {

        private final List<String> keywords;
        private Mode mode = Mode.INSERT;
        private List<String> matches = new ArrayList<String>();
        private Iterator<String> matchIterator = Iterators.cycle(); // into matching strings
        private int position = -1;

        public Autocomplete(List<String> keywords) {
            this.keywords = keywords;
            Collections.sort(keywords);
        }

        @Override
        public void changedUpdate(DocumentEvent ev) { }

        @Override
        public void removeUpdate(DocumentEvent ev) { }

        @Override
        public void insertUpdate(DocumentEvent ev) {
            if (ev.getLength() != 1)
                return;

            int pos = ev.getOffset();
            try {
                String content = AuthorSetField.this.getText(0, pos + 1);
                if (findMatches(content.toLowerCase()))
                    // We cannot modify Document from within notification,
                    // so we submit a task that does the change later
                    SwingUtilities.invokeLater(new CompletionTask(pos + 1));
                else
                    // Nothing found
                    mode = Mode.INSERT;
            } catch (BadLocationException e) {
                return; // ignore this exception
            }
        }

        private boolean findMatches(String prefix) {
            matches.clear();

            // TODO: remove after testing:
            for (Iterator<String> i = keywords.iterator(); i.hasNext(); ) {
                String s = i.next();
                if (s.toLowerCase().startsWith(prefix))
                    matches.add(s);
            }

//            for (int i = 0; i < authorModel.getSize(); i++) {
//                String s1 = ((AuthorCell) authorModel.getElementAt(i)).label;
//                if (s1 != null && s1.toLowerCase().startsWith(prefix)) // TODO: test that not already selected?
//                    matches.add(s1);
//            }

            synchronized (matchIterator) {
                matchIterator = Iterators.cycle(matches);
            }

            return matches.size() > 0;
        }

        private class CompleteAction extends AbstractAction {
            private static final long serialVersionUID = -7874851540618105429L;

            @Override
            public void actionPerformed(ActionEvent ev) {
                if (mode == Mode.COMPLETION)
                    nextCompletion();
                else
                    AuthorSetField.this.replaceSelection("");
            }
        }

        private class CompletionTask implements Runnable {
            CompletionTask(int position) {
                Autocomplete.this.position = position;
            }

            public void run() {
                nextCompletion();
            }
        }

        private void nextCompletion() {
            synchronized (matchIterator) {
                if (matchIterator.hasNext()) {
                    String completion = matchIterator.next();
                    AuthorSetField.this.setText(completion);
                    AuthorSetField.this.setCaretPosition(completion.length());
                    AuthorSetField.this.moveCaretPosition(position);
                    mode = Mode.COMPLETION;
                } else
                    // TODO: delete selection?
                    mode = Mode.INSERT;
            }
        }
    }
}
