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
package com.sri.ltc.filter;

import com.sri.ltc.server.LTCserverInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * A singleton class that manages the (persistent) settings for boolean preferences
 * including the status of showing or hiding certain changes.
 *
 * @author linda
 */
public final class Filtering {

    /**
     * --- begin of singleton pattern --------------------------------------
     * Nested class to implement thread-safe singleton with deferred
     * instantiation.  We want to defer creation of instance until the call to
     * getInstance().
     * <p>
     * Using patterns in
     * http://c2.com/cgi/wiki?JavaSingleton
     * http://www.javaworld.com/javaworld/jw-05-2003/jw-0530-letters.html
     */
    private static final class FilterHolder {
        static final Filtering INSTANCE = new Filtering();
    }

    /**
     * Obtains singleton instance of this class.
     * @return singleton instance of this class
     */
    public static synchronized Filtering getInstance() {
        return FilterHolder.INSTANCE;
    }

    // private constructor to prevent multiple instantiations
    private Filtering() {
        init();
    }

    // --- end of singleton pattern ---------------------------------------- //

    private static final Logger logger = Logger.getLogger(Filtering.class.getName());
    private final Preferences preferences = Preferences.userNodeForPackage(this.getClass());
    private final Map<LTCserverInterface.BoolPrefs,Boolean> defaultsShow =
            new HashMap<LTCserverInterface.BoolPrefs,Boolean>();

    private void init () {
        // default values for showing flags
        defaultsShow.put(LTCserverInterface.BoolPrefs.SMALL, false);
        defaultsShow.put(LTCserverInterface.BoolPrefs.DELETIONS, true);
        defaultsShow.put(LTCserverInterface.BoolPrefs.PREAMBLE, true);
        defaultsShow.put(LTCserverInterface.BoolPrefs.COMMANDS, true);
        defaultsShow.put(LTCserverInterface.BoolPrefs.COMMENTS, false);
        // default values for other boolean preferences
        defaultsShow.put(LTCserverInterface.BoolPrefs.COLLAPSE_AUTHORS, false);
    }

    public boolean getStatus(LTCserverInterface.BoolPrefs key) {
        synchronized (preferences) {
            return preferences.getBoolean(key.name(), defaultsShow.get(key));
        }
    }

    public void setStatus(LTCserverInterface.BoolPrefs key, boolean value) {
        synchronized (preferences) {
            preferences.putBoolean(key.name(), value);
            try {
                preferences.flush();
            } catch (BackingStoreException e) {
                logger.log(Level.SEVERE, "Exception while setting status:", e);
            }
        }
    }

    public void resetAllStatus() {
        synchronized (preferences) {
            for (LTCserverInterface.BoolPrefs key : LTCserverInterface.BoolPrefs.values())
                preferences.putBoolean(key.name(), defaultsShow.get(key));
            try {
                preferences.flush();
            } catch (BackingStoreException e) {
                logger.log(Level.SEVERE, "Exception while resetting all status:", e);
            }
        }
    }
}
