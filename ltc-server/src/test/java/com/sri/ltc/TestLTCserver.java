/**
 ************************ 80 columns *******************************************
 * TestLTCserver
 *
 * Created on 11/11/11.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc;

import com.sri.ltc.server.LTC;
import edu.nyu.cs.javagit.api.JavaGitConfiguration;
import edu.nyu.cs.javagit.api.JavaGitException;
import org.junit.*;

import java.io.IOException;

/**
 * @author linda
 */
public final class TestLTCserver {

    private final static String gitPath = "/usr/local/git/bin"; // location of git if not on PATH
    private static LTC ltc;
    static {
        try {
            JavaGitConfiguration.setGitPath(gitPath);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JavaGitException e) {
            e.printStackTrace();
        }
        ltc = LTC.getInstance();
    }

    @Before
    public void newline() {
        System.out.println();
    }

    @Test
    public void mainHelp() {
        LTC.main(new String[] {"-h"});
    }

    @Test
    public void mainLevel() {
        for (String level : new String[]{"FINE", "INFO"}) {
            System.out.println("Setting log level to "+level+":");
            LTC.main(new String[] {"-l",level});
            // TODO: assert that no log output if level == INFO
        }
    }

    @Test
    public void instance() {
        Assert.assertEquals(ltc, LTC.getInstance());
    }
}
