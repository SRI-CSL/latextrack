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
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import com.sri.ltc.xplatform.AppInterface;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Common utilities for all LTC classes.
 *
 * @author linda
 */
public final class CommonUtils {

    private static final Logger LOGGER = Logger.getLogger(CommonUtils.class.getName());

    public final static FilenameFilter LOG_FILE_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            return file.isDirectory() && s.matches("\\.LTC.*\\.log");
        }
    };

    // for natural language dates
    public final static DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    private final static Parser NATTY_PARSER = new Parser();

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

    // obtain build properties
    private final static Properties BUILD_PROPERTIES = new Properties();
    static {
        InputStream is = ClassLoader.getSystemResourceAsStream("build.properties");
        if (is != null)
            try {
                BUILD_PROPERTIES.load(is);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "cannot load build properties", e);
            }
    }

    // obtain logo image
    private final static Icon LOGO;
    static {
        URL imgUrl = ClassLoader.getSystemResource("images/LTC-logo.png");
        if (imgUrl == null) {
            LOGO = null;
            LOGGER.severe("cannot find logo image");
        } else
            LOGO = new ImageIcon(imgUrl);
    }

    /**
     * Obtain copyright notice and disclaimer as a String.
     * @return Copyright notice and disclaimer
     */
    public static String getNotice() {
        return NOTICE;
    }

    /**
     * Obtain license as a String.
     * @return License text
     */
    public static String getLicense() {
        return LICENSE;
    }

    /**
     * Obtain build properties such as version number, git SHA1 and time stamp.
     * @return Build properties
     */
    public static Properties getBuildProperties() {
        return BUILD_PROPERTIES;
    }

    public static String getVersion() {
        return BUILD_PROPERTIES.getProperty("build.version","UNKNOWN");
    }

    public static String getBuildInfo() {
        return BUILD_PROPERTIES.getProperty("build.commit.number","<UNKNOWN SHA-1>")+" @ "+
                BUILD_PROPERTIES.getProperty("build.commit.time","<UNKNOWN TIME>");
    }

    /**
     * Obtain LTC logo as an icon.
     * @return LTC logo or <code>null</code> if not found
     */
    public static Icon getLogo() {
        return LOGO;
    }

    /**
     * Whether or not we are running on Mac OS X.
     * @return true if we are running on Mac OS X
     */
    public static boolean isMacOSX() {
        return System.getProperty("os.name").toLowerCase().startsWith("mac os x");
    }

    public static void customizeApp(String image) {
        Class myClass = null;
        String myClassName = "com.sri.ltc.xplatform.UnknownApp";
        if (CommonUtils.isMacOSX())
            myClassName = "com.sri.ltc.xplatform.MacOSXApp";
        try {
            myClass = Class.forName(myClassName);
            AppInterface appInterface = (AppInterface) myClass.getConstructor(String.class).newInstance(image);
            appInterface.customize();
        } catch (Exception e) {
            // ignore
            LOGGER.log(Level.SEVERE, "Customizing LTC application: " + e.getMessage(), e);
        }
    }

    /**
     * Translate given date into a String representation using the {@link #FORMATTER}.
     *
     * @param date Date to translate
     * @return String representing the given date
     */
    public static String serializeDate(Date date) {
        return FORMATTER.format(date);
    }

    /**
     * Try to parse given date into a Java Date instance.  If the date does not adhere to {@link #FORMATTER}, we try
     * to parse it with Natty that understands natural language to some extend.
     *
     * @param date String describing the date
     * @return Date that the given String described
     */
    public static Date deSerializeDate(String date) throws ParseException {
        Date result = null;
        try {
            result = FORMATTER.parse(date);
        } catch (ParseException e) {
            // now try to parse with natty:
            List<DateGroup> groups = NATTY_PARSER.parse(date);
            if (groups.isEmpty())
                throw new ParseException("Cannot parse given string into a date (empty groups)", 0);
            List<Date> dates = groups.get(0).getDates();
            if (dates.isEmpty())
                throw new ParseException("Cannot parse given string into a date (empty dates)", groups.get(0).getPosition());
            result = dates.get(0);
        }
        return result;
    }
}
