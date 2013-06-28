package com.sri.ltc.versioncontrol.history;

import com.sri.ltc.filter.Author;
import com.sri.ltc.latexdiff.ReaderWrapper;

/**
 * Encapsulating the revision, author, and reader of a history unit.
 * @author linda
 */
public final class HistoryUnit {

    public final String revision;
    public final Author author;
    public final ReaderWrapper reader;

    public HistoryUnit(Author author, String revision, ReaderWrapper reader) {
        if (author == null || revision == null || reader == null)
            throw new IllegalArgumentException("Cannot create history unit with NULL components");
        this.author = author;
        this.revision = revision;
        this.reader = reader;
    }
}
