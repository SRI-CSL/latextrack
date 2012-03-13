/**
 ************************ 80 columns *******************************************
 * Change
 *
 * Created on Jan 6, 2010.
 *
 * Copyright 2009-2010, SRI International.
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
        DELETION,
        SMALL,
        PREAMBLE,
        COMMENT,
        COMMAND;
    };
    public final static Set<Flag> buildFlags(
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

    static String escapeText(String text) {
        // escape "<", ">", "&", ', and "
        text = text.replaceAll("<","&lt;");
        text = text.replaceAll(">","&gt;");
        text = text.replaceAll("&","&amp;");
        text = text.replaceAll("'","&apos;");
        text = text.replaceAll("\"","&quot;");
        return text;
    }
}
