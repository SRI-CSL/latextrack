/*
 * #%L
 * LaTeX Track Changes (LTC) allows collaborators on a version-controlled LaTeX writing project to view and query changes in the .tex documents.
 * %%
 * Copyright (C) 2009 - 2013 SRI International
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
