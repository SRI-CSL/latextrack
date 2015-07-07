/*
 * #%L
 * LaTeX Track Changes (LTC) allows collaborators on a version-controlled LaTeX writing project to view and query changes in the .tex documents.
 * %%
 * Copyright (C) 2009 - 2015 SRI International
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

import com.google.common.collect.Sets;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Encapsulate the command or action and its arguments for editing LTC preferences through the server
 * command line invocation.
 *
 * @author linda
 */
public final class EditPreferences {

    final static Preferences PREFERENCES = Preferences.userNodeForPackage(LTCserverImpl.class);

    public enum Command {DISPLAY,SET,REMOVE};

    public final Command command;
    public final String key;
    public final String value;

    public EditPreferences(Command command, String key, String value) {
        if (command == null)
            this.command = Command.DISPLAY;
        else
            this.command = command;
        if (!Command.DISPLAY.equals(this.command))
            if (key == null)
                throw new IllegalArgumentException("Cannot create preference edit data for SET or REMOVE with NULL key!");
        this.key = key.trim();
        this.value = value.trim();
    }

    public void perform() throws BackingStoreException {
        switch (command) {
            case DISPLAY:
                for (String key : PREFERENCES.keys())
                    if (this.key == null || key.contains(this.key))
                        System.out.println("\"" + key + "\" => " + PREFERENCES.get(key, null));
                break;
            case SET:
                PREFERENCES.put(key, value);
                System.out.println("SET \"" + key + "\" => " + PREFERENCES.get(key, null));
                break;
            case REMOVE:
                if (Sets.newHashSet(PREFERENCES.keys()).contains(key)) {
                    PREFERENCES.remove(key);
                    System.out.println("REMOVE \"" + key + "\"");
                } else
                    System.err.println("Cannot remove \"" + key + "\" as preference with this key does not exist!");
                break;
        }
    }

    @Override
    public String toString() {
        return command + " " + (key==null?"*":key) + " " + (value==null?"":value);
    }
}
