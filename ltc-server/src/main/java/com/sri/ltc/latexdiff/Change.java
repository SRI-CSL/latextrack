/**
 ************************ 80 columns *******************************************
 * Change
 *
 * Created on Jan 6, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * Super class for all kind of changes when comparing two LaTeX texts.
 * <p>
 * This implementation of comparable is not consistent with {@link #equals(Object)}
 * of subclasses that have more fields.  It does use, however, an internal
 * sequence number to order entries of the same (sub-)class starting at the same
 * position.
 * 
 * @author linda
 */
public abstract class Change implements Comparable<Change> {

    public final int start_position;
    public final ImmutableSet<Flag> flags;
    private final Integer sequenceNumber;

    public enum Flag {
        DELETION,
        SMALL,
        PREAMBLE,
        COMMENT,
        COMMAND;
    };

    private final static Map<Class,Integer> ORDER = new HashMap<Class,Integer>(6);
    static {
        // If normal and small have the same position,
        // always take the normal ones first (as they must have come from the same, original hunk)
        // When comparing Addition with Deletion at the same position,
        // then use Addition first
        ORDER.put(Addition.class, 1);
        ORDER.put(Deletion.class, 2);
        ORDER.put(SmallAddition.class, 3);
        ORDER.put(SmallDeletion.class, 4);
    }

    // sequence numbering for any Change object created
    private static Integer sequence = 0;
    public static void resetSequenceNumbering() {
        synchronized (sequence) {
            sequence = 0;
        }
    }
    
    protected Change(int start_position, EnumSet<Flag> flags) {
        if (start_position < 0) throw new IllegalArgumentException("Start position of change cannot be negative");
        this.start_position = start_position;
        // compute immutable set of flags:
        if (this instanceof SmallAddition || this instanceof SmallDeletion) flags.add(Flag.SMALL);
        if (this instanceof Deletion) flags.add(Flag.DELETION);
        this.flags = Sets.immutableEnumSet(flags);
        // set and update sequence number:
        synchronized (sequence) {
            sequenceNumber = sequence++;
        }
    }

    public int compareTo(Change o) {
        int result = start_position - o.start_position;
        if (result == 0) // if start position is the same, use class information
            result = ORDER.get(getClass()) - ORDER.get(o.getClass());
        if (result == 0) // if class information is the same, compare using creation time
            result = sequenceNumber.compareTo(o.sequenceNumber);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Change change = (Change) o;

        if (start_position != change.start_position) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return start_position;
    }

    String toXMLContents() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("  <start position="+start_position);
        buffer.append(" />\n");
        buffer.append("  <flags>");
        buffer.append(flags.toString());
        buffer.append("</flags>\n");
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
