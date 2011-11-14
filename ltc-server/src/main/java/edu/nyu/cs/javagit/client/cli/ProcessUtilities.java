/*
 * ====================================================================
 * Copyright (c) 2008 JavaGit Project.  All rights reserved.
 *
 * This software is licensed using the GNU LGPL v2.1 license.  A copy
 * of the license is included with the distribution of this source
 * code in the LICENSE.txt file.  The text of the license can also
 * be obtained at:
 *
 *   http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *
 * For more information on the JavaGit project, see:
 *
 *   http://www.javagit.com
 * ====================================================================
 */
package edu.nyu.cs.javagit.client.cli;

import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.responses.CommandResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * <code>ProcessUtilities</code> contains methods to help managing processes.
 */
public class ProcessUtilities {

    // TODO (jhl): add unit tests for this class.

    /**
     * Reads the output from the process and prints it to stdout.
     *
     * @param p      The process from which to read the output.
     * @param parser Parser used for processing output lines.
     * @throws java.io.IOException An <code>IOException</code> is thrown if there is trouble reading input from the
     *                     sub-process.
     */
    private static <T extends CommandResponse> void getProcessOutput(Process p, IParser<T> parser) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        int c;
        StringBuilder lineBuffer = new StringBuilder();
        boolean isCR = false; // flag when encountering CR, as we need to take next char into account
        // read input byte-by-byte to preserve line-endings
        while ((c = br.read()) != -1) {
            switch (c) {
                case '\r':
                    if (isCR) {
                        parser.parseLine(lineBuffer.toString(), "\r");
                        // reset data collection structures
                        lineBuffer = new StringBuilder();
                    }
                    isCR = true;
                    break;
                case '\n':
                    if (isCR)
                        parser.parseLine(lineBuffer.toString(), "\r\n");
                    else
                        parser.parseLine(lineBuffer.toString(), "\n");
                    // reset data collection structures
                    lineBuffer = new StringBuilder();
                    isCR = false;
                    break;
                default:
                    if (isCR) {
                        parser.parseLine(lineBuffer.toString(), "\r");
                        // reset data collection structures
                        lineBuffer = new StringBuilder();
                        isCR = false;
                    } else
                        lineBuffer.append((char) c);
            }
        }
        if (isCR) {
            parser.parseLine(lineBuffer.toString(), "\r");
        } else if (lineBuffer.length() > 0)
            parser.parseLine(lineBuffer.toString(), null);
    }

    private static int waitForAndDestroyProcess(Process p) {
        /*
        * I'm not sure this is the best way to handle waiting for a process to complete. -- jhl388
        * 06.14.2008
        */
        int i;
        while (true) {
            try {
                i = p.waitFor();
            } catch (InterruptedException e) {
                // TODO: deal with this interrupted exception in a better manner. -- jhl388 06.14.2008
                i = 128;
            }
            p.destroy();
            return i;
        }
    }

    /**
     * Runs the command specified in the command line with the specified working directory. The
     * IParser is used to parse the response given by the command line.
     *
     * @param workingDirectory The working directory in with which to start the process.
     * @param commandLine      The command line to run.
     * @param parser           The parser to use to parse the command line's response.
     * @return The command response from the <code>IParser</code>.
     * @throws edu.nyu.cs.javagit.api.JavaGitException if an IOException occurs while running the command
     */
    public static <T extends CommandResponse> T runCommand(File workingDirectory, List<String> commandLine, IParser<T> parser)
            throws JavaGitException {

        ProcessBuilder pb = new ProcessBuilder(commandLine);

        if (workingDirectory != null) {
            pb.directory(workingDirectory);
        }

        pb.redirectErrorStream(true);

        try {
            Process p = pb.start();
            getProcessOutput(p, parser);
            parser.processExitCode(waitForAndDestroyProcess(p));
        } catch (IOException e) {
            throw new JavaGitException("IOException while running git command", e);
        }

        return parser.getResponse();
    }

}
