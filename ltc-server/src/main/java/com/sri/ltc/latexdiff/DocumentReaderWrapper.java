/**
 ************************ 80 columns *******************************************
 * DocumentReaderWrapper
 *
 * Created on 4/20/12.
 *
 * Copyright 2009-2010, SRI International.
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
    public Reader createReader() {
        return getWrapped().getReader();
    }

    @Override
    public Lexeme removeAdditions(Lexeme lexeme) {
        // remove any characters marked as additions
        StringBuilder newContents = new StringBuilder();
        List<Integer> removed = new ArrayList<Integer>(); // collect removed character positions

        try {
            for (int i = 0; i < lexeme.length; i++)
                if (getWrapped().isAddition(lexeme.pos+i))
                    removed.add(i);
                else
                    newContents.append(lexeme.contents.charAt(i));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
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
                        lexeme.preambleSeen, lexeme.inComment, removed.toArray(new Integer[removed.size()])
                );
        }
    }
}
