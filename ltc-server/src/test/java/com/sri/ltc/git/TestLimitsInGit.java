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
package com.sri.ltc.git;

import com.sri.ltc.CommonUtils;
import com.sri.ltc.categories.IntegrationTests;
import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.Remote;
import com.sri.ltc.versioncontrol.TrackedFile;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author linda
 */
@Category(IntegrationTests.class)
public class TestLimitsInGit {

    @ClassRule
    public static TemporaryClonedRepository clonedRepository = new TemporaryClonedRepository();

    @SuppressWarnings("unchecked")
    @Test
    public void testRemotes() {
        Set<Remote> remotes = clonedRepository.getRepository().getRemotes().get();
        assertTrue("set of remotes is not NULL", remotes != null);
        assertTrue("set of remotes has one element", remotes.size() == 1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCommits() {
        try {
            TrackedFile trackedFile = clonedRepository.getTrackedFile();
            assertTrue("tracked file is not NULL", trackedFile != null);

            List<Commit> commits = trackedFile.getCommits();
            assertTrue("list of commits is not NULL", commits != null);
            assertEquals("list of commits has length 6", 6, commits.size());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
    public void testLimits() {
        try {
            TrackedFile trackedFile = clonedRepository.getTrackedFile();
            assertTrue("tracked file is not NULL", trackedFile != null);
            List<Commit> commits;

            // TODO: test revision limit
            commits = trackedFile.getCommits(null, "36eeab06"); // this should return commits until revision 3!
            assertTrue("list of commits is not NULL", commits != null);
            assertEquals("list of commits has length 4", 4, commits.size());

            // TODO: test date limit: date somewhere between r5 and r4 => 3 commits
            commits = trackedFile.getCommits(CommonUtils.deSerializeDate("2010-07-23 13:15 -0500"), null);
            assertTrue("list of commits is not NULL", commits != null);
            assertEquals("list of commits has length 3", 3, commits.size());
            // TODO: test date limit: date exactly r4 => 4 commits
            commits = trackedFile.getCommits(CommonUtils.deSerializeDate("2010-07-23 13:12:42 -0500"), null);
            assertTrue("list of commits is not NULL", commits != null);
            assertEquals("list of commits has length 4", 4, commits.size());

            // TODO: test both limits at the same time; date later than revision, so date is significant
            commits = trackedFile.getCommits(CommonUtils.deSerializeDate("2010-07-23 13:10 -0500"), "bac2f515");
            assertTrue("list of commits is not NULL", commits != null);
            assertEquals("list of commits has length 5", 5, commits.size());
            // TODO: test both limits at the same time; date earlier than revision, so revision is significant
            commits = trackedFile.getCommits(CommonUtils.deSerializeDate("2010-07-23 13:10 -0500"), "203e0ce8");
            assertTrue("list of commits is not NULL", commits != null);
            assertEquals("list of commits has length 3", 3, commits.size());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
