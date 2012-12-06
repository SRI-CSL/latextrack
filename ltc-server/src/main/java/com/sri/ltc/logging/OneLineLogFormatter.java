package com.sri.ltc.logging;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author linda
 */
public final class OneLineLogFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {        
        return new Date(record.getMillis())+" | "+record.getLevel()+": \t"+record.getMessage()+System.getProperty("line.separator");
    }
}
