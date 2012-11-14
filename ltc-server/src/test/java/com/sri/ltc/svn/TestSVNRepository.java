/**
 ************************ 80 columns *******************************************
 * TestSVNRepository
 *
 * Created on 11/14/12.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.svn;

import com.sri.ltc.versioncontrol.TrackedFile;
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author linda
 */
public class TestSVNRepository {
    @ClassRule
    public static TemporarySVNRepository temporarySVNRepository = new TemporarySVNRepository();

    @Test
    public void testUntracked() {
        assertTrue(temporarySVNRepository.getRoot().exists());

        try {
            TrackedFile trackedFile = temporarySVNRepository.getTrackedFile();
            assertTrue("tracked file is not NULL", trackedFile != null);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
