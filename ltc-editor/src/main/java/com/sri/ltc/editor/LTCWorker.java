package com.sri.ltc.editor;

/*
 * #%L
 * LaTeX Track Changes (LTC) allows collaborators on a version-controlled LaTeX writing project to view and query changes in the .tex documents.
 * %%
 * Copyright (C) 2009 - 2012 SRI International
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.sri.ltc.ProgressReceiver;
import com.sri.ltc.server.LTCserverImpl;
import com.sri.ltc.server.LTCserverInterface;
import org.apache.xmlrpc.XmlRpcException;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Exception handling modeled after jfpoilpret's answer (not the accepted one!) in:
 *   http://stackoverflow.com/questions/6523623/gracefull-exception-handling-in-swing-worker
 *
 * @author linda
 */
public abstract class LTCWorker<T,V> extends SwingWorker<T,V> implements ProgressReceiver {

    protected final Logger LOGGER = Logger.getLogger(LTCWorker.class.getName());
    protected final LTCserverInterface LTC = new LTCserverImpl(this);
    protected final int sessionID;

    protected LTCWorker(JFrame frame, int sessionID, String progressTitle, String progressText, boolean withCancel) {
        this.sessionID = sessionID;
        // initialize progress
        ProgressDialog.showDialog(frame, progressTitle, progressText, withCancel?this:null);
        addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                if ("progress".equals(e.getPropertyName()))
                    ProgressDialog.setProgress((Integer) e.getNewValue());
                if ("state".equals(e.getPropertyName()) && StateValue.DONE.equals(e.getNewValue()))
                    ProgressDialog.done(); // also covering any exceptions occurring here as well!
            }
        });
    }

    @Override
    protected final T doInBackground() throws Exception {
        T result = null;
        try {
            result = callLTCinBackground();
        } catch (XmlRpcException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        }
        return result;
    }

    protected abstract T callLTCinBackground() throws XmlRpcException;

    @Override
    public final void updateProgress(int progress) {
        setProgress(progress);
    }
}
