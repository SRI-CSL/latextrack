package com.sri.ltc.xmlrpc;

import org.apache.xmlrpc.XmlRpcException;

/**
 * @author linda
 */
public interface myIF {
    public int hello() throws XmlRpcException;
    public String echo(String message) throws XmlRpcException;
    public byte[] echo64(byte[] message) throws XmlRpcException;
}
