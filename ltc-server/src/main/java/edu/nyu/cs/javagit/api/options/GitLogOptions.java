/*
 * ====================================================================
 * Copyright (c) 2008 JavaGit Project.  All rights reserved.
 *
 * This software is licensed using the GNU LGPL v2.1 license.  A copy
 * of the license is included with the distribution of this source
 * code in the LICENSE.txt file.  The text of the license can also
 * be obtained at:
 *
 *   http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *
 * For more information on the JavaGit project, see:
 *
 *   http://www.javagit.com
 * ====================================================================
 */
package edu.nyu.cs.javagit.api.options;

public class GitLogOptions {

    // general options
    private boolean optBreakRewriteChanges = false;
    private boolean optDetectRenames = false;
    private boolean optFindCopies = false;
    private boolean optFindCopiesHarder = false;
    private boolean optFileDetails = false;
    private String optRelativePath = null;
    private String optSince = null;
    private String optUntil = null;

    // commit limiting options
    private int optLimitMax = 0;
    private int optLimitSkip = 0;
    private String optLimitSince = null;
    private String optLimitAfter = null;
    private String optLimitUntil = null;
    private String optLimitBefore = null;
    private String optLimitAuthor = null;
    private String optLimitCommitter = null;
    private String optLimitGrep = null;
    private boolean optMatchIgnoreCase = false;
    private boolean optEnableExtendedRegex = false;
    private boolean optEnableFixedStrings = false;
    private boolean optRemoveEmpty = false;
    private boolean optMerges = false;
    private boolean optNoMerges = false;
    private boolean optFirstParent = false;
    private boolean optAll = false;
    private boolean optCherryPick = false;

    // commit ordering options
    private boolean optOrderingTopological = false;
    private boolean optOrderingDate = false;
    private boolean optOrderingReverse = false;

    // formatting options
    private boolean optGraph = false;
    private DateFormats optFormatDate = null;

    private static enum DateFormats {
        RELATIVE, LOCAL, DEFAULT, ISO8601, RFC2822, SHORT, RAW
    }

    ; // must be turned to lower case!
    private String optFormat = null;

    // history simplification
    private boolean optLimitFullHistory = false;


    // --- general options ---

    public boolean isOptBreakRewriteChanges() {
        return optBreakRewriteChanges;
    }

    public void setOptBreakRewriteChanges(boolean value) {
        this.optBreakRewriteChanges = value;
    }

    public boolean isOptDetectRenames() {
        return optDetectRenames;
    }

    public void setOptDetectRenames(boolean value) {
        this.optDetectRenames = value;
    }

    public boolean isOptFindCopies() {
        return optFindCopies;
    }

    public void setOptFindCopies(boolean value) {
        this.optFindCopies = value;
    }

    public boolean isOptFindCopiesHarder() {
        return optFindCopiesHarder;
    }

    public void setOptFindCopiesHarder(boolean value) {
        this.optFindCopiesHarder = value;
    }

    public boolean isOptFileDetails() {
        return optFileDetails;
    }

    public void setOptFileDetails(boolean value) {
        this.optFileDetails = value;
    }

    public boolean isOptRelativePath() {
        return optRelativePath != null;
    }

    public String getOptRelativePath() {
        return optRelativePath;
    }

    public void setOptRelativePath(String relativePath) {
        this.optRelativePath = relativePath;
    }

    public boolean isOptSince() {
        return optSince != null;
    }

    public String getOptSince() {
        return optSince;
    }

    public void setOptSince(String since) {
        this.optSince = since;
    }

    public boolean isOptUntil() {
        return optUntil != null;
    }

    public String getOptUntil() {
        return optUntil;
    }

    public void setOptUntil(String until) {
        this.optUntil = until;
    }

    // --- commit limiting options ---

    public int getOptLimitMax() {
        return optLimitMax;
    }

    public boolean isOptLimitMax() {
        return optLimitMax > 0;
    }

    public void setOptLimitMax(int limit) {
        this.optLimitMax = limit;
    }

    public boolean isOptLimitSkip() {
        return optLimitSkip > 0;
    }

    public void setOptLimitSkip(int limit) {
        this.optLimitSkip = limit;
    }

    public int getOptLimitSkip() {
        return optLimitSkip;
    }

    public boolean isOptLimitSince() {
        return optLimitSince != null;
    }

    public void setOptLimitSince(String since) {
        this.optLimitSince = since;
    }

    public String getOptLimitSince() {
        return optLimitSince;
    }

    public boolean isOptLimitAfter() {
        return optLimitAfter != null;
    }

