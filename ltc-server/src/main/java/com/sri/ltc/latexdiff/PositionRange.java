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
 * Left-closed and right-open interval of positions.
 * An interval starting at -infinity is created using NULL for the left side.
 * An interval ending at +infinity is created using NULL for the right side.
 *
 * @author linda
 */
public class PositionRange implements Comparable<PositionRange> {

    public final Integer left, right;

    public PositionRange(Integer left, Integer right) {
        if (right != null && left != null && right <= left)
            throw new IllegalArgumentException("Cannot create PositionRange with right side <= left side");
        this.left = (left==null?Integer.MIN_VALUE:left);
        this.right = (right==null?Integer.MAX_VALUE:right);
    }

    /**
     * Check whether a given position falls into this range.  That is,
     * the position is greater or equal than the left side of the interval
     * and less than the right side.
     *
     * @param position position to be tested against this range
     * @return false, if given position is <code>null</code> or it doesn't
     * fall into range, otherwise true
     */
    public boolean inRange(Integer position) {
        if (position == null)
            return false;
        return position >= left && position < right;
    }

    public int compareTo(PositionRange positionRange) {
        // consider ordering of left side first, then right side
        int left_comparison = left.compareTo(positionRange.left);
        if (left_comparison == 0)
            return right.compareTo(positionRange.right);
        else
            return left_comparison;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PositionRange range = (PositionRange) o;

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

    private final static String format(Integer i) {
        if (i == Integer.MIN_VALUE)
            return "-Inf";
        if (i == Integer.MAX_VALUE)
            return "+Inf";
        return ""+i;
    }

    @Override
    public String toString() {
        return "["+format(left)+" to "+format(right)+"[";
    }
}