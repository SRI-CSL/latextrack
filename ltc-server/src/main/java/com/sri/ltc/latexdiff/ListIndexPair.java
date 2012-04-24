/**
 ************************ 80 columns *******************************************
 * ListIndexPair
 *
 * Created on 4/24/12.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import java.util.List;

/**
 * A pair of a lexeme list and an index, where the last preamble lexeme was seen.
 * The list may not be NULL and the index not smaller than -1.
 *
 * @author linda
 */
public final class ListIndexPair {

    final List<Lexeme> list;
    final Integer index;

    public ListIndexPair(List<Lexeme> list, Integer index) {
        if (list == null || index == null)
            throw new IllegalArgumentException("Cannot create a pair with a NULL list.");
        if (index < -1)
            throw new IllegalArgumentException("Cannot create a pair with index < -1.");
        this.list = list;
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListIndexPair that = (ListIndexPair) o;

        if (!index.equals(that.index)) return false;
        if (!list.equals(that.list)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = list.hashCode();
        result = 31 * result + index.hashCode();
        return result;
    }
}