    public void setOptLimitAfter(String after) {
        this.optLimitAfter = after;
    }

    public String getOptLimitAfter() {
        return optLimitAfter;
    }

    public boolean isOptLimitUntil() {
        return optLimitUntil != null;
    }

    public void setOptLimitUntil(String until) {
        this.optLimitUntil = until;
    }

    public String getOptLimitUntil() {
        return optLimitUntil;
    }

    public boolean isOptLimitBefore() {
        return optLimitBefore != null;
    }

    public void setOptLimitBefore(String before) {
        this.optLimitBefore = before;
    }

    public String getOptLimitBefore() {
        return optLimitBefore;
    }

    public boolean isOptLimitAuthor() {
        return optLimitAuthor != null;
    }

    public void setOptLimitAuthor(String pattern) {
        this.optLimitAuthor = pattern;
    }

    public String getOptLimitAuthor() {
        return optLimitAuthor;
    }

    public boolean isOptLimitCommitter() {
        return optLimitCommitter != null;
    }

    public void setOptLimitCommitter(String pattern) {
        this.optLimitCommitter = pattern;
    }

    public String getOptLimitCommitter() {
        return optLimitCommitter;
    }

    public boolean isOptLimitGrep() {
        return optLimitGrep != null;
    }

    public void setOptLimitGrep(String pattern) {
        this.optLimitGrep = pattern;
    }

    public String getOptLimitGrep() {
        return optLimitGrep;
    }

    public boolean isOptMatchIgnoreCase() {
        return optMatchIgnoreCase;
    }

    public void setOptMatchIgnoreCase(boolean value) {
        this.optMatchIgnoreCase = value;
    }

    public boolean isOptEnableExtendedRegex() {
        return optEnableExtendedRegex;
    }

    public void setOptEnableExtendedRegex(boolean value) {
        this.optEnableExtendedRegex = value;
    }

    public boolean isOptEnableFixedStrings() {
        return optEnableFixedStrings;
    }

    public void setOptEnableFixedStrings(boolean value) {
        this.optEnableFixedStrings = value;
    }

    public boolean isOptRemoveEmpty() {
        return optRemoveEmpty;
    }

    public void setOptRemoveEmpty(boolean value) {
        this.optRemoveEmpty = value;
    }

    public boolean isOptMerges() {
        return optMerges;
    }

    public void setOptMerges(boolean value) {
        this.optMerges = value;
    }

    public boolean isOptNoMerges() {
        return optNoMerges;
    }

    public void setOptNoMerges(boolean value) {
        this.optNoMerges = value;
    }

    public boolean isOptFirstParent() {
        return optFirstParent;
    }

    public void setOptFirstParent(boolean value) {
        this.optFirstParent = value;
    }

    public boolean isOptAll() {
        return optAll;
    }

    public void setOptAll(boolean value) {
        this.optAll = value;
    }

    public boolean isOptCherryPick() {
        return optCherryPick;
    }

    public void setOptCherryPick(boolean value) {
        this.optCherryPick = value;
    }

    // --- commit ordering options ---

    public boolean isOptOrderingTopological() {
        return optOrderingTopological;
    }

    public void setOptOrderingTopological(boolean value) {
        this.optOrderingTopological = value;
    }

    public boolean isOptOrderingDate() {
        return optOrderingDate;
    }

    public void setOptOrderingDate(boolean value) {
        this.optOrderingDate = value;
    }

    public boolean isOptOrderingReverse() {
        return optOrderingReverse;
    }

    public void setOptOrderingReverse(boolean value) {
        this.optOrderingReverse = value;
    }

    // --- formatting options ---


    public boolean isOptGraph() {
        return optGraph;
    }

    public void setOptGraph(boolean value) {
        this.optGraph = value;
    }

    public boolean setOptFormatDate(String optFormatDate) {
        if (optFormatDate == null)
            this.optFormatDate = null;
        else
            try {
                this.optFormatDate = DateFormats.valueOf(optFormatDate.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.optFormatDate = null;
                return false;
            }
        return true;
    }

    public String getOptFormatDate() {
        return optFormatDate.name().toLowerCase();
    }

    public boolean isOptFormatDate() {
        return optFormatDate != null;
    }

    public boolean isOptFormat() {
        return optFormat != null;
    }

    public String getOptFormat() {
        return optFormat;
    }

    public void setOptFormat(String format) {
        this.optFormat = format;
    }

    // --- history simplification ---

    public boolean isOptLimitFullHistory() {
        return optLimitFullHistory;
    }

    public void setOptLimitFullHistory(boolean value) {
        this.optLimitFullHistory = value;
    }
}