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

import com.sri.ltc.versioncontrol.Remote;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.ParseException;

/**
 * @author linda
 */
public final class RemoteComboBoxEditor implements ComboBoxEditor {

    private final JFormattedTextField delegate = new JFormattedTextField(new RemoteFormatter());
    {
        delegate.setFocusLostBehavior(JFormattedTextField.COMMIT);
    }

    @Override
    public Component getEditorComponent() {
        return delegate;
    }

    @Override
    public void setItem(Object anObject) {
        delegate.setValue(anObject);
        delegate.setCaretPosition(0);
    }

    @Override
    public Object getItem() {
        return delegate.getValue();
    }

    @Override
    public void selectAll() {
        // ignore
    }

    @Override
    public void addActionListener(ActionListener l) {
        delegate.addActionListener(l);
    }

    @Override
    public void removeActionListener(ActionListener l) {
        delegate.removeActionListener(l);
    }

    private class RemoteFormatter extends JFormattedTextField.AbstractFormatter {
        private static final long serialVersionUID = -5645152278945041701L;

        @Override
        public Object stringToValue(String text) throws ParseException {
            return Remote.parse(text);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value == null)
                return "";
            if (value instanceof Remote)
                return ((Remote) value).toString();
            throw new ParseException("Value is not of class Remote", 0);
        }
    }

}
