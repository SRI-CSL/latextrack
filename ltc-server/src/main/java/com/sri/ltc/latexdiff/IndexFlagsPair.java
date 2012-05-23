/**
 ************************ 80 columns *******************************************
 * IndexFlagsPair
 *
 * Created on 3/13/12.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Pair of an index (with generic type) and an immutable set of change flags.
 *
 * @author linda
 */
public final class IndexFlagsPair<T> {

    public final T index;
    public final ImmutableSet<Change.Flag> flags;

    public IndexFlagsPair(T index, Set<Change.Flag> flags) {
        if (index == null || flags == null)
            throw new IllegalArgumentException("Cannot instantiate pair with NULL arguments");
        this.index = index;
        this.flags = Sets.immutableEnumSet(flags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexFlagsPair)) return false;

        IndexFlagsPair that = (IndexFlagsPair) o;

        if (!index.equals(that.index)) return false;
        if (!flags.equals(that.flags)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = index.hashCode();
        result = 31 * result + flags.hashCode();
        return result;
    }

    private String escapeIndex() {
        if (index.getClass().equals(String.class)) {
            return escapeText((String) index);
        } else
            return index.toString();
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

    @Override
    public String toString() {
        return "("+ escapeIndex() +","+flags+")";
    }
}
