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
package com.sri.ltc.versioncontrol;

import com.sri.ltc.filter.Author;

import java.io.File;
import java.io.IOException;

public interface Repository {
    public void addFile(File file) throws Exception;

    public TrackedFile getFile(File file) throws IOException;

    public Remotes getRemotes();

    /**
     * Create a bundle for bug reporting purposes of the current repository.
     *
     * @param outputDirectory directory where to create the bundle
     * @return File that contains the bundle or <code>null</code> if not possible
     * @throws IOException if the bundle cannot be generated in a file
     */
    public File getBundle(File outputDirectory) throws IOException;

    // TODO: could push these into a separate interface, but probably not needed
    public Author getSelf();
    public void setSelf(Author author);
    public void resetSelf();
}
