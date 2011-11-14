/**
 ************************ 80 columns *******************************************
 * GitConfigResponse
 *
 * Created on Jul 27, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package edu.nyu.cs.javagit.api.responses;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates response codes (in case of errors) of the <code>git config</code> command.
 *
 * @author linda
 */
public final class GitConfigResponse extends AbstractResponse {

    private String errorMessage = "";
    private int errorCode = 0;
    private final List<String> output = new ArrayList<String>();

    public void setErrorDetails(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void addOutputEntry(String string) {
        output.add(string);
    }

    public String getOutputEntry(int index) {
        return output.get(index);
    }
}
