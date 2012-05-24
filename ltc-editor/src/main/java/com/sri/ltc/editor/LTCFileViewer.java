/**
 ************************ 80 columns *******************************************
 * LTCFileViewer
 *
 * Created on 5/24/12.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.editor;

/**
 * A non-editable version of LTC that uses a list of files instead of a GIT repository.
 *
 * @author linda
 */
public final class LTCFileViewer extends LTCGui {

    public LTCFileViewer() {
        super(false, "LTC File Viewer");
    }
}
