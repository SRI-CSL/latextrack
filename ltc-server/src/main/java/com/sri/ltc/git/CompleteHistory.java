/**
 ************************ 80 columns *******************************************
 * FileHistory
 *
 * Created on Jul 24, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.git;

import edu.nyu.cs.javagit.api.GitFile;
import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.responses.GitLogResponse;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * @author linda
 */
public final class CompleteHistory extends FileHistory {

    public CompleteHistory(GitFile gitFile) throws IOException, ParseException, JavaGitException {
        super(gitFile);
        update();
    }

    @Override
    List<GitLogResponse.Commit> updateCommits() throws IOException, JavaGitException, ParseException {
        // perform git log with static options
        return getLog(options, "complete");
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
