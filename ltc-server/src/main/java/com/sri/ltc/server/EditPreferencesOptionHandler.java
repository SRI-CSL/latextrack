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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * @author linda
 */
public final class EditPreferencesOptionHandler extends OptionHandler<EditPreferences> {

    public EditPreferencesOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super EditPreferences> setter) {
        super(parser, option, setter);
    }

    @Override
    public String getDefaultMetaVariable() {
        return "[CMD ARG*]";
    }

    @Override
    public int parseArguments(Parameters parameters) throws CmdLineException {
        if (parameters.size() == 0) {
            setter.addValue(new EditPreferences(null, null, null));
            return 0;
        }
        EditPreferences.Command command = null;
        switch (parameters.getParameter(0).toLowerCase().charAt(0)) {
            case 'd':
                command = EditPreferences.Command.DISPLAY;
                break;
            case 's':
                command = EditPreferences.Command.SET;
                break;
            case 'r':
                command = EditPreferences.Command.REMOVE;
                break;
            default:
                throw new RuntimeException("Cannot decode edit preferences command: "+parameters.getParameter(0));
        }
        int consumed = 1;
        String key = null, value = null;
        if (parameters.size() > 1) {
            key = parameters.getParameter(1);
            consumed = 2;
            if (EditPreferences.Command.SET.equals(command))
                if (parameters.size() > 2) {
                    // ignore value for anything but SET:
                    value = parameters.getParameter(2);
                    consumed = 3;
                } else
                    throw new RuntimeException("Need a value for SET command!");
        }
        setter.addValue(new EditPreferences(command, key, value));
        return consumed;
    }
}
