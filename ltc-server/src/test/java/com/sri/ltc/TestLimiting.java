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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sri.ltc.categories.IntegrationTests;
import com.sri.ltc.filter.Author;
import com.sri.ltc.git.TemporaryGitRepository;
import com.sri.ltc.server.LTCserverImpl;
import com.sri.ltc.server.LTCserverInterface;
import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.TrackedFile;
import org.apache.xmlrpc.XmlRpcException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests about limiting history via date or revision.
 *
 * Includes unit test for issue #27:
 * wrong history (one or two commits too many) when specifying dates in between commit dates.
 *
 * @author linda
 */
@Category(IntegrationTests.class)
public final class TestLimiting {

    @ClassRule
    public static TemporaryGitRepository temporaryGitRepository = new TemporaryGitRepository();

    enum Authors {
        ANTON,
        BERTA,
        CARLOS,
        DOROTHEE,
        EMIL,
        FELICITAS;
        public Author toAuthor() {
            return new Author(this.name().charAt(0)+this.name().toLowerCase().substring(1), // capitalize name
                    this.name().toLowerCase()+"@test.com"); // generate email address
        }
    }
    private final static String[] fileTexts = new String[] {
            "some contents",
            "more and more contents\n",
            "\n\nand another round\n",
            "is this the final text?\n"
    };
    private final static LTCserverInterface API = new LTCserverImpl();
    private static int sessionID = -1;
    private static List<String> commits = Lists.newArrayList();
    private static Map<String,Date> commitDates = Maps.newHashMap();

    private static void commit(TrackedFile trackedFile, String message) throws Exception {
        Commit c = trackedFile.commit(message);
        System.out.println(" -- commit ("+c.getId().substring(0,7)+"): "+message+
                " @ "+CommonUtils.serializeDate(c.getDate())+
                " by "+c.getAuthor());
    }

    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void prepareRepository() throws Exception {
        // create a commit history of file "foo.txt" that has up to 5 commits with 5 seconds in between
        // need different author for each commit to prevent filtering by author
        temporaryGitRepository.setAuthor(Authors.ANTON.toAuthor());
        TrackedFile trackedFile = null;
        trackedFile = temporaryGitRepository.createTestFileInRepository("foo", ".txt", "first version", true);
        commit(trackedFile, "1. commit");

        for (int i = 0; i < fileTexts.length; i++) {
            Thread.sleep(5000); // five seconds
            Author author = Authors.values()[i+1].toAuthor();  // start with second author
            temporaryGitRepository.setAuthor(author);
            temporaryGitRepository.modifyTestFileInRepository(trackedFile, fileTexts[i], false);
            commit(trackedFile, (i + 2) + ". commit");
        }

        // start session
        sessionID = API.init_session(trackedFile.getFile().getPath());
        assertTrue("session ID is valid", sessionID > 0);

        // obtain list of commits:
        for (Object[] commit : (List<Object[]>) API.get_commits(sessionID)) {
            if (commit == null || commit.length != 6)
                fail("cannot parse commit");
            commits.add(commit[0].toString());
            commitDates.put(commit[0].toString(), CommonUtils.deSerializeDate(commit[4].toString()));
        }
        assertEquals("number of commits", fileTexts.length+1, commits.size());
        assertEquals("number of dates", fileTexts.length+1, commitDates.size());
    }

    @Before
    public void outputNewline() {
        System.out.println();
    }

    @SuppressWarnings("unchecked")
    private List<String> getRevs() throws XmlRpcException {
        Map map = API.get_changes(sessionID, false, fileTexts[fileTexts.length-1], null, 0);
        List<String> revs = (List<String>) map.get(LTCserverInterface.KEY_REVS);
        // print revisions:
        System.out.println(" active revisions:");
        for (String rev : revs)
            System.out.println("  "+rev.substring(0,7));
        return revs;
    }

    @Test
    public void limitToExact() throws XmlRpcException {
        assertTrue("resetting limit date", API.reset_limited_date(sessionID) == 0);
        assertTrue("resetting limit rev", API.reset_limited_rev(sessionID) == 0);

        // set limiting date to 2nd to last commit:
        String dateOfSecondCommit = CommonUtils.serializeDate(commitDates.get(commits.get(commits.size() - 2)));
        String limitingDate = API.set_limited_date(sessionID, dateOfSecondCommit);
        System.out.println(" limited to date: "+limitingDate);
        assertEquals("dates are equal", dateOfSecondCommit, limitingDate);

        // now get active revisions: should be one less than before
        List<String> revs = getRevs();
        assertEquals("one fewer revision", commits.size()-1, revs.size());
    }

    @Test
    public void limitToInBetween() throws XmlRpcException {
        assertTrue("resetting limit date", API.reset_limited_date(sessionID) == 0);
        assertTrue("resetting limit rev", API.reset_limited_rev(sessionID) == 0);

        // set limiting date to something in between 3rd and 4th commit:
        if (commits.size() < 4)
            fail("cannot test with too few commits");
        Date dateOfThirdCommit = commitDates.get(commits.get(commits.size() - 3));
        Date dateOfFourthCommit = commitDates.get(commits.get(commits.size() - 4));
        assertFalse("dates are not equal", dateOfThirdCommit.equals(dateOfFourthCommit));
        assertTrue("third commit occurred before fourth", dateOfThirdCommit.before(dateOfFourthCommit));
        long offset = (dateOfFourthCommit.getTime() - dateOfThirdCommit.getTime())/2L;
        String limitingDate = API.set_limited_date(sessionID, CommonUtils.serializeDate(
                new Date(dateOfThirdCommit.getTime() + offset))); // in the middle between 3rd and 4th commit
        System.out.println(" limited to date: "+limitingDate);
        assertFalse("dates are not equal", limitingDate.equals(CommonUtils.serializeDate(dateOfThirdCommit)));

        // now get active revisions: should be two less than before
        List<String> revs = getRevs();
        assertEquals("three fewer revisions", commits.size()-3, revs.size());
    }

    @Test
    public void limitByRevision() throws XmlRpcException {
        assertTrue("resetting limit date", API.reset_limited_date(sessionID) == 0);
        assertTrue("resetting limit rev", API.reset_limited_rev(sessionID) == 0);

        // set limiting rev to 3rd commit
        if (commits.size() < 3)
            fail("cannot test with too few commits");
        String rev = commits.get(2).substring(0,6);
        System.out.println(" limited to rev: "+rev);
        assertEquals("revision string", rev, API.set_limited_rev(sessionID, rev));

        // now get active revisions: should be two less than before
        List<String> revs = getRevs();
        assertEquals("two fewer revisions", commits.size()-2, revs.size());
    }
}
