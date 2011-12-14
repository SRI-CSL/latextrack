/**
 ************************ 80 columns *******************************************
 * AuthorListModel
 *
 * Created on Aug 6, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.editor;

import com.sri.ltc.filter.Author;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author linda
 */
public final class AuthorListModel extends AbstractListModel {

    private static final long serialVersionUID = -1625971771825835266L;
    private final SortedSet<AuthorCell> authors = new TreeSet<AuthorCell>();

    private final LTCSession session;

    public AuthorListModel(LTCSession session) {
        this.session = session;
    }

    public int getSize() {
        synchronized (authors) {
            return authors.size();
        }
    }

    public Object getElementAt(int index) {
        synchronized (authors) {
            return new ArrayList<AuthorCell>(authors).get(index);
        }
    }

    public void clear() {
        synchronized (authors) {
            int size = authors.size();
            authors.clear();
            if (size > 0)
                fireIntervalRemoved(this, 0, size-1);
        }
    }

    public void init(List<Object[]> list) {
        synchronized (authors) {
            clear();            
            if (list != null && !list.isEmpty()) {
                for (Object[] authorAsList : list) {
                    authors.add(new AuthorCell(
                            Author.fromList(authorAsList),
                            Color.decode((String) authorAsList[2])));
                }
                fireIntervalAdded(this, 0, authors.size()-1);
            }
        }
    }

    public void addAuthors(List<Object[]> list) {
        synchronized (authors) {
            if (list != null && !list.isEmpty()) {
                for (Object[] authorAsList : list)
                    authors.add(new AuthorCell(
                            Author.fromList(authorAsList),
                            Color.decode((String) authorAsList[2])));
                fireContentsChanged(this, 0, getSize() - 1);
            }
        }
    }

    private int getIndex(AuthorCell authorCell) {
        return new ArrayList<AuthorCell>(authors).indexOf(authorCell);
    }

    public Color getColorForAuthor(Author author) {
        synchronized (authors) {
            int index =  getIndex(new AuthorCell(author, null));
            if (index == -1)
                return Color.black; // given author is not yet known
            return ((AuthorCell) getElementAt(index)).getColor();
        }
    }

    public void fireChanged(AuthorCell authorCell) {
        synchronized (authors) {
            int index =  getIndex(authorCell);
            if (index != -1)
                fireContentsChanged(this, index, index);
        }
    }

    private void fireAllChanged() {
        int size = getSize();
        if (size > 0)
            fireContentsChanged(this, 0, size - 1);
    }

    public void resetAll() {
        synchronized (authors) {
            for (AuthorCell authorCell : authors)
                authorCell.limited = true;
            fireAllChanged();
        }
        session.setLimitedAuthors(true, null);
    }

    public void setLimited(int[] indices, boolean limited) {
        List<String[]> limitedAuthors = new ArrayList<String[]>(); // collect authors that are limited
        boolean allLimited = true; // track whether at least one author not limited
        synchronized (authors) {
            List<AuthorCell> authorsAsList = new ArrayList<AuthorCell>(authors);
            Set<Integer> indicesAsSet = new HashSet<Integer>();
            for (int index : indices)
                indicesAsSet.add(index);
            for (int i = 0; i < authorsAsList.size(); i++) {
                boolean status = indicesAsSet.contains(i)?limited:!limited;
                AuthorCell authorCell = authorsAsList.get(i);
                authorCell.limited = status;
                if (status)
                    limitedAuthors.add(new String[] {authorCell.author.name, authorCell.author.email});
                else
                    allLimited = false;
            }
            fireAllChanged();
        }
        session.setLimitedAuthors(allLimited, limitedAuthors);
    }
}
