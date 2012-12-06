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
        load(LogConfiguration.class.getResourceAsStream("/com/sri/ltc/logging/logging.properties"));
    }

    public InputStream asInputStream() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        store(buffer, null); // store current properties in buffer
        return new ByteArrayInputStream(buffer.toByteArray());
    }
}
