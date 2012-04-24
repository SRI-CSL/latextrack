/**
 ************************ 80 columns *******************************************
 * LocationRange
 *
 * Created on May 3, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

/**
 * Pair of indices.
 * Both indices must be non-negative.  The left one must be equal or smaller than the right one.
 * <p>
 * The natural ordering of a pair is first determined by the left index.  If the left indices are equal,
 * the right index determines the order.  Thus, the ordering is consistent with <code>equals</code>.
 *
 * @author linda
 */
public final class IndexPair implements Comparable<IndexPair> {

    public final Integer left, right;

    public IndexPair(Integer left, Integer right) {
        if (left == null || right == null)
            throw new IllegalArgumentException("Cannot create an index pair using NULL.");
        if (left < 0 || right < 0)
            throw new IllegalArgumentException("Cannot create an index pair with negative numbers.");
        if (right < left)
            throw new IllegalArgumentException("Cannot create an index pair with right side < left side");
        this.left = left;
        this.right = right;
    }

    @Override
    public int compareTo(IndexPair o) {
        int leftOrder = left.compareTo(o.left);
        return (leftOrder == 0 ? right.compareTo(o.right) : leftOrder);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IndexPair range = (IndexPair) o;

        if (!left.equals(range.left)) return false;
        if (!right.equals(range.right)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = left.hashCode();
        result = 31 * result + right.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "("+left+","+right+")";
    }
}