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

import com.sri.ltc.CommonUtils;
import com.sri.ltc.filter.Author;
import com.sri.ltc.server.LTCserverInterface;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author linda
 */
public final class CommitTableRow {

    protected final static String[] COLUMN_NAMES = {"","Revision","Date","Author","Message"};
    protected final static Object[] REF_VALUES = {
            new CommitGraphRow(null),
            LTCserverInterface.ON_DISK+"MM",
            new Date(),
            new Author("Git Author XXXXX", null),
            "msgM"
    }; // reference values to determine initial column widths

    CommitGraphRow graph = new CommitGraphRow(this);
    final String ID; // should be non-null
    final Date date;
    final Author author;
    final String message;
    final List<CommitTableRow> parents = new ArrayList<CommitTableRow>();
    final List<CommitTableRow> children = new ArrayList<CommitTableRow>();
    private boolean isActive = true;

    public CommitTableRow(Object[] array) throws ParseException {
        if (array == null)
            throw new IllegalArgumentException("Cannot create commit table row from NULL array");
        if (array.length < 6)
            throw new IllegalArgumentException("Cannot create commit table row from less than 6 objects in array");
        this.ID = array[0].toString();
        // limit to first full stop, question or exclamation mark followed by space ("\.\s+") or newline (if any):
        String[] messages = array[1].toString().split("(\\.\\?\\!\\s+)|[\n\r]+",2);
        this.message = messages[0];
        this.date = CommonUtils.deSerializeDate(array[4].toString());
        this.author = new Author(array[2].toString(), array[3].toString());
    }

    public CommitTableRow(String ID, Author author) {
        this.ID = ID==null?"":ID;
        this.date = null;
        this.author = author;
        this.message = "";
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Object getColumn(int column) {
        switch (column) {
            case 0:
                return graph;
            case 1:
                return ID;
            case 2:
                return date;
            case 3:
                return author;
            case 4:
                return message;
            default:
                return null;
        }
    }

    static public Class getColumnClass(int column) {
        switch (column) {
            case 0:
                return CommitGraphRow.class;
            case 1:
            case 4:
                return String.class;
            case 2:
                return Date.class;
            case 3:
                return Author.class;
            default:
                return Object.class;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommitTableRow that = (CommitTableRow) o;

        if (!ID.equals(that.ID)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }
}
