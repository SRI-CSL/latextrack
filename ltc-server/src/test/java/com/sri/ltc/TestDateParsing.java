/*
 * #%L
 * LaTeX Track Changes (LTC) allows collaborators on a version-controlled LaTeX writing project to view and query changes in the .tex documents.
 * %%
 * Copyright (C) 2009 - 2013 SRI International
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

import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author linda
 */
public final class TestDateParsing {

    @Test
    public void wellFormedDates() throws ParseException {
        for (String date : new String[] {
                "2012-07-23 20:12:42 +0200"
        })
            assertEquals(CommonUtils.FORMATTER.parse(date), CommonUtils.deSerializeDate(date));
    }

    private boolean inCentralTime(Date date, long epochStart) {
        // whether given date is in 24h time window from given epoch start
        long diffFromStart = epochStart - date.getTime();
        long diffFromEnd = epochStart + 86400000L - date.getTime();
        boolean result = (diffFromStart <= 0) && (0 <= diffFromEnd);
        if (!result)
            System.err.println("Diff from start = "+diffFromStart+"\nDiff from end = "+diffFromEnd);
        return result;
    }

    @Test
    public void naturalLanguageDates() throws ParseException {
        // use "Epoch dates for the start and end of the year/month/day" with local time
        // and add 1000 to each number in seconds at
        // http://www.epochconverter.com/
        long JulyTwentyThird = 1343019600000L;
        assertTrue(inCentralTime(CommonUtils.deSerializeDate("2012-07-23"), JulyTwentyThird));
        assertTrue(inCentralTime(CommonUtils.deSerializeDate("23 Jul 2012  1p"), JulyTwentyThird));
        assertTrue(inCentralTime(CommonUtils.deSerializeDate("Jul 23, 2012, 1a.m."), JulyTwentyThird));
        assertTrue(inCentralTime(CommonUtils.deSerializeDate("Jul 23 2012, 11:59p"), JulyTwentyThird));
    }

    @Test(expected = ParseException.class)
    public void badDate1() throws ParseException {
        CommonUtils.deSerializeDate("quatsch");
    }

    @Test(expected = ParseException.class)
    public void badDate2() throws ParseException {
        CommonUtils.deSerializeDate("");
    }

    @Test(expected = ParseException.class)
    public void badDate3() throws ParseException {
        CommonUtils.deSerializeDate("123");
    }
}
