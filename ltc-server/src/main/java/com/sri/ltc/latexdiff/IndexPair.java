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
 * Pair of indices along with a boolean.
 * Both indices must be non-negative.  The left one must be equal or smaller than the right one.
 * <p>
 * The natural ordering of a pair is first determined by the left index.  If the left indices are equal,
 * the right index determines the order.  If both indices are equal, then the boolean decides the order.
 * Thus, the ordering is consistent with <code>equals</code>.
 *
 * @author linda
 */
public final class IndexPair implements Comparable<IndexPair> {

    public final Integer left, right;
    public final Boolean addRearSpace;
    public final ImmutableSet<Change.Flag> flags;

    /**
     *
     * @param left
     * @param right
     * @param addRearSpace
     * @param flags can be NULL!
     */
    public IndexPair(Integer left, Integer right, Boolean addRearSpace, Set<Change.Flag> flags) {
        if (left == null || right == null || addRearSpace == null)
            throw new IllegalArgumentException("Cannot create an index pair using NULL.");
        if (left < 0 || right < 0)
            throw new IllegalArgumentException("Cannot create an index pair with negative numbers.");
        if (right < left)
            throw new IllegalArgumentException("Cannot create an index pair with right side < left side");
        this.left = left;
        this.right = right;
        this.addRearSpace = addRearSpace;
        this.flags = (flags==null?null:Sets.immutableEnumSet(flags));
    }

    @Override
    public int compareTo(IndexPair o) {
        int order = left.compareTo(o.left);
        if (order != 0) return order;
        order = right.compareTo(o.right);
        if (order != 0) return order;
        return addRearSpace.compareTo(o.addRearSpace);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IndexPair indexPair = (IndexPair) o;

        if (!addRearSpace.equals(indexPair.addRearSpace)) return false;
        if (!left.equals(indexPair.left)) return false;
        if (!right.equals(indexPair.right)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = left.hashCode();
        result = 31 * result + right.hashCode();
        result = 31 * result + addRearSpace.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "("+left+","+right+(addRearSpace?" ":"")+")";
    }
}