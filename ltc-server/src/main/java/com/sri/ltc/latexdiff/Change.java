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
package com.sri.ltc.latexdiff;

import com.google.common.collect.*;

import java.util.*;

/**
 * Super class for addition and deletion changes when comparing two LaTeX texts.
 *
 * @author linda
 */
public abstract class Change<T> implements Comparable<Change> {

    public final int start_position;
    public final ImmutableList<IndexFlagsPair<T>> flags; // a list of flags by position/string fragments
    private final Integer sequenceNumber;

    public enum Flag {
        DELETION('D'),
        SMALL('S'),
        PREAMBLE('P'),
        COMMENT('%'),
        COMMAND('C');
        private final char indicator;
        private Flag(char indicator) {
            this.indicator = indicator;
        }
        public char getIndicator() {
            return indicator;
        }
    };
    public final static Set<Flag> buildFlagsToHide(
            boolean showDeletions,
            boolean showSmallChanges,
            boolean showPreambleChanges,
            boolean showCommentChanges,
            boolean showCommandChanges) {
        EnumSet<Flag> result = EnumSet.noneOf(Flag.class);
        if (!showDeletions) result.add(Flag.DELETION);
        if (!showSmallChanges) result.add(Flag.SMALL);
        if (!showPreambleChanges) result.add(Flag.PREAMBLE);
        if (!showCommentChanges) result.add(Flag.COMMENT);
        if (!showCommandChanges) result.add(Flag.COMMAND);
        return result;
    }

    // sequence numbering for any Change object created
    private static Integer sequence = 0;
    public static void resetSequenceNumbering() {
        synchronized (sequence) {
            sequence = 0;
        }
    }

    protected Change(int start_position, List<IndexFlagsPair<T>> flags) {
        if (start_position < 0) throw new IllegalArgumentException("Start position of change cannot be negative");
        this.start_position = start_position;
        // create immutable list of flags by fragments
        if (flags == null) throw new IllegalArgumentException("Flags of change cannot be NULL");
        this.flags = new ImmutableList.Builder<IndexFlagsPair<T>>()
                .addAll(flags)
                .build();
        // set and update sequence number:
        synchronized (sequence) {
            sequenceNumber = sequence++;
        }
    }

    public int compareTo(Change o) {
        int result = start_position - o.start_position;
        if (result == 0) // if start position is the same, use class information: Addition smaller than Deletion
            result = this.getClass().getName().compareTo(o.getClass().getName());
        if (result == 0) // if class information is the same, compare using creation time
            result = sequenceNumber.compareTo(o.sequenceNumber);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Change)) return false;

        Change change = (Change) o;

        if (start_position != change.start_position) return false;
        if (!flags.equals(change.flags)) return false;
        if (!sequenceNumber.equals(change.sequenceNumber)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = start_position;
        result = 31 * result + flags.hashCode();
        result = 31 * result + sequenceNumber.hashCode();
        return result;
    }

    String toXMLContents() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("  <start position="+start_position);
        buffer.append(" />\n");
        buffer.append("  <flags>\n");
        buffer.append("  "+flags.toString());
        buffer.append("\n  </flags>\n");
        return buffer.toString();
    }
}
