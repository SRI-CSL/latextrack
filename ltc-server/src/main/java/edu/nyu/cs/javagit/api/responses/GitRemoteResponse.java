/**
 ************************ 80 columns *******************************************
 * GitRemoteResponse
 *
 * Created on Oct 25, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package edu.nyu.cs.javagit.api.responses;

import edu.nyu.cs.javagit.utilities.CheckUtilities;

import java.util.HashSet;
import java.util.Set;

/**
 * A response data object for the git-remote command.
 */
public final class GitRemoteResponse extends AbstractResponse {

    private final Set<Remote> remotes = new HashSet<Remote>();

    public Set<Remote> getRemotes() {
        return remotes;
    }

    public void addOrUpdateRemote(String name, String url, boolean readOnly) {
        Remote remote = new Remote(name, url, readOnly);
        if (remotes.contains(remote)) {
            if (!readOnly) {
                // update existing entry by overwriting
                remotes.remove(remote);
                remotes.add(remote);
            }
        } else
            remotes.add(remote);
    }

    /**
     * A data structure which  holds information about each remote.
     */
    public class Remote {

        public final String name;
        public final String url;
        boolean readOnly = true;

        public Remote(String name, String url, boolean readOnly) {
            CheckUtilities.checkStringArgument(name, "name of remote");
            CheckUtilities.checkStringArgument(url, "url of remote");
            this.name = name;
            this.url = url;
            this.readOnly = readOnly;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Remote remote = (Remote) o;

            if (!name.equals(remote.name)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
