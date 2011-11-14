/**
 ************************ 80 columns *******************************************
 * LogConfiguration
 *
 * Created on Sep 3, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.logging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author linda
 */
@SuppressWarnings("serial")
public final class LogConfiguration extends Properties {

    public LogConfiguration() throws IOException {
        load(LogConfiguration.class.getResourceAsStream("/src/main/resources/com/sri/ltc/logging/logging.properties"));
    }

    public InputStream asInputStream() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        store(buffer, null); // store current properties in buffer
        return new ByteArrayInputStream(buffer.toByteArray());
    }
}
