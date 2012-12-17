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
package com.sri.ltc.server;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.webserver.XmlRpcServlet;

import java.io.IOException;

/**
 * @author linda
 */
public final class RpcServlet extends XmlRpcServlet {

    private static final long serialVersionUID = 8467025898193162324L;
    protected Class iface;
    protected Class impl;

    public RpcServlet(Class iface, Class impl) {
        this.iface = iface;
        this.impl = impl;
    }

    // Override newXmlRpcHandlerMapping() to prevent attempt to
    // open property file under name of superclass, which is just
    // a pain when all we want to do is support the impl of
    // one interface
    protected XmlRpcHandlerMapping newXmlRpcHandlerMapping()throws XmlRpcException {
        try {
            return newPropertyHandlerMapping(null);
        } catch (IOException e) {
            throw new XmlRpcException("Failed to load resource:" + e.getMessage(), e);
        }
    }

    // Instead of loading properties from a file, we override this
    // method to set up a mapping based on the classes given to the
    // constructor.  If for some reason we are handed a URL,
    // fallback to default behavior of superclass (which will try to load
    // properties file)
    protected PropertyHandlerMapping newPropertyHandlerMapping(java.net.URL url)
            throws java.io.IOException, XmlRpcException {
        PropertyHandlerMapping mapping;
        if (url == null) {
            mapping = new PropertyHandlerMapping();
            mapping.addHandler(iface.getName(), impl);
        } else
            mapping = super.newPropertyHandlerMapping(url);
        return mapping;
    }
}
