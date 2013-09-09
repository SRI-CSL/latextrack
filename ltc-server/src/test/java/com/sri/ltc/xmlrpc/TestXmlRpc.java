/*
 * #%L
 * LaTeX Track Changes (LTC) allows collaborators on a version-controlled LaTeX writing project to view and query changes in the .tex documents.
 * %%
 * Copyright (C) 2009 - 2013 SRI International
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
package com.sri.ltc.xmlrpc;

import com.sri.ltc.categories.IntegrationTests;
import com.sri.ltc.server.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.xmlrpc.XmlRpcException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import static junit.framework.Assert.assertEquals;

/**
 * @author linda
 */
@Category(IntegrationTests.class)
public final class TestXmlRpc {

    private static final Logger logger = Logger.getLogger(LTC.class.getName());
    private final static int PORT = LTCserverInterface.PORT+1;
    private static myIF myInterface;

    @BeforeClass
    public static void connect() throws IOException, ServletException, InterruptedException {
        Server server = new Server(
                myIF.class,
                myImpl.class,
                PORT);
        server.start();
        logger.info("Started RPC server on port "+PORT+".");
        Thread.sleep(200);

        // connect a second XML-RPC server-client instance (not LTC)
        HelloLTC.Client myClient = new HelloLTC.Client(new URL("http://localhost:"+PORT+"/xmlrpc"));
        myInterface = (myIF) myClient.GetProxy(myIF.class);
    }

    @Test
    public void hello() throws XmlRpcException {
        assertEquals("The answer is 42", 42, myInterface.hello());
    }

    @Test
    public void echo() throws XmlRpcException {
        String message;

        message = "Hello World!\n";
        assertEquals("Hello World", message, myInterface.echo(message));
    }

    @Test
    public void echo64() throws XmlRpcException {
        byte[] message, echo;
        String messageString, echoString;

        messageString = "\nHello, different line endings!\r\n\r\n";
        message = Base64.encodeBase64(messageString.getBytes());
        echo = myInterface.echo64(message);
        echoString = new String(Base64.decodeBase64(echo));
        assertEquals("line endings with base64", messageString, echoString);
    }
}
