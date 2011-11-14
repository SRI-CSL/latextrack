/**
 ************************ 80 columns *******************************************
 * AbstractParser
 *
 * Created on Sep 29, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package edu.nyu.cs.javagit.client.cli;

import edu.nyu.cs.javagit.api.responses.AbstractResponse;

/**
 * @author linda
 */
public abstract class AbstractParser<T extends AbstractResponse> implements IParser<T> {

    private final static String NEWLINE = System.getProperty("line.separator");

    protected T response;

    protected AbstractParser(T response) {
        this.response = response;
    }

    @Override
    public final T getResponse() {
        return response;
    }

    @Override
    public void processExitCode(int code) {
        response.setExitCode(code);
        if (code != 0) {
            if (!response.isError()) // didn't catch error during parsing
                response.addError(response.getOutput()); // copy output to error message
            response.addError("(exit code = " + code + ")");
        }
    }

    @Override
    public void parseLine(String line, String lineending) {
        response.addOutput(line + (lineending==null?"":lineending));
    }
}
