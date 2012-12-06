package com.sri.ltc.latexdiff;

import java.util.List;

/**
 * @author linda
 */
public final class Deletion extends Change<String> {

    public Deletion(int start_position, List<IndexFlagsPair<String>> flags) {
        super(start_position, flags);
    }

    @Override
    public String toString() {
        return "<deletion>\n"+toXMLContents()+"</deletion>";
    }
}
