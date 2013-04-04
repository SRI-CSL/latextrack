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

import com.sri.ltc.filter.Author;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;

/**
 * A non-editable text field for displaying the "self" author.
 * This text field registers with the author model to receive updates when the user changes the model (e.g., to change
 * a color), upon which the text field redraws itself.
 *
 * @author linda
 */
public final class SelfTextField extends JTextField implements ListDataListener {

    private final AuthorListModel authorModel;
    private Author self = null;

    public SelfTextField(AuthorListModel authorModel) {
        this.authorModel = authorModel;
        authorModel.addListDataListener(this);
        setEditable(false);
    }

    public void setSelf(Object[] self) {
        if (self != null && self.length > 0)
            this.self = Author.fromList(self);
        else
            this.self = null;
        triggerUpdate();
    }

    @Override
    public void contentsChanged(ListDataEvent listDataEvent) {
        triggerUpdate();
    }

    @Override
    public void intervalAdded(ListDataEvent listDataEvent) {
        triggerUpdate();
    }

    @Override
    public void intervalRemoved(ListDataEvent listDataEvent) {
        triggerUpdate();
    }

    private void triggerUpdate() {
        if (self == null) {
            super.setText("");
            setForeground(Color.black);
        } else {
            super.setText(self.toString());
            setForeground(authorModel.getColorForAuthor(self));
        }
        revalidate();
    }

    @Override
    public void setText(String s) {
        // ignore; only manipulate text via setSelf
    }
}
