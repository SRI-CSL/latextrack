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
 *
 * @author linda
 */
public class IndexPair {

    public final Integer left, right;

    public IndexPair(Integer left, Integer right) {
        if (left == null || right == null)
            throw new IllegalArgumentException("Cannot create an index pair using NULL.");
        if (right < left)
            throw new IllegalArgumentException("Cannot create an index pair with right side < left side");
        this.left = left;
        this.right = right;
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