/**
 ************************ 80 columns *******************************************
 * FileLogFormatter
 *
 * Created on Aug 27, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author linda
 */
public final class FileLogFormatter extends Formatter {

    /**
     * An instance of a DateFormatter that is used for formatting
     * the time of a log record into a human-readable string,
     * according to the rules of the current locale.  The value
     * is set after the first invocation of format, since it is
     * common that a JVM will instantiate a SimpleFormatter without
     * ever using it.
     */
    private DateFormat dateFormat;

    /**
     * Always adding an indent to following lines.
     */
    private static final String lineSep = System.getProperty("line.separator")+"  ";

    @Override
    public String format(LogRecord record) {
        StringBuffer buf = new StringBuffer(180);

        if (dateFormat == null)
            dateFormat = DateFormat.getDateTimeInstance();

        // first line
        buf.append(dateFormat.format(new Date(record.getMillis())));
        buf.append(' ');
        buf.append(record.getSourceClassName());
        buf.append(' ');
        buf.append(record.getSourceMethodName());
        buf.append(" \t");

        // second line
        buf.append(record.getLevel());
        buf.append(": ");
        buf.append(formatMessage(record));
        buf.append(lineSep);

        Throwable throwable = record.getThrown();
        if (throwable != null) {
            StringWriter sink = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sink, true));
            buf.append(sink.toString());
        } else
            return buf.substring(0, buf.length()-2); // remove the last 2 indents

        return buf.toString();
    }
}
