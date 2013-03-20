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
package com.sri.ltc.server;

import com.sri.ltc.versioncontrol.TrackedFile;
import com.sri.ltc.versioncontrol.VersionControlException;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * @author linda
 */
public final class SessionManager {

    private final static String LOCK = "object to synchronize on";
    private final static Map<Integer,Session> sessions = new HashMap<Integer,Session>();
    private final static Set<TrackedFile> activeFiles = new HashSet<TrackedFile>();

    /**
     * Create a session for the given tracked file.  After this method finishes, the returned ID
     * is know for the created session, and the given file is considered active.  If the given file
     * was already active when this method is called, it will throw an <code>IllegalStateException</code>
     * and not create a new session.  The created session will be initialized with the version history
     * of the tracked file.
     *
     * @param file Tracked file for which to create a new session
     * @return ID of the newly created session
     * @throws IOException if an IO error occurs while initializing the session with the version
     *         history of the tracked file
     * @throws ParseException if a parse error occurs while initializing the session with the version
     *         history of the tracked file
     * @throws VersionControlException if an error with the underlying version control system occurs
     *         while initializing the session with the version history of the tracked file
     */
    public static int createSession(TrackedFile file) throws IOException, ParseException, VersionControlException {
        synchronized (LOCK) {
            if (isActive(file))
                throw new IllegalStateException("cannot create session for tracked file that is already active");
            Session session = new Session(file);
            if (sessions.containsKey(session.ID))
                finishSession(session.ID); // the ID's wrapped, so discard the previously known session
            sessions.put(session.ID, session);
            activeFiles.add(file);
            return session.ID;
        }
    }

    /**
     * Finish that is remove an active session denoted by its ID.  If the given ID does not correspond
     * to an active session, nothing will happen and the method returns <code>null</code>. Otherwise,
     * the active session is made inactive and the corresponding file is no longer considered active
     * after this method finishes.
     *
     * @param sessionID ID of the session to be made inactive
     * @return session that was made inactive
     */
    public static Session finishSession(int sessionID) {
        synchronized (LOCK) {
            Session session = sessions.remove(sessionID);
            if (session != null)
                activeFiles.remove(session.getTrackedFile());
            return session;
        }
    }

    /**
     * Get the active session for a given ID.  If the given ID does not correspond to an active session,
     * the method returns <code>null</code>.
     *
     * @param sessionID ID of the session to be made inactive
     * @return corresponding active session of <code>null</code> if none with given ID exists
     */
    public static Session getSession(int sessionID) {
        synchronized (LOCK) {
            return sessions.get(sessionID);
        }
    }

    /**
     * Test whether the given tracked file is currently active, i.e., a session exists for this file.
     *
     * @param file Tracked file to test
     * @return whether the given file is currently active
     */
    public static boolean isActive(TrackedFile file) {
        synchronized (LOCK) {
            return activeFiles.contains(file);
        }
    }
}
