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
import com.sri.ltc.categories.IntegrationTests;
import com.sri.ltc.filter.Author;
import com.sri.ltc.git.TemporaryGitRepository;
import com.sri.ltc.server.Session;
import com.sri.ltc.server.SessionManager;
import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.TrackedFile;
import com.sri.ltc.versioncontrol.VersionControlException;
import com.sri.ltc.versioncontrol.history.CompleteHistory;
import com.sri.ltc.versioncontrol.history.HistoryUnit;
import com.sri.ltc.versioncontrol.history.LimitedHistory;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for limiting by authors.
 *
 * @author linda
 */
@Category(IntegrationTests.class)
public final class TestSession {

    @ClassRule
    public static TemporaryGitRepository temporaryGitRepository = new TemporaryGitRepository();

    enum Authors {
        ANTON,
        BERTA,
        CARLOS;
        public Author toAuthor() {
            return new Author(this.name().charAt(0)+this.name().toLowerCase().substring(1), // capitalize name
                    this.name().toLowerCase()+"@test.com"); // generate email address
        }
    }

    private static Session session = null;
    private static final List<String> revisions = Lists.newArrayList();
    private static final List<Date> dates = Lists.newArrayList();

    @BeforeClass
    public static void prepareRepository() throws Exception {
        // create a commit history of file "foo.txt" that looks like (newest to oldest):
        // 8. Carlos
        // 7. Carlos
        // 6. Berta
        // 5. Berta
        // 4. Carlos
        // 3. Berta
        // 2. Anton
        // 1. Anton
        Commit commit;

        temporaryGitRepository.setAuthor(Authors.ANTON.toAuthor());
        TrackedFile trackedFile = temporaryGitRepository.createTestFileInRepository("foo", ".txt", "first version", true);
        commit = trackedFile.commit("first commit");
        revisions.add(commit.getId());
        dates.add(commit.getDate());
        Thread.sleep(1000);
        temporaryGitRepository.modifyTestFileInRepository(trackedFile, "second version", false);
        commit = trackedFile.commit("second commit");
        revisions.add(commit.getId());
        dates.add(commit.getDate());
        Thread.sleep(1000);

        temporaryGitRepository.setAuthor(Authors.BERTA.toAuthor());
        temporaryGitRepository.modifyTestFileInRepository(trackedFile, "third version", false);
        commit = trackedFile.commit("third commit");
        revisions.add(commit.getId());
        dates.add(commit.getDate());
        Thread.sleep(1000);

        temporaryGitRepository.setAuthor(Authors.CARLOS.toAuthor());
        temporaryGitRepository.modifyTestFileInRepository(trackedFile, "fourth version", false);
        commit = trackedFile.commit("fourth commit");
        revisions.add(commit.getId());
        dates.add(commit.getDate());
        Thread.sleep(1000);

        temporaryGitRepository.setAuthor(Authors.BERTA.toAuthor());
        temporaryGitRepository.modifyTestFileInRepository(trackedFile, "fifth version", false);
        commit = trackedFile.commit("fifth commit");
        revisions.add(commit.getId());
        dates.add(commit.getDate());
        Thread.sleep(1000);
        temporaryGitRepository.modifyTestFileInRepository(trackedFile, "sixth version", false);
        commit = trackedFile.commit("sixth commit");
        revisions.add(commit.getId());
        dates.add(commit.getDate());
        Thread.sleep(1000);

        temporaryGitRepository.setAuthor(Authors.CARLOS.toAuthor());
        temporaryGitRepository.modifyTestFileInRepository(trackedFile, "seventh version", false);
        commit = trackedFile.commit("seventh commit");
        revisions.add(commit.getId());
        dates.add(commit.getDate());
        Thread.sleep(1000);
        temporaryGitRepository.modifyTestFileInRepository(trackedFile, "eighth version", false);
        commit = trackedFile.commit("eighth commit");
        revisions.add(commit.getId());
        dates.add(commit.getDate());

        assertNotNull("tracked file is not null", trackedFile);
        assertTrue("Revisions and Dates have same length", revisions.size() == dates.size());

        session = SessionManager.getSession(SessionManager.createSession(trackedFile));
        assertNotNull("session is not null", session);
    }

