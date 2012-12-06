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

import java.awt.*;

/**
 * @author linda
 */
public final class AuthorCell implements Comparable<AuthorCell> {

    protected final String label;
    protected final Author author;
    protected boolean limited = true;
    private Color color = Color.blue;

    public AuthorCell(Author author, Color color) {
        if (author == null)
            throw new IllegalArgumentException("Cannot create author cell with NULL author");
        this.author = author;
        this.label = author.gitRepresentation();
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public boolean setColor(Color color) {
        if (color == null)
            return false;
        boolean changed = !color.equals(this.color);
        this.color = color;
        return changed;
    }

    public int compareTo(AuthorCell o) {
        return label.compareTo(((AuthorCell) o).label);
    }

    @Override
    public String toString() {
        return label+" -> "+color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthorCell that = (AuthorCell) o;

        if (!author.equals(that.author)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return author.hashCode();
    }
}
