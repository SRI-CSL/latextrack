/**
 ************************ 80 columns *******************************************
 * AuthorCell
 *
 * Created on Aug 6, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.viewer;

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
