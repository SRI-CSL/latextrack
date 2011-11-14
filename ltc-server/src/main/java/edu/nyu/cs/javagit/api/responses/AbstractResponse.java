/**
 ************************ 80 columns *******************************************
 * AbstractResponse
 *
 * Created on Sep 29, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package edu.nyu.cs.javagit.api.responses;

/**
 * @author linda
 */
public abstract class AbstractResponse implements CommandResponse {

    private StringBuffer error = new StringBuffer();
    private StringBuffer output = new StringBuffer();
    private int exitCode = 0;

    public final int getExitCode() {
        return exitCode;
    }

    public final void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public void addError(String s) {
        error.append(s);
    }

    @Override
    public boolean isError() {
        return error.length() > 0;
    }

    @Override
    public String getError() {
        return error.toString();
    }

    @Override
    public void addOutput(String s) {
        output.append(s);
    }

    @Override
    public String getOutput() {
        return output.toString();
    }
}
