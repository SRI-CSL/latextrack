/**
 ************************ 80 columns *******************************************
 * CommitTableRow
 *
 * Created on Aug 9, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.editor;

import com.sri.ltc.filter.Author;
import com.sri.ltc.git.Commit;
import com.sri.ltc.server.LTCserverInterface;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author linda
 */
public final class CommitTableRow {

    protected final static String[] COLUMN_NAMES = {"","SHA-1","Date","Author","Message"};
    protected final static Object[] REF_VALUES = {
            new CommitGraphRow(null),
            LTCserverInterface.ON_DISK+"MM",
            new Date(),
            new Author("Git Author XXXXX", null, null),
            "msgM"
    }; // reference values to determine initial column widths

    CommitGraphRow graph = new CommitGraphRow(this);
    final String sha1;
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
        this.sha1 = array[0].toString();
        this.message = array[1].toString(); // TODO: limit to first newline (if any)
        this.date = Commit.FORMATTER.parse(array[4].toString());
        this.author = new Author(array[2].toString(), array[3].toString(), null);
    }

    public CommitTableRow(String sha1) {
        this.sha1 = sha1;
        this.date = null;
        this.author = null;
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
                return sha1;
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

        if (sha1 != null ? !sha1.equals(that.sha1) : that.sha1 != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return sha1 != null ? sha1.hashCode() : 0;
    }
}
