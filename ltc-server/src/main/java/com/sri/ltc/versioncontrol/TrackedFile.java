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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public abstract class TrackedFile<RepositoryClass extends Repository> {

    private final RepositoryClass repository;
    private final File file;

    public enum Status {
        Added,
        Changed,
        Conflicting,
        Ignored,
        Missing,
        Modified,
        NotTracked,
        Removed,
        Unchanged,
        Unknown
    }
    public static final String HAT_REVISION = "Hat on top of HEAD";

    /**
     * Obtain list of commits in topological order.  The list could be empty but not NULL.
     * @return List of commits in topological order (could be empty)
     * @throws VersionControlException
     * @throws IOException
     */
    abstract public List<Commit> getCommits() throws VersionControlException, IOException;
    // Note: must support/default to the following options or their equivalent
    //    options.setOptFormatDate("iso8601");
    //    options.setOptOrderingTopological(true);
    //    options.setOptGraph(true);
    //    options.setOptFormat("commit %H%nAuthor: %an <%ae>%nDate: %ad%nParents: %P%n%s%n");

    abstract public List<Commit> getCommits(@Nullable Date inclusiveLimitDate, @Nullable String inclusiveLimitRevision) throws VersionControlException, IOException;

    abstract public Status getStatus() throws VersionControlException;

    abstract public Commit commit(String message) throws Exception;

    protected TrackedFile(RepositoryClass repository,@Nonnull File file) {
        if (file == null)
            throw new IllegalArgumentException("Cannot create tracked file with NULL file");
        this.repository = repository;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public RepositoryClass getRepository() {
        return repository;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrackedFile)) return false;

        TrackedFile that = (TrackedFile) o;

        if (!file.equals(that.file)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }
}
