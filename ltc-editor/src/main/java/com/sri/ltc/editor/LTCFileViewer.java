/**
 ************************ 80 columns *******************************************
 * LTCFileViewer
 *
 * Created on 5/24/12.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.editor;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import javax.swing.*;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * A non-editable version of LTC that uses a list of files instead of a GIT repository.
 *
 * @author linda
 */
public final class LTCFileViewer extends LTCGui {

    private final Preferences preferences = Preferences.userRoot().node(this.getClass().getCanonicalName().replaceAll("\\.","/"));

    public LTCFileViewer() {
        super(false, "LTC File Viewer");
    }

    private static void printUsage(PrintStream out, CmdLineParser parser) {
        out.println("usage: java -cp ... com.sri.ltc.editor.LTCEditor [options...] [FILE] \nwith");
        parser.printUsage(out);
    }

    public static void main(String[] args) {
        // parse arguments
        final LTCFileViewerOptions options = new LTCFileViewerOptions();
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
            System.exit(1);
        }

        final LTCFileViewer viewer = new LTCFileViewer();

        if (options.resetDefaults) {
            try {
                LOGGER.config("Resetting preferences to defaults");
                viewer.preferences.clear();
            } catch (BackingStoreException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(viewer);
            }
        });
    }

    static class LTCFileViewerOptions {
        @Option(name="-h", usage="display usage and exit")
        boolean displayHelp = false;

        @Option(name = "-r", usage = "reset to default settings")
        boolean resetDefaults = false;
    }
}
