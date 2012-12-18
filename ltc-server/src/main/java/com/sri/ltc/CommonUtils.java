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
package com.sri.ltc;

import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Common utilities for all LTC classes.
 *
 * @author linda
 */
public class CommonUtils {

    private static final Logger LOGGER = Logger.getLogger(CommonUtils.class.getName());

    // create notice
    private static String NOTICE = "";
    static {
        InputStream is = ClassLoader.getSystemResourceAsStream("about.txt");
        if (is != null)
            try {
                NOTICE = CharStreams.toString(new InputStreamReader(is, "UTF-8"));
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "cannot find or decode notice from resources", e);
            }
    }

    // create license
    private static String LICENSE = "";
    static {
        InputStream is = ClassLoader.getSystemResourceAsStream("LICENSE");
        if (is != null)
            try {
                LICENSE = CharStreams.toString(new InputStreamReader(is, "UTF-8"));
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "cannot find or decode license from resources", e);
            }
    }

    /**
     * Obtain copyright notice and disclaimer as a String.
     * @return Copyright notice and disclaimer
     */
    public static String getNotice() {
        return NOTICE;
    }

    public static String getLicense() {
        return LICENSE;
    }
}
