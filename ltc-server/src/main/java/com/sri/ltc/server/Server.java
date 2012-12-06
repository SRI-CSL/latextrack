package com.sri.ltc.server;

import org.apache.xmlrpc.webserver.ServletWebServer;

import javax.servlet.ServletException;

/**
 * @author linda
 */
public final class Server extends ServletWebServer {

    public Server(Class iface, Class impl, int port) throws ServletException {
        super(new RpcServlet(iface, impl), port);
    }
}
