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
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author linda
 */
public final class SelfComboBoxModel extends AbstractListModel implements ComboBoxModel {

    private static final long serialVersionUID = 2481826979689433485L;
    private final SortedSet<Author> authors = new TreeSet<Author>();
    private Author self = null;

    private final LatexPane latexPane;
    private final AuthorListModel authorModel;
    private final LTCSession session;

    private boolean updateLTC = true; // false to signal when NOT to update self

    public SelfComboBoxModel(LatexPane latexPane, AuthorListModel authorModel, LTCSession session) {
        this.latexPane = latexPane;
        this.authorModel = authorModel;
        this.session = session;
    }

    public void setSelectedItem(Object anItem) {
        int newIndex = -1;
        Author priorSelf = self;
        if (anItem instanceof Author) {
            self = (Author) anItem;
            synchronized (authors) {
                authors.add(self);
                newIndex = new ArrayList<Author>(authors).indexOf(self);
            }
        } else
            self = null;
        if ((priorSelf == null && self != null) || (priorSelf != null && !priorSelf.equals(self))) {
            // selected item has changed, so fire update
            if (newIndex != -1)
                fireContentsChanged(this, newIndex, newIndex);
            else
                fireContentsChanged(this, 0, 0);
            // update LTC accordingly (if not flagged to skip)
            if (updateLTC && session.isValid()) {
                session.setSelf(self);
                session.getAuthors();
            }
            updateLTC = true; // reset flag
            // tell document filter about it
            if (self == null)
                latexPane.getDocumentFilter().setColor(Color.black);
            else
                latexPane.getDocumentFilter().setColor(authorModel.getColorForAuthor(self));
        }
    }

    public Object getSelectedItem() {
        return self;
    }

    public int getSize() {
        return authors.size();
    }

    public Object getElementAt(int index) {
        return new ArrayList<Author>(authors).get(index);
    }

    // TODO: change to List<Object[]> authors...
    public void init(List<Object[]> list, Object[] self) {
        synchronized (authors) {
            clear();            
            if (list != null && !list.isEmpty()) {
                for (Object[] authorAsList : list) {
                    authors.add(Author.fromList(authorAsList));
                }
                fireIntervalAdded(this, 0, authors.size()-1);
            }
            if (self != null && self.length > 0) {
                Author author = Author.fromList(self);
                authors.add(author);
                updateLTC = false; // as self is coming from init, we don't need to update LTC
                setSelectedItem(author);
            }
        }
    }

    public void clear() {
        synchronized (authors) {
            int size = authors.size();
            authors.clear();
            if (size > 0)
                fireIntervalRemoved(this, 0, size-1);
        }
        setSelectedItem(null);
    }
}
