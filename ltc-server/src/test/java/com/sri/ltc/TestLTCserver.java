/**
 ************************ 80 columns *******************************************
 * TestLTCserver
 *
 * Created on 11/11/11.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc;

import com.sri.ltc.categories.IntegrationTests;
import com.sri.ltc.server.LTC;
import org.junit.*;
import org.junit.experimental.categories.Category;

/**
 * @author linda
 */
@Category(IntegrationTests.class)
public final class TestLTCserver {

    private static LTC ltc;
    static {
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
    public void instance() {
        Assert.assertEquals(ltc, LTC.getInstance());
    }
}
