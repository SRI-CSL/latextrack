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
