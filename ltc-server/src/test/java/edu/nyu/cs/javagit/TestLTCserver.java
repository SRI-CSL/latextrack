/**
 ************************ 80 columns *******************************************
 * TestLTCserver
 *
 * Created on 11/11/11.
 *
 * Copyright 2009-2010, SRI International.
 */
package edu.nyu.cs.javagit;

import com.sri.ltc.server.LTC;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author linda
 */
public final class TestLTCserver {

    @Before
    public void newline() {
        System.out.println();
    }

    @Test
    public void mainHelp() {
        LTC.main(new String[] {"-h"});
    }

    @Ignore
    public void mainLevel() {
        LTC.main(new String[] {"-l","FINE"});
    }

    @Test
    public void instance() {
        LTC ltc = LTC.getInstance();
        Assert.assertEquals(ltc, LTC.getInstance());
    }
}
