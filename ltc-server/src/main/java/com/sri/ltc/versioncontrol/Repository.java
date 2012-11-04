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
