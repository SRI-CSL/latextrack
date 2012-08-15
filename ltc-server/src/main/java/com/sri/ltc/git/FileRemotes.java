/**
 ************************ 80 columns *******************************************
 * Self
 *
 * Created on Aug 11, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.git;

import com.sri.ltc.versioncontrol.Repository;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author linda
 */

// TODO: break this into an interface and let each implementation be separate (or mash them into Repository, but that seems bad)
public final class FileRemotes {
    private final static Logger LOGGER = Logger.getLogger(FileRemotes.class.getName());

    public FileRemotes(Repository gitFile) {
    }

    public Set<Remote> updateAndGetRemotes() {
        Set<Remote> remotes = new HashSet<Remote>();
        LOGGER.fine("Obtained current remotes with "+remotes.size()+" entries.");
        return remotes;
    }

    public int addRemote(String name, String url) {
        // TODO: return codes?
        return 0;
        //return remote.add(workingTree, null, name, url);
    }

    public int rmRemote(String name) {
        return 0;
    }

    public String pull(String repository) {
        return "";
    }

    public String push(String repository) {
        return "";
    }
}