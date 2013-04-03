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
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
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
    private final JFormattedTextField delegate = new JFormattedTextField(new AuthorFormatter());
    {
        delegate.setFocusLostBehavior(JFormattedTextField.COMMIT);
        delegate.setTransferHandler(new AuthorTransferHandler()); // customized drag 'n drop
    }
    private final AuthorListModel authorModel;

    public SelfComboBoxEditor(AuthorListModel authorModel) {
        this.authorModel = authorModel;
        authorModel.addListDataListener(this);
    }

    public Component getEditorComponent() {
        return delegate;
    }

    public void setItem(Object anObject) {
        if (anObject instanceof Author) 
            delegate.setForeground(authorModel.getColorForAuthor((Author) anObject));
        else
            delegate.setForeground(Color.black);
        delegate.setValue(anObject);
    }

    public Object getItem() {
        return delegate.getValue();
    }

    public void selectAll() {
        delegate.setForeground(Color.black); // start editing
        // don't select all text to prevent deleting everything upon hitting ENTER
    }

    public void addActionListener(ActionListener l) {
        delegate.addActionListener(l);
    }

    public void removeActionListener(ActionListener l) {
        delegate.removeActionListener(l);
    }

    private void triggerForegroundUpdate() {
        setItem(getItem());
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
}
