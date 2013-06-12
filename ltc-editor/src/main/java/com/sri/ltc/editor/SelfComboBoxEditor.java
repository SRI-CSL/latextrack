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

import com.sri.ltc.filter.Author;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.awt.datatransfer.DataFlavor.javaJVMLocalObjectMimeType;

/**
 * NOTE: problem with 2 characters being deleted on newer Mac OS X:
 *       http://lists.apple.com/archives/java-dev/2010/May/msg00092.html
 *
 * @author linda
 */
public final class SelfComboBoxEditor implements ComboBoxEditor, ListDataListener {

    static final Logger LOGGER = Logger.getLogger(SelfComboBoxEditor.class.getName());
    private static DataFlavor AUTHOR_FLAVOR;
    static {
        try {
            AUTHOR_FLAVOR = new DataFlavor(javaJVMLocalObjectMimeType + ";class=com.sri.ltc.filter.Author");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    private final static String TEXT_HINT = "name [<email>]";
    private final JFormattedTextField delegate = new JFormattedTextField(new AuthorFormatter()) {
        @Override
        public Object getValue() {
            Object value = super.getValue();
            // update color for text field and text pane:
            Color color = Color.black;
            if (value instanceof Author)
                color = authorModel.getColorForAuthor((Author) value);
            setForeground(color);
            if (latexPane != null)
                latexPane.getDocumentFilter().setColor(color);
            return value;
        }
    };
    {
        delegate.setFocusLostBehavior(JFormattedTextField.REVERT);
        delegate.setTransferHandler(new AuthorTransferHandler()); // customized drag 'n drop
        delegate.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                delegate.setCaretPosition(0); // start at beginning
                delegate.setText(""); // to make the prior statement work
                delegate.setForeground(Color.gray); // display temporary text in gray
                delegate.setText(TEXT_HINT);
                Document document = delegate.getDocument();
                if (document instanceof AbstractDocument)
                    ((AbstractDocument) document).setDocumentFilter(new FocusGainedFilter());
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                Document document = delegate.getDocument();
                if (document instanceof AbstractDocument)
                    ((AbstractDocument) document).setDocumentFilter(null);
            }
        });
    }
    private final AuthorListModel authorModel;
    private final LatexPane latexPane;

    public SelfComboBoxEditor(AuthorListModel authorModel, LatexPane latexPane) {
        this.latexPane = latexPane;
        this.authorModel = authorModel;
        authorModel.addListDataListener(this);
    }

    public Component getEditorComponent() {
        return delegate;
    }

    public void setItem(Object anObject) {
        delegate.setValue(anObject);
    }

    public Object getItem() {
        return delegate.getValue();
    }

    public void selectAll() {
        delegate.selectAll();
    }

    public void addActionListener(ActionListener l) {
        delegate.addActionListener(l);
    }

    public void removeActionListener(ActionListener l) {
        delegate.removeActionListener(l);
    }

    private void triggerForegroundUpdate() {
        delegate.getValue(); // this will trigger the color updates
    }

    public void intervalAdded(ListDataEvent e) {
        triggerForegroundUpdate();
    }

    public void intervalRemoved(ListDataEvent e) {
        triggerForegroundUpdate();
    }

    public void contentsChanged(ListDataEvent e) {
        triggerForegroundUpdate();
    }

    private class AuthorFormatter extends JFormattedTextField.AbstractFormatter {
        private static final long serialVersionUID = 2916130979423057587L;

        @Override
        public Object stringToValue(String text) throws ParseException {
            return Author.parse(text);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value == null)
                return "";
            if (value instanceof Author)
                return ((Author) value).toString();
            throw new ParseException("Value is not of class Author", 0);
        }
    }

    private class AuthorTransferHandler extends TransferHandler {

        private static final long serialVersionUID = 9174921893409876838L;

        @Override
        public boolean canImport(TransferSupport support) {
            if (support.isDataFlavorSupported(AUTHOR_FLAVOR))
                return true;
            return false;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support))
                return false;

            // Fetch the Transferable and its data
            try {
                Author data = (Author) support.getTransferable().getTransferData(AUTHOR_FLAVOR);
                // insert data
                delegate.setValue(data);
                // signal success
                return true;
            } catch (UnsupportedFlavorException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
            return false;
        }
    }

    private class FocusGainedFilter extends DocumentFilter {
        private boolean startEditing = true;

        private synchronized int startEditing(FilterBypass fb, int offset) {
            if (startEditing) {
                delegate.setForeground(Color.black); // start edit in black
                try {
                    fb.remove(0, fb.getDocument().getLength()); // erase any text hint
                } catch (BadLocationException e) {
                    LOGGER.log(Level.SEVERE, "while start editing: "+e.getMessage(), e);
                }
                startEditing = false;
                return 0; // reset prior offset
            }
            return offset;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attributeSet) throws BadLocationException {
            if ("".equals(text)) return;

            offset = startEditing(fb, offset);
            fb.insertString(offset, text, attributeSet);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            if (length == 0) return;

            offset = startEditing(fb, offset);
            fb.remove(offset, length);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attributeSet) throws BadLocationException {
            this.remove(fb, offset, length);
            this.insertString(fb, offset, text, attributeSet);
        }
    }
}
