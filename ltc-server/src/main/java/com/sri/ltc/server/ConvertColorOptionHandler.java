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

import java.awt.*;

/**
 * @author linda
 */
public class ConvertColorOptionHandler extends OptionHandler<Color> {

    public ConvertColorOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Color> setter) {
        super(parser, option, setter);
    }

    @Override
    public int parseArguments(Parameters parameters) throws CmdLineException {
        if (parameters.size() == 1) {
            setter.addValue(Color.decode(parameters.getParameter(0)));
            return 1;
        }
        if (parameters.size() == 3) {
            setter.addValue(new Color(
                    Integer.decode(parameters.getParameter(0)),
                    Integer.decode(parameters.getParameter(1)),
                    Integer.decode(parameters.getParameter(2))));
            return 3;
        }
        throw new RuntimeException("Need either 1 or 3 numeric arguments to convert color.");
    }

    @Override
    public String getDefaultMetaVariable() {
        return "int | R G B";
    }
}
