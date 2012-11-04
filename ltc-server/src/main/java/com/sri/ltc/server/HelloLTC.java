/**
 ************************ 80 columns *******************************************
 * HelloLTC
 *
 * Created on 12/16/11.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.server;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.util.ClientFactory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple command line utility to test whether LTC Server is running.
 *
 * @author linda
 */
public class HelloLTC {

    private final static Logger LOGGER = Logger.getLogger(HelloLTC.class.getName());

    private static void printUsage(PrintStream out, CmdLineParser parser) {
        out.println("usage: java -cp ... com.sri.ltc.server.HelloLTC [options...] \nwith");
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
