/**
 ************************ 80 columns *******************************************
 * FileHistory
 *
 * Created on Jul 24, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.versioncontrol.history;

import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.TrackedFile;
import com.sri.ltc.versioncontrol.VersionControlException;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * @author linda
 */
public final class CompleteHistory extends FileHistory {

    public CompleteHistory(TrackedFile file) throws IOException, ParseException, VersionControlException {
        super(file);
        update();
    }

    @Override
    List<Commit> updateCommits() throws VersionControlException, IOException {
        // perform git log with static options
        return trackedFile.getCommits();
    }

    @Override
    void transformGraph() {
        // nothing to prune
    }

    @Override
    void transformList() {
        // nothing to remove
    }
}
