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

import com.bmsi.gnudiff.Diff;

/**
 * Pair of indices each with length.
 * Both indices and both lengths must be non-negative.
 * Can be created from non-NULL hunk.
 * Also able to create a hunk from this class.
 *
 * @author linda
 */
public final class IndexLengthPair {

    public final Integer index0, index1;
    public final Integer length0, length1;

    public IndexLengthPair(Integer index0, Integer index1, Integer length0, Integer length1) {
        if (index0 == null || index1 == null || length0 == null || length1 == null)
            throw new IllegalArgumentException("Cannot create an index-length pair using NULL.");
        if (index0 < 0 || index1 < 0 || length0 < 0 || length1 < 0)
            throw new IllegalArgumentException("Cannot create an index-length pair with negative index or length.");
        this.index0 = index0;
        this.index1 = index1;
        this.length0 = length0;
        this.length1 = length1;
    }

    public IndexLengthPair(Diff.change hunk) {
        if (hunk == null)
            throw new IllegalArgumentException("Cannot create an index-length pair from NULL hunk.");
        this.index0 = hunk.line0;
        this.index1 = hunk.line1;
        this.length0 = hunk.deleted;
        this.length1 = hunk.inserted;
    }

    public Diff.change createHunk(Diff.change link) {
        return new Diff.change(index0, index1, length0, length1, link);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IndexLengthPair that = (IndexLengthPair) o;

        if (!index0.equals(that.index0)) return false;
        if (!index1.equals(that.index1)) return false;
        if (!length0.equals(that.length0)) return false;
        if (!length1.equals(that.length1)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = index0.hashCode();
        result = 31 * result + index1.hashCode();
        result = 31 * result + length0.hashCode();
        result = 31 * result + length1.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "("+index0+","+index1+","+length0+","+length1+")";
    }
}