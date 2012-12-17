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

/**
 * @author linda
 */
public final class Lexeme {

    final LexemeType type;
    final String contents;
    final int pos, length;
    final boolean preambleSeen;
    final boolean inComment;
    final Integer[] removed;

    public Lexeme(LexemeType type, String contents, int pos, boolean preambleSeen, boolean inComment, Integer... removed) {
        if (type == null)
            throw new IllegalArgumentException("Cannot create Lexeme of type NULL");
        this.type = type;
        if (contents == null)
            throw new IllegalArgumentException("Cannot create Lexeme with contents NULL");
        this.contents = contents;
        this.length = contents.length();
        this.pos = pos;
        this.preambleSeen = preambleSeen;
        this.inComment = inComment;
        this.removed = removed;
    }

    public String displayContents() {
        return type.isPrintable()?contents:"";
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(type+" "+displayContents()+"  @ "+pos+" ("+length+")");
        if (preambleSeen)
            result.append(" P");
        if (inComment)
            result.append(" C");
        if (removed != null && removed.length > 0) {
            result.append(" [");
            for (int i = 0; i < removed.length; i++)
                result.append(removed[i]+",");
            result.deleteCharAt(result.length()-1); // remove last ","
            result.append("]");
        }
        return result.toString();
    }
}
