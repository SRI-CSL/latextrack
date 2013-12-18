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

/**
 * Utility to communicate current selection status between the text pane and undo actions.
 *
 * @author linda
 */
public final class DotMark {

    private int dot;
    private int mark;

    public void set(int dot, int mark) {
        this.dot = dot;
        this.mark = mark;
    }

    public int getDot() {
        return dot;
    }

    public int getMark() {
        return mark;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DotMark dotMark = (DotMark) o;

        if (dot != dotMark.dot) return false;
        if (mark != dotMark.mark) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dot;
        result = 31 * result + mark;
        return result;
    }
}
