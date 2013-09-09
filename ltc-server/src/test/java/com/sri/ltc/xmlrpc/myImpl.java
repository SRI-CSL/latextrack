package com.sri.ltc.xmlrpc;

import org.apache.xmlrpc.XmlRpcException;

/**
 * @author linda
 */
public final class myImpl implements myIF {

    @Override
    public int hello() throws XmlRpcException {
        return 42;
    }

    @Override
    public String echo(String message) throws XmlRpcException {
        return message;
    }

    @Override
    public byte[] echo64(byte[] message) throws XmlRpcException {
        return message;
    }
}