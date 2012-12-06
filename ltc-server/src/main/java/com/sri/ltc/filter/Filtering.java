package com.sri.ltc.filter;

import com.sri.ltc.server.LTCserverInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
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
    private final Map<LTCserverInterface.Show,Boolean> defaultsShow =
            new HashMap<LTCserverInterface.Show,Boolean>();

    private void init () {
        // default values for showing flags
        defaultsShow.put(LTCserverInterface.Show.SMALL, false);
        defaultsShow.put(LTCserverInterface.Show.DELETIONS, true);
        defaultsShow.put(LTCserverInterface.Show.PREAMBLE, true);
        defaultsShow.put(LTCserverInterface.Show.COMMANDS, true);
        defaultsShow.put(LTCserverInterface.Show.COMMENTS, false);
    }

    public boolean getShowingStatus(LTCserverInterface.Show key) {
        synchronized (preferences) {
            return preferences.getBoolean(key.name(), defaultsShow.get(key));
        }
    }

    public void setShowingStatus(LTCserverInterface.Show key, boolean value) {
        synchronized (preferences) {
            preferences.putBoolean(key.name(), value);
            try {
                preferences.flush();
            } catch (BackingStoreException e) {
                logger.log(Level.SEVERE, "Exception while setting showing status:", e);
            }
        }
    }

    public void resetShowingStatus() {
        synchronized (preferences) {
            for (LTCserverInterface.Show key : LTCserverInterface.Show.values())
                preferences.putBoolean(key.name(), defaultsShow.get(key));
            try {
                preferences.flush();
            } catch (BackingStoreException e) {
                logger.log(Level.SEVERE, "Exception while resetting showing status:", e);
            }
        }
    }
}
