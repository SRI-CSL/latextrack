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
        boolean changed = false;
        StringBuilder newContents = new StringBuilder();
        try {
            for (int i = 0; i < lexeme.length; i++) {
                if (!getWrapped().isAddition(lexeme.pos+i))
                    newContents.append(lexeme.contents.charAt(i));
                else
                    changed = true;
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        if (changed) {
            if (newContents.length() == 0)
                return null; // the whole lexeme was an addition
            else
                return new Lexeme(
                        lexeme.type,
                        newContents.toString(),
                        lexeme.pos
                );
        } else
            return lexeme;
    }
}
