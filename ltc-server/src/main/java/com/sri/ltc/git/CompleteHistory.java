/**
 ************************ 80 columns *******************************************
 * FileHistory
 *
 * Created on Jul 24, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.git;

import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.TrackedFile;

import java.util.List;

/**
 * @author linda
 */
public final class CompleteHistory extends FileHistory {

    public CompleteHistory(TrackedFile gitFile) throws Exception {
        super(gitFile);
        update();
    }

    @Override
    List<Commit> updateCommits() throws Exception {
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
