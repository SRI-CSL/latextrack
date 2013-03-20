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

import com.sri.ltc.CommonUtils;
import com.sri.ltc.logging.LogConfiguration;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.util.ClientFactory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Simple command line utility to test whether LTC Server is running.
 *
 * @author linda
 */
public class HelloLTC {

    private final static Logger LOGGER = Logger.getLogger(HelloLTC.class.getName());
    static {
        // first thing is to configure Mac OS X before AWT gets loaded:
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Hello LTC");
        // print NOTICE on command line
        System.out.println(CommonUtils.getNotice()); // output notice
        // default configuration for logging
        try {
            LogManager.getLogManager().readConfiguration(new LogConfiguration().asInputStream());
            LOGGER.fine("Default logging configuration complete");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Couldn't configure logging", e);
        }
    }

    private static void printUsage(PrintStream out, CmdLineParser parser) {
        out.println("usage: java -cp ... "+HelloLTC.class.getCanonicalName()+" [options...] \nwith");
        parser.printUsage(out);
    }

    public static void main(String[] args) {
        // parse arguments
        MyOptions options = new MyOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            printUsage(System.err, parser);
            return;
        }

        if (options.displayHelp) {
            printUsage(System.out, parser);
            return;
        }

        if (options.displayLicense) {
            System.out.println("LTC is licensed under:\n\n" + CommonUtils.getLicense());
            return;
        }

        try {
            // obtain server instance:
            Client client = new Client(new URL("http://localhost:"+MyOptions.port+"/xmlrpc"));
            LTCserverInterface server = (LTCserverInterface) client.GetProxy(LTCserverInterface.class);

            // request answer to the question of life:
            System.out.println("The answer to the question of life is: "+ server.hello());
            System.out.println("(Seeing this means your LTC server on port "+MyOptions.port+" is running.)");
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "Couldn't obtain server URL", e);
        } catch (XmlRpcException e) {
            System.err.println(" *** ERROR: Could not connect to LTC server on port "+MyOptions.port);
        }
    }

    static class MyOptions {
        @Option(name="-h",usage="display usage and exit")
        boolean displayHelp = false;

        @Option(name="-c",usage="display copyright/license information and exit")
        boolean displayLicense = false;

        @Option(name="-p",usage="port on localhost used for XML-RPC")
        static int port = LTCserverInterface.PORT;
    }

    public static class Client {
        private XmlRpcClientConfigImpl config;
        private XmlRpcClient client;

        public Client(URL url) {
            config = new XmlRpcClientConfigImpl();
            config.setServerURL(url);
            client = new XmlRpcClient();
            client.setConfig(config);
        }

        public Object GetProxy(Class ifClass) {
            return new ClientFactory(client).newInstance(ifClass);
        }
    }
}
