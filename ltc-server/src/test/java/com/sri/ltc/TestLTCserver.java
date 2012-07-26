/**
 ************************ 80 columns *******************************************
 * TestLTCserver
 *
 * Created on 11/11/11.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc;

import com.sri.ltc.server.HelloLTC;
import com.sri.ltc.server.LTC;
import com.sri.ltc.server.LTCserverInterface;
import edu.nyu.cs.javagit.TestGitRepository;
import edu.nyu.cs.javagit.api.JavaGitConfiguration;
import edu.nyu.cs.javagit.api.JavaGitException;
import org.apache.xmlrpc.XmlRpcException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

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
        System.out.format("\nGetting server help:\n");
        LTC.main(new String[] {"-h"});
    }

    @Test
    public void mainLevel() {
        for (String level : new String[]{"FINE", "INFO"}) {
            System.out.format("\nSetting log level to %s:\n",level);
            LTC.main(new String[] {"-l",level});
            // TODO: assert that no log output if level == INFO
        }
    }

    @Test
    public void bugReport() throws MalformedURLException, XmlRpcException, FileNotFoundException {
        HelloLTC.Client client = new HelloLTC.Client(new URL("http://localhost:" + LTCserverInterface.PORT + "/xmlrpc"));
        LTCserverInterface server = (LTCserverInterface) client.GetProxy(LTCserverInterface.class);

        TestGitRepository.createDirs();
        TestGitRepository testGitRepository = new TestGitRepository();
        testGitRepository.showFoo();
        int sessionID = server.init_session(testGitRepository.getFooFile().getPath());
        server.set_limited_date(sessionID, new Date().toString());
        server.create_bug_report(sessionID, "testing", "target");
        TestGitRepository.deleteTempFiles();
    }

    @Test
    public void instance() {
        Assert.assertEquals(ltc, LTC.getInstance());
    }
}
