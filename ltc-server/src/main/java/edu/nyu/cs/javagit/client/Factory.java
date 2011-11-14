/**
 ************************ 80 columns *******************************************
 * Factory
 *
 * Created on Sep 29, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package edu.nyu.cs.javagit.client;

import edu.nyu.cs.javagit.api.commands.*;
import edu.nyu.cs.javagit.client.cli.*;

/**
 * @author linda
 */
public final class Factory {

    public static IGitAdd createGitAdd() {
        return new CliGitAdd();
    }

    public static IGitBranch createGitBranch() {
        return new CliGitBranch();
    }

    public static IGitCheckout createGitCheckout() {
        return new CliGitCheckout();
    }

    public static IGitCommit createGitCommit() {
        return new CliGitCommit();
    }

    public static IGitConfig createGitConfig() {
        return new CliGitConfig();
    }

    public static IGitDiff createGitDiff() {
        return new CliGitDiff();
    }

    public static IGitGrep createGitGrep() {
        return new CliGitGrep();
    }

    public static IGitLog createGitLog() {
        return new CliGitLog();
    }

    public static IGitMv createGitMv() {
        return new CliGitMv();
    }

    public static IGitPull createGitPull() {
        return new CliGitPull();
    }
    
    public static IGitPush createGitPush() {
        return new CliGitPush();
    }

    public static IGitRemote createGitRemote() {
        return new CliGitRemote();
    }
    
    public static IGitReset createGitReset() {
        return new CliGitReset();
    }

    public static IGitRevert createGitRevert() {
        return new CliGitRevert();
    }

    public static IGitRevParse createGitRevParse() {
        return new CliGitRevParse();
    }

    public static IGitRm createGitRm() {
        return new CliGitRm();
    }

    public static IGitShow createGitShow() {
        return new CliGitShow();
    }

    public static IGitStatus createGitStatus() {
        return new CliGitStatus();
    }
}
