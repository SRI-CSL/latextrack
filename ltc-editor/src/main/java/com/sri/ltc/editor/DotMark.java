package com.sri.ltc.editor;

/**
 * Utility to communicate current selection status between the text pane and undo actions.
 *
 * @author linda
 */
public final class DotMark {

    private int dot;
    private int mark;

    public void set(int dot, int mark) {
        this.dot = dot;
        this.mark = mark;
    }

    public int getDot() {
        return dot;
    }

    public int getMark() {
        return mark;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DotMark dotMark = (DotMark) o;

        if (dot != dotMark.dot) return false;
        if (mark != dotMark.mark) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dot;
        result = 31 * result + mark;
        return result;
    }
}
