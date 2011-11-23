/**
 ************************ 80 columns *******************************************
 * Translocation
 *
 * Created on Apr 30, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

/**
 * @author linda
 */
public final class Translocation extends Change {

    public final int position_offset;

    /**
     * Create by default the identity translocation at the origin.
     */
    public Translocation() {
        this(0, 0);
    }

    public Translocation(int start_position, int position_offset) {
        super(start_position, false, false, false);
        this.position_offset = position_offset;
    }

    public int apply(int position) {
        return position+position_offset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Translocation that = (Translocation) o;

        if (position_offset != that.position_offset) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + position_offset;
        return result;
    }

    String toXMLContents() {
        StringBuffer buffer = new StringBuffer(super.toXMLContents());
        buffer.append("  <differences position="+position_offset+"/>\n");
        return buffer.toString();
    }

    @Override
    public String toString() {
        return "<translocation>\n"+toXMLContents()+"</translocation>";
    }
}
