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

import javax.swing.text.BadLocationException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapping a marked up document, which implements a method to remove any characters that are marked up as
 * additions for a given lexeme.
 *
 * @author linda
 */
public final class DocumentReaderWrapper extends AbstractReaderWrapper<MarkedUpDocument> implements ReaderWrapper {

    public DocumentReaderWrapper(MarkedUpDocument document) {
        super(document);
    }

    @Override
    public Reader createReader() throws BadLocationException {
        return getWrapped().getReader();
    }

    @Override
    public Lexeme removeAdditions(Lexeme lexeme) {
        // remove any characters marked as additions
        StringBuilder newContents = new StringBuilder();
        List<Integer> removed = new ArrayList<Integer>(); // collect removed character positions

        for (int i = 0; i < lexeme.length; i++)
            if (getWrapped().isAddition(lexeme.pos+i))
                removed.add(i);
            else
                newContents.append(lexeme.contents.charAt(i));
        if (removed.isEmpty())
            return lexeme;
        else {
            if (newContents.length() == 0)
                return null; // the whole lexeme was an addition
            else
                return new Lexeme(
                        lexeme.type,
                        newContents.toString(),
                        lexeme.pos,
                        lexeme.preambleSeen,
                        removed.toArray(new Integer[removed.size()])
                );
        }
    }
}
