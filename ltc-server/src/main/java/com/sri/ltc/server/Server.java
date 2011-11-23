/**
 ************************ 80 columns *******************************************
 * Server
 *
 * Created on May 17, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.server;

import org.apache.xmlrpc.webserver.ServletWebServer;

import javax.servlet.ServletException;

/**
 * @author linda
 */
public class Server extends ServletWebServer {

    public Server(Class iface, Class impl, int port) throws ServletException {
        super(new RpcServlet(iface, impl), port);
    }
}
