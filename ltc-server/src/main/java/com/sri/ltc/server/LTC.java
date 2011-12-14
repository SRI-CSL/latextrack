/**
 ************************ 80 columns *******************************************
 * LTC
 *
 * Created on May 17, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.server;


import com.sri.ltc.logging.LevelOptionHandler;
import com.sri.ltc.logging.LogConfiguration;
import edu.nyu.cs.javagit.api.JavaGitConfiguration;
import edu.nyu.cs.javagit.api.JavaGitException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Main class to run base system of Latex Track Changes as a server.
 *
 * @author linda
 */
public final class LTC {

    /**
     * --- begin of singleton pattern --------------------------------------
     * Nested class to implement thread-safe singleton with deferred
     * instantiation.  We want to defer creation of instance until the call to
     * getInstance().
     * <p>
     * Using patterns in
     * http://c2.com/cgi/wiki?JavaSingleton
     * http://www.javaworld.com/javaworld/jw-05-2003/jw-0530-letters.html
     */
    private static final class LCHolder {
        static final LTC INSTANCE = new LTC();
    }

    /**
     * Obtains singleton instance of this class.
     * @return singleton instance of this class
     */
    public static synchronized LTC getInstance() {
        return LCHolder.INSTANCE;
    }

    // private constructor to prevent multiple instantiations
    private LTC() {
        init();
    }

    // --- end of singleton pattern ---------------------------------------- //

    static {
        try {
            LogManager.getLogManager().readConfiguration(new LogConfiguration().asInputStream());
            Logger.getLogger(LTC.class.getName()).config("Default logging configuration complete");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static final Logger logger = Logger.getLogger(LTC.class.getName());

    private void init() {
        // TODO: test for Java version 6 or higher (graph algorithms)

        try {
            // test for git
            logger.info("Git version: "+ JavaGitConfiguration.getGitVersion());
        } catch (JavaGitException e) {
            logger.log(Level.SEVERE, "Cannot obtain GIT executable", e);
        }

        try {
            // set up RPC server - this will enable us to receive XML-RPC calls
            Server rpcserver = new Server(
                    LTCserverInterface.class,
                    LTCserverImpl.class,
                    LTCOptions.port);
            rpcserver.start();
            logger.info("Started RPC server on port "+ LTCOptions.port+".");
        } catch (ServletException e) {
            logger.log(Level.SEVERE, "Cannot start RPC server", e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot start RPC server", e);
        }
    }

    private static void printUsage(PrintStream out, CmdLineParser parser) {
        out.println("usage: java -cp ... com.sri.ltc.server.LTC [options...] with");
        parser.printUsage(out);
    }

    private static void setGitDir(String gitDir, String message) {
        if (gitDir != null && !"".equals(gitDir)) {
            logger.info("Trying to set git path to \""+gitDir+"\" "+message);
            try {
                JavaGitConfiguration.setGitPath(gitDir);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Cannot set git path", e);
            }
        }
    }

    public static void main(String[] args) {
        // parse arguments
        CmdLineParser.registerHandler(Level.class, LevelOptionHandler.class);
        LTCOptions options = new LTCOptions();
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

        // configure logging
        try {
            LogConfiguration logConfig = new LogConfiguration();
            logConfig.setProperty("java.util.logging.ConsoleHandler.level",options.consoleLogLevel.getName());
            logConfig.setProperty("java.util.logging.FileHandler.level",options.consoleLogLevel.getName());
            LogManager.getLogManager().readConfiguration(logConfig.asInputStream());
            logger.config("Logging configured to level " + options.consoleLogLevel.getName());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot configure logging", e);
        }

        // handling path information to git executable (if given in environment or via argument
        setGitDir(System.getenv("GIT_BIN_DIR"),"from environment");
        setGitDir(options.gitDir,"from commandline");

        LTC.getInstance(); // start up server (if not already running)
    }

    static class LTCOptions {
        @Option(name="-l",usage="set console log level\nSEVERE, WARNING, INFO, CONFIG (default), FINE, FINER, FINEST")
        Level consoleLogLevel = Level.CONFIG;

        @Option(name="-h",usage="display usage and exit")
        boolean displayHelp = false;

        @Option(name="-p",usage="port on localhost used for XML-RPC")
        static int port = LTCserverInterface.PORT;

        @Option(name="-g",usage="path to directory with git executable",required=false,metaVar="PATH")
        String gitDir = null;
    }
}
