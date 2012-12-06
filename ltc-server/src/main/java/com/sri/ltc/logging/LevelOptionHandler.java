package com.sri.ltc.logging;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

import java.util.logging.Level;

/**
 * @author linda
 */
public final class LevelOptionHandler extends OptionHandler<Level> {

    public LevelOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Level> setter) {
        super(parser, option, setter);
    }

    @Override
    public int parseArguments(Parameters parameters) throws CmdLineException {
        setter.addValue(Level.parse(parameters.getParameter(0)));
        return 1;
    }

    @Override
    public String getDefaultMetaVariable() {
        return "LEVEL";
    }
}