    @Test
    public void testLimitingAuthors() throws Exception {
        List<HistoryUnit> units;

//        // as Anton and not collapsing
//        trackedFile.getRepository().setSelf(Authors.ANTON.toAuthor());
//        units = new LimitedHistory(trackedFile, null, null, false).getHistoryUnits();
//        assertEquals("as Anton and not collapsing", 7, units.size());
//
//        // as Anton and collapsing
//        units = new LimitedHistory(trackedFile, null, null, true).getHistoryUnits();
//        assertEquals("as Anton and collapsing", 5, units.size());
//
////        // as Anton and collapsing; limiting to Anton and Berta
////        units = new LimitedHistory(trackedFile,
////                null, null, true).getHistoryUnits();
////        assertEquals("as Anton and limiting to Anton and Berta", 2, units.size());
//
//        // as Carlos and not collapsing
//        trackedFile.getRepository().setSelf(Authors.CARLOS.toAuthor());
//        units = new LimitedHistory(trackedFile, null, null, false).getHistoryUnits();
//        assertEquals("as Carlos and not collapsing", 5, units.size());
//
//        // as Carlos and collapsing
//        units = new LimitedHistory(trackedFile, null, null, true).getHistoryUnits();
//        assertEquals("as Carlos and collapsing", 3, units.size());
//
////        // as Carlos and collapsing; limiting to Berta and Anton
////        units = new LimitedHistory(trackedFile,
////                null, null, true).getHistoryUnits();
////        assertEquals("as Carlos and limiting to Anton and Berta", 2, units.size());
//
////        // as Carlos and not collapsing; limiting to Berta and Anton
////        units = new LimitedHistory(trackedFile,
////                null, null, false).getHistoryUnits();
////        assertEquals("as Carlos and not collapsing; limiting to Anton and Berta", 5, units.size());
    }

    @Test
    public void testLimitingByRevision() throws Exception {
        List<HistoryUnit> units;

        // as Anton:
//        trackedFile.getRepository().setSelf(Authors.ANTON.toAuthor());
//
//        // as Anton, collapsing, and limited by rev #2
//        units = new LimitedHistory(trackedFile, null, revisions.get(1), true).getHistoryUnits();
//        assertEquals("as Anton, collapsing, and limited by rev #2", 5, units.size());
//        // as Anton, collapsing, and limited by rev #4
//        units = new LimitedHistory(trackedFile, null, revisions.get(3), true).getHistoryUnits();
//        assertEquals("as Anton, collapsing, and limited by rev #4", 4, units.size());
//        // as Anton, not collapsing, and limited by rev #4
//        units = new LimitedHistory(trackedFile, null, revisions.get(3), false).getHistoryUnits();
//        assertEquals("as Anton, not collapsing, and limited by rev #4", 6, units.size());
//
//        // as Carlos:
//        trackedFile.getRepository().setSelf(Authors.CARLOS.toAuthor());
//
//        // as Carlos, not collapsing, and limited by rev #3
//        units = new LimitedHistory(trackedFile, null, revisions.get(2), false).getHistoryUnits();
//        assertEquals("as Carlos, not collapsing, and limited by rev #3", 7, units.size());
//        // as Carlos, collapsing, and limited by rev #3
//        units = new LimitedHistory(trackedFile, null, revisions.get(2), true).getHistoryUnits();
//        assertEquals("as Carlos, collapsing, and limited by rev #3", 5, units.size());
    }

    @Test
    public void testLimitingByDate() throws Exception {
        List<HistoryUnit> units;

//        // as Anton:
//        trackedFile.getRepository().setSelf(Authors.ANTON.toAuthor());
//
//        // as Anton, collapsing, and limited by date #4
//        units = new LimitedHistory(trackedFile, dates.get(3).toString(), null, true).getHistoryUnits();
//        assertEquals("as Anton, collapsing, and limited by date #4", 4, units.size());
//        // as Anton, not collapsing, and limited by date #5
//        units = new LimitedHistory(trackedFile, dates.get(4).toString(), null, false).getHistoryUnits();
//        assertEquals("as Anton, not collapsing, and limited by date #5", 5, units.size());
//
//        // as Carlos:
//        trackedFile.getRepository().setSelf(Authors.CARLOS.toAuthor());
//
//        // as Carlos, not collapsing, and limited by date #7
//        units = new LimitedHistory(trackedFile, dates.get(6).toString(), null, false).getHistoryUnits();
//        assertEquals("as Carlos, not collapsing, and limited by date #7", 3, units.size());
//        // as Carlos, collapsing, and limited by date #5
//        units = new LimitedHistory(trackedFile, dates.get(4).toString(), null, true).getHistoryUnits();
//        assertEquals("as Carlos, collapsing, and limited by date #5", 3, units.size());
    }
}
