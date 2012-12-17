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
