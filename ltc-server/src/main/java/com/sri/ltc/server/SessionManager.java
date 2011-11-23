/**
 ************************ 80 columns *******************************************
 * SessionManager
 *
 * Created on May 19, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.server;

import edu.nyu.cs.javagit.api.GitFile;
import edu.nyu.cs.javagit.api.JavaGitException;

import javax.swing.text.BadLocationException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author linda
 */
public class SessionManager {

    final static Map<Integer,Session> sessions =
            Collections.synchronizedMap(new HashMap<Integer,Session>());

    public static int createSession(GitFile gitFile, String currentText) throws IOException, JavaGitException, ParseException, BadLocationException {
        Session session = new Session(gitFile, currentText);
        sessions.put(session.ID, session);
        return session.ID;
    }

    public static Session removeSession(int sessionID) {
        return sessions.remove(sessionID);
    }

    public static Session getSession(int sessionID) {
        return sessions.get(sessionID);
    }
}
