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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sri.ltc.categories.IntegrationTests;
import com.sri.ltc.filter.Author;
import com.sri.ltc.git.TemporaryGitRepository;
import com.sri.ltc.versioncontrol.TrackedFile;
import com.sri.ltc.versioncontrol.VersionControlException;
import com.sri.ltc.versioncontrol.history.CompleteHistory;
import com.sri.ltc.versioncontrol.history.LimitedHistory;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for file history and limiting.
 * @author linda
 */
@Category(IntegrationTests.class)
public final class TestFileHistory {

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

    private TrackedFile trackedFile = null;

    @Before
    public void prepareRepository() throws Exception {
        // create a commit history of file "foo.txt" that looks like (newest to oldest):
        // 8. Carlos
        // 7. Carlos
        // 6. Berta
        // 5. Berta
        // 4. Carlos
        // 3. Berta
        // 2. Anton
        // 1. Anton

        temporaryGitRepository.setAuthor(Authors.ANTON.toAuthor());
        trackedFile = temporaryGitRepository.createTestFileInRepository("foo", ".txt", "first version", true);
        trackedFile.commit("first commit");
        temporaryGitRepository.modifyTestFileInRepository(trackedFile, "second version", false);
        trackedFile.commit("second commit");

        temporaryGitRepository.setAuthor(Authors.BERTA.toAuthor());
        temporaryGitRepository.modifyTestFileInRepository(trackedFile, "third version", false);
        trackedFile.commit("third commit");

        temporaryGitRepository.setAuthor(Authors.CARLOS.toAuthor());
        temporaryGitRepository.modifyTestFileInRepository(trackedFile, "fourth version", false);
        trackedFile.commit("fourth commit");

        temporaryGitRepository.setAuthor(Authors.BERTA.toAuthor());
        temporaryGitRepository.modifyTestFileInRepository(trackedFile, "fifth version", false);
        trackedFile.commit("fifth commit");
        temporaryGitRepository.modifyTestFileInRepository(trackedFile, "sixth version", false);
        trackedFile.commit("sixth commit");

        temporaryGitRepository.setAuthor(Authors.CARLOS.toAuthor());
        temporaryGitRepository.modifyTestFileInRepository(trackedFile, "seventh version", false);
        trackedFile.commit("seventh commit");
        temporaryGitRepository.modifyTestFileInRepository(trackedFile, "eighth version", false);
        trackedFile.commit("eighth commit");
    }

    @Test
    public void testCompleteHistory() throws ParseException, VersionControlException, IOException {
        assertNotNull("tracked file is not null", trackedFile);
        CompleteHistory history = new CompleteHistory(trackedFile);

        assertEquals("history has 3 authors", 3, history.getAuthors().size());

        List<Object[]> commits = history.update();
        assertEquals("history has 8 commits", 8, commits.size());
    }

    @Test
    public void testLimitedHistory() throws Exception {
        assertNotNull("tracked file is not null", trackedFile);
        LimitedHistory history;

        // as Anton and not collapsing; no other limits
        trackedFile.getRepository().setSelf(Authors.ANTON.toAuthor());
        history = new LimitedHistory(trackedFile, null, null, null, false);
        assertEquals("as Anton and not collapsing", 7, history.getIDs().size());

        // as Anton and collapsing; no other limits
        history = new LimitedHistory(trackedFile, null, null, null, true);
        assertEquals("as Anton and collapsing", 5, history.getIDs().size());

        // as Anton and collapsing; limiting to Anton and Berta
        history = new LimitedHistory(trackedFile,
                Sets.newHashSet(Authors.ANTON.toAuthor(), Authors.BERTA.toAuthor()),
                null, null, true);
        assertEquals("as Anton and limiting to Anton and Berta", 2, history.getIDs().size());

        // as Carlos and not collapsing; no other limits
        trackedFile.getRepository().setSelf(Authors.CARLOS.toAuthor());
        history = new LimitedHistory(trackedFile, null, null, null, false);
        assertEquals("as Carlos and not collapsing", 5, history.getIDs().size());

        // as Carlos and collapsing; no other limits
        history = new LimitedHistory(trackedFile, null, null, null, true);
        assertEquals("as Carlos and collapsing", 3, history.getIDs().size());

        // as Carlos and collapsing; limiting to Berta and Anton
        history = new LimitedHistory(trackedFile,
                Sets.newHashSet(Authors.ANTON.toAuthor(), Authors.BERTA.toAuthor()),
                null, null, true);
        assertEquals("as Carlos and limiting to Anton and Berta", 2, history.getIDs().size());

        // as Carlos and not collapsing; limiting to Berta and Anton
        history = new LimitedHistory(trackedFile,
                Sets.newHashSet(Authors.ANTON.toAuthor(), Authors.BERTA.toAuthor()),
                null, null, false);
        assertEquals("as Carlos and not collapsing; limiting to Anton and Berta", 5, history.getIDs().size());
    }
}
