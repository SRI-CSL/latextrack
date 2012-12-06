package com.sri.ltc.server;

import com.sri.ltc.versioncontrol.TrackedFile;
import com.sri.ltc.versioncontrol.VersionControlException;

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

    public static int createSession(TrackedFile gitFile) throws IOException, ParseException, VersionControlException {
        Session session = new Session(gitFile);
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
