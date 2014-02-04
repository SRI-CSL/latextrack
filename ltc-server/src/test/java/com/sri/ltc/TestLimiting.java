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
import com.google.common.collect.Sets;
import com.sri.ltc.categories.IntegrationTests;
import com.sri.ltc.filter.Author;
import com.sri.ltc.git.TemporaryGitRepository;
import com.sri.ltc.server.LTCserverImpl;
import com.sri.ltc.server.LTCserverInterface;
import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.TrackedFile;
import org.apache.commons.codec.binary.Base64;
import org.apache.xmlrpc.XmlRpcException;
import org.junit.*;
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
        DOROTHEE;
        public Author toAuthor() {
            return new Author(this.name().charAt(0)+this.name().toLowerCase().substring(1), // capitalize name
                    this.name().toLowerCase()+"@test.com"); // generate email address
        }
    }
    private final static String[] fileTexts = new String[] {
            "some contents",             // BERTA
            "more and more contents\n",  // CARLOS
            "\n\nand another round\n",   // BERTA
            "is this the final text?\n", // BERTA
            "not the final text\n",      // ANTON
            "this is the final text\n"   // DOROTHEE
    };
    private final static Authors[] AUTHORS = new Authors[] {
            Authors.ANTON, Authors.BERTA, Authors.CARLOS, Authors.BERTA, Authors.BERTA, Authors.ANTON, Authors.DOROTHEE};
    private static Author ORIGINAL_SELF;
    private static boolean COLLAPSE_AUTHORS;
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
            Thread.sleep(3000); // three seconds
            Author author = AUTHORS[i+1].toAuthor(); // start with second author
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

        // save current setting for collapsing authors for later:
        ORIGINAL_SELF = Author.fromList(API.get_self(sessionID));
        COLLAPSE_AUTHORS = API.get_bool_pref(LTCserverInterface.BoolPrefs.COLLAPSE_AUTHORS.name());
        API.set_bool_pref(LTCserverInterface.BoolPrefs.COLLAPSE_AUTHORS.name(), false); // start without collapsing
    }

    @AfterClass
    public static void resetPrefs() throws XmlRpcException {
        API.set_bool_pref(LTCserverInterface.BoolPrefs.COLLAPSE_AUTHORS.name(), COLLAPSE_AUTHORS);
    }

    @Before
    public void outputNewline() throws XmlRpcException {
        assertTrue("resetting limit date", API.reset_limited_date(sessionID) == 0);
        assertTrue("resetting limit rev", API.reset_limited_rev(sessionID) == 0);
        assertTrue("resetting authors", API.reset_limited_authors(sessionID) == 0);
        assertEquals("back to original self", ORIGINAL_SELF,
                Author.fromList(API.set_self(sessionID, ORIGINAL_SELF.name, ORIGINAL_SELF.email)));
        System.out.println();
    }

    class ListsNString {
        public final List<String> strings;
        public final String text;
        public final List<Integer> integers;
        ListsNString(List<String> strings, String text, List<Integer> integers) {
            this.strings = strings;
            this.text = text;
            this.integers = integers;
        }
    }

    @SuppressWarnings("unchecked")
    private ListsNString getRevs() throws XmlRpcException {
        Map map = API.get_changes(sessionID, false, Base64.encodeBase64(fileTexts[fileTexts.length - 1].getBytes()), null, 0);
        ListsNString result = new ListsNString(
                (List<String>) map.get(LTCserverInterface.KEY_REVS),
                (String) map.get(LTCserverInterface.KEY_LAST),
                (List<Integer>) map.get(LTCserverInterface.KEY_REV_INDICES));
        // print stuff:
        System.out.println(" last revision: "+result.text.substring(0,7));
        System.out.println(" consecutive revisions:");
        for (String rev : result.strings)
            System.out.println("  "+rev.substring(0,7));
        System.out.print(" active indices: ");
        for (Integer index : result.integers)
            System.out.print("" + index + " ");
        outputNewline();
        return result;
    }

    @Test
    public void nameOfVersionControl() throws XmlRpcException {
        assertEquals("name of VCS is GIT",
                LTCserverInterface.VersionControlSystems.GIT.name(),
                API.get_VCS(sessionID));
    }

    @Test
    public void limitToExact() throws XmlRpcException {
        if (commits.size() < 3)
            fail("cannot test with too few commits");
        {
            // set limiting date to 1st commit:
            String dateOfFirstCommit = CommonUtils.serializeDate(commitDates.get(commits.get(commits.size() - 1)));
            String limitingDate = API.set_limited_date(sessionID, dateOfFirstCommit);
            System.out.println(" limited to date: "+limitingDate);
            assertEquals("dates are equal", dateOfFirstCommit, limitingDate);

            // now get active revisions: should be one less than before
            ListsNString revs = getRevs();
            assertEquals("last rev is the first commit", commits.get(commits.size() - 1), revs.text);
            assertEquals("one fewer revision", commits.size() - 1, revs.strings.size());
        }
        {
            // set limiting date to 3rd commit:
            String dateOfThirdCommit = CommonUtils.serializeDate(commitDates.get(commits.get(commits.size() - 3)));
            String limitingDate = API.set_limited_date(sessionID, dateOfThirdCommit);
            System.out.println(" limited to date: "+limitingDate);
            assertEquals("dates are equal", dateOfThirdCommit, limitingDate);

            // now get active revisions: should be one less than before
            ListsNString revs = getRevs();
            assertEquals("last rev is the second commit", commits.get(commits.size() - 2), revs.text);
            assertEquals("two fewer revisions", commits.size() - 2, revs.strings.size());
        }
    }

    @Test
    public void limitToInBetween() throws XmlRpcException {
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
        ListsNString revs = getRevs();
        assertEquals("last rev is the third commit", commits.get(commits.size() - 3), revs.text);
        assertEquals("three fewer revisions", commits.size() - 3, revs.strings.size());
    }

    @Test
    public void limitByRevision() throws XmlRpcException {
        // set limiting rev to 3rd commit
        if (commits.size() < 3)
            fail("cannot test with too few commits");
        String rev = commits.get(commits.size() - 3).substring(0,6);
        System.out.println(" limited to rev: "+rev);
        assertEquals("revision string", rev, API.set_limited_rev(sessionID, rev));

        // now get active revisions: should be two less than before
        ListsNString revs = getRevs();
        assertEquals("limiting rev is first active", rev, revs.strings.get(0).substring(0, 6));
        assertEquals("last rev is one before limiting", commits.get(commits.size() - 2), revs.text);
        assertEquals("two fewer revisions", commits.size() - 2, revs.strings.size());
    }

    @Test
    public void limitByAuthors() throws XmlRpcException {
        {
            // self = ANTON, limit to ANTON and DOROTHEE
            Author self = Authors.ANTON.toAuthor();
            assertEquals("ANTON is self", Authors.ANTON.toAuthor(), Author.fromList(API.set_self(sessionID, self.name, self.email)));
            API.set_limited_authors(sessionID, Lists.newArrayList(
                    Authors.ANTON.toAuthor().asList(),
                    Authors.DOROTHEE.toAuthor().asList()));

            ListsNString revs = getRevs();
            assertTrue("only 1 consecutive revision", revs.strings.size() == 1);
            assertEquals("last revision is sixth", commits.get(commits.size() - 6), revs.text);
            assertEquals("only 1 active revision with index 0", Lists.newArrayList(new Integer(0)), revs.integers);
        }

        {
            // self = CARLOS, limit to ANTON and CARLOS
            Author self = Authors.CARLOS.toAuthor();
            assertEquals("CARLOS is self", Authors.CARLOS.toAuthor(), Author.fromList(API.set_self(sessionID, self.name, self.email)));
            API.set_limited_authors(sessionID, Lists.newArrayList(
                    Authors.ANTON.toAuthor().asList(),
                    Authors.CARLOS.toAuthor().asList()));

            ListsNString revs = getRevs();
            assertTrue("4 consecutive revision", revs.strings.size() == 4);
            assertEquals("last revision is fifth (moves up due to ignored authors)", commits.get(commits.size() - 5), revs.text);
            assertEquals("only 1 active revision with index 2", Lists.newArrayList(new Integer(2)), revs.integers);
        }

        {
            // self = DOROTHEE, limit to ANTON and CARLOS
            Author self = Authors.DOROTHEE.toAuthor();
            assertEquals("DOROTHEE is self", Authors.DOROTHEE.toAuthor(), Author.fromList(API.set_self(sessionID, self.name, self.email)));
            API.set_limited_authors(sessionID, Lists.newArrayList(
                    Authors.ANTON.toAuthor().asList(),
                    Authors.CARLOS.toAuthor().asList()));

            ListsNString revs = getRevs();
            assertTrue("6 consecutive revision", revs.strings.size() == 6);
            assertEquals("last revision is second (moves up due to ignored authors)", commits.get(commits.size() - 2), revs.text);
            assertEquals("active revisions are {1, 4}",
                    Sets.newHashSet(1, 4),
                    Sets.newHashSet(revs.integers));
        }
    }

    @Test
    public void limitByAuthorsCollapsing() throws XmlRpcException {
        API.set_bool_pref(LTCserverInterface.BoolPrefs.COLLAPSE_AUTHORS.name(), true);

        {
            // self = CARLOS, limit to ANTON and BERTA
            Author self = Authors.CARLOS.toAuthor();
            assertEquals("CARLOS is self", Authors.CARLOS.toAuthor(), Author.fromList(API.set_self(sessionID, self.name, self.email)));
            API.set_limited_authors(sessionID, Lists.newArrayList(
                    Authors.ANTON.toAuthor().asList(),
                    Authors.BERTA.toAuthor().asList()));

            ListsNString revs = getRevs();
            assertTrue("3 consecutive revision", revs.strings.size() == 3); // one is collapsed
            assertEquals("last revision is third", commits.get(commits.size() - 3), revs.text);
            assertEquals("active revisions are {0, 1}",
                    Sets.newHashSet(0, 1),
                    Sets.newHashSet(revs.integers));
        }

        {
            // self = DOROTHEE, limit to DOROTHEE and BERTA
            Author self = Authors.DOROTHEE.toAuthor();
            assertEquals("DOROTHEE is self", Authors.DOROTHEE.toAuthor(), Author.fromList(API.set_self(sessionID, self.name, self.email)));
            API.set_limited_authors(sessionID, Lists.newArrayList(
                    Authors.DOROTHEE.toAuthor().asList(),
                    Authors.BERTA.toAuthor().asList()));

            ListsNString revs = getRevs();
            assertTrue("5 consecutive revision", revs.strings.size() == 5);
            assertEquals("last revision is first one", commits.get(commits.size() - 1), revs.text);
            assertEquals("active revisions are {0, 2, 4}",
                    Sets.newHashSet(0, 2, 4),
                    Sets.newHashSet(revs.integers));
        }

        API.set_bool_pref(LTCserverInterface.BoolPrefs.COLLAPSE_AUTHORS.name(), false);
    }
}
