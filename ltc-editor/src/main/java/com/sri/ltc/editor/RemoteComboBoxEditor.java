/**
 ************************ 80 columns *******************************************
 * RemoteComboBoxEditor
 *
 * Created on Oct 27, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.editor;

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
