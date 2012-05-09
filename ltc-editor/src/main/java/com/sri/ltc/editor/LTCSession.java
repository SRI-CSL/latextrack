/**
 ************************ 80 columns *******************************************
 * LTCSession
 *
 * Created on Oct 11, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.editor;

import com.sri.ltc.filter.Author;
import com.sri.ltc.server.LTCserverInterface;
import org.apache.xmlrpc.XmlRpcException;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

/**
 * Encapsulates an LTC session, which communicates with the LTC server.
 * This object is meant to be executed on Event Dispatching Thread but when
 * calling LTC API, it will execute those calls on their own worker threads.
 *
 * @author linda
 */
public class LTCSession {

    private final LTCEditor LTCEditor;

    private int ID = -1;
    private String canonicalPath = "";

    public LTCSession(LTCEditor LTCEditor) {
        this.LTCEditor = LTCEditor;
    }

    public String getCanonicalPath() {
        return isValid()?canonicalPath:""; // path only valid if ID is valid
    }

    public boolean isValid() {
        return ID != -1;
    }

    public void startInitAndUpdate(final File file,
                                   final String date, final String rev, final int caretPosition)
            throws IOException {
        canonicalPath = file.getCanonicalPath();

        // create new worker to init session
        (new LTCWorker<Integer,Void>(LTCEditor, ID,
                "Initializing...", "<html>Initializing track changes of file<br>"+canonicalPath+"</html>", true) {
            List<Object[]> authors = null;
            java.util.List<Object[]> commits = null;
            Object[] self = null;
            int sessionID = -1;

            @SuppressWarnings("unchecked")
            @Override
            protected Integer callLTCinBackground() throws XmlRpcException {
                setProgress(1);
                sessionID = LTC.init_session(file.getAbsolutePath());
                if (isCancelled()) return -1;
                setProgress(25);
                authors = LTC.get_authors(sessionID);
                if (isCancelled()) return -1;
                setProgress(50);
                commits = LTC.get_commits(sessionID);
                if (isCancelled()) return -1;
                setProgress(75);
                self = LTC.get_self(sessionID);
                if (isCancelled()) return -1;
                setProgress(100);
                return sessionID;
            }

            @Override
            protected void done() {
                if (isCancelled()) {
                    ID = -1;
                    LTCEditor.finishInit(null, null, null);
                } else
                    try {
                        ID = get();
                        LTCEditor.finishInit(authors, commits, self);
                        startUpdate(date, rev, false, "", Collections.<int[]>emptyList(), caretPosition);
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    } catch (ExecutionException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
            }
        }).execute();
    }

    public void close() {
        if (!isValid()) return;

        // create new worker to close session
        executeWorkerAndWait(new LTCWorker<Map,Void>(LTCEditor, ID,
                "Closing...", "<html>Closing track changes of file<br>"+getCanonicalPath()+"</html>", false) {
            @Override
            protected Map callLTCinBackground() throws XmlRpcException {
                return LTC.close_session(ID, "", Collections.emptyList(), 0); // forget about any modifications
            }

            @Override
            protected void done() {
                ID = -1;
                LTCEditor.finishClose();
            }
        });
    }

    public void startUpdate(final String date, final String rev,
                            final boolean isModified, final String currentText, final List<int[]> deletions, final int caretPosition) {
        if (!isValid()) return;

        // create new worker to update session
        // TODO: decide if and how to make get_changes interruptible in order to support cancel button
        (new LTCWorker<Map,Void>(LTCEditor, ID,
                "Updating...", "<html>Updating changes of<br>"+getCanonicalPath()+"</html>", false) {
            java.util.List<Object[]> commits = null;
            List<Object[]> remotes = null;

            @SuppressWarnings("unchecked")
            @Override
            protected Map callLTCinBackground() throws XmlRpcException {
                // update limited date and rev
                LTC.set_limited_date(sessionID, date);
                LTC.set_limited_rev(sessionID, rev);
                setProgress(1);
                if (isCancelled()) return null;
                // get changes
                Map map = LTC.get_changes(sessionID, isModified, currentText, deletions, caretPosition);
                setProgress(90);
                if (isCancelled()) return null;
                // update commit graph
                commits = LTC.get_commits(sessionID);
                setProgress(97);
                if (isCancelled()) return null;
                // update remotes
                remotes = LTC.get_remotes(sessionID);
                if (isCancelled()) return null;
                return map;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void done() {
                if (isCancelled()) {
                    // TODO: reset everything?
                } else
                    try {
                        Map map = get();
                        LTCEditor.finishUpdate(
                                (Map<Integer,Object[]>) map.get(LTCserverInterface.KEY_AUTHORS),
                                (String) map.get(LTCserverInterface.KEY_TEXT),
                                (java.util.List<Integer[]>) map.get(LTCserverInterface.KEY_STYLES),
                                (Integer) map.get(LTCserverInterface.KEY_CARET),
                                new HashSet<String>((List<String>) map.get(LTCserverInterface.KEY_SHA1)),
                                commits, remotes);
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    } catch (ExecutionException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
            }
        }).execute();
    }

    public void save(final String currentText, final List<int[]> deletions) {
        if (ID == -1) return;

        // create new worker to save file in session
        executeWorkerAndWait(new LTCWorker<Void,Void>(LTCEditor, ID,
                "Saving...", "<html>Saving file<br>"+getCanonicalPath()+"</html>", false) {
            @Override
            protected Void callLTCinBackground() throws XmlRpcException {
                LTC.save_file(ID, currentText, deletions);
                return null;
            }
        });
    }

    public void commit(final String message, final JButton saveButton) {
        if (!isValid()) return;

        // create new worker to set self in session
        (new LTCWorker<Integer,Void>(LTCEditor, ID,
                "Setting...", "Setting current self", false) {
            Object[] last_commit = null;

            @SuppressWarnings("unchecked")
            @Override
            protected Integer callLTCinBackground() throws XmlRpcException {
                int code = 0;
                try {
                    LTC.commit_file(ID, message);
                } catch (XmlRpcException e) {
                    code = e.code;
                }
                if (code == 0) {
                    setProgress(50);
                    java.util.List<Object[]> commits = LTC.get_commits(sessionID);
                    if (!commits.isEmpty())
                        last_commit = commits.get(0);
                }
                setProgress(100);
                return code;
            }

            @Override
            protected void done() {
                try {
                    int code = get();
                    if (code == 5) {
                        if (saveButton.isEnabled()) {
                            // dialog if nothing to commit && unsaved edits
                            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(LTCEditor,
                                    "Save file first before committing?",
                                    "Nothing to commit but unsaved edits",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.WARNING_MESSAGE)) {
                                saveButton.doClick();
                                commit(message, saveButton);
                            }
                        } else
                            JOptionPane.showMessageDialog(LTCEditor,
                                    "Nothing to commit.",
                                    "Nothing to commit",
                                    JOptionPane.INFORMATION_MESSAGE);
                    } else if (code == 0)
                        LTCEditor.finishCommit(last_commit);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                } catch (ExecutionException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }).execute();
    }

    public void setSelf(final Author self) {
        if (!isValid()) return;

        // create new worker to set self in session
        (new LTCWorker<Void,Void>(LTCEditor, ID,
                "Setting...", "Setting current self", false) {
            @Override
            protected Void callLTCinBackground() throws XmlRpcException {
                if (self == null)
                    LTC.reset_self(ID);
                else {
                    LTC.set_self(ID, self.name, self.email);
                }
                return null;
            }
        }).execute();
    }

    public void setLimitedAuthors(final boolean allLimited, final List<String[]> limitedAuthors) {
        if (!isValid()) return;

        // create new worker to set limited authors in session
        (new LTCWorker<Void,Void>(LTCEditor, ID,
                "Setting...", "Setting limited authors", false) {
            @Override
            protected Void callLTCinBackground() throws XmlRpcException {
                if (allLimited)
                    LTC.reset_limited_authors(ID);
                else
                    LTC.set_limited_authors(ID, limitedAuthors);
                return null;
            }
        }).execute();
    }

    public void getAuthors() {
        if (!isValid()) return;

        // create new worker to get authors in session
        (new LTCWorker<List,Void>(LTCEditor, ID,
                "Retrieving...", "Retrieving currently known authors", false) {
            @Override
            protected List callLTCinBackground() throws XmlRpcException {
                return LTC.get_authors(ID);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void done() {
                try {
                    LTCEditor.finishAuthors(get());
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                } catch (ExecutionException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }).execute();
    }

    public void updateRemote(final String name, final String url, final boolean removeFirst) {
        if (!isValid()) return;

        // create new worker to add remote in session
        (new LTCWorker<Void,Void>(LTCEditor, ID,
                "Updating Remote...", "Updating remote alias", false) {
            @Override
            protected Void callLTCinBackground() throws XmlRpcException {
                if (removeFirst)
                    LTC.rm_remote(ID, name);
                LTC.add_remote(ID, name, url);
                return null;
            }
        }).execute();
    }

    public void pullOrPush(final String repository, final boolean usePull,
                           final String date, final String rev,
                           final boolean isModified, final String currentText, final List<int[]> deletions, final int caretPosition) {
        if (!isValid()) return;

        // create new worker to add remote in session
        final String verb = (usePull?"Pulling":"Pushing");
        String preposition = (usePull?" from ":" to ");
        (new LTCWorker<String,Void>(LTCEditor, ID,
                verb+"...",
                verb+preposition+"remote repository\n"+repository, false) {
            @Override
            protected String callLTCinBackground() throws XmlRpcException {
                if (usePull)
                    return LTC.pull(ID, repository);
                else
                    return LTC.push(ID, repository);
            }

            @Override
            protected void done() {
                try {
                    String error = get();
                    if ("".equals(error))
                        startUpdate(date, rev, isModified, currentText, deletions, caretPosition);
                    else
                        JOptionPane.showMessageDialog(LTCEditor, error, verb+" Error", JOptionPane.ERROR_MESSAGE);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                } catch (ExecutionException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }).execute();
    }

    public void colors(final String name, final String email, final String color) {
        // if color == null then get otherwise set color

        // create new worker to set or get
        executeWorkerAndWait(new LTCWorker<String,Void>(LTCEditor, ID,
                "...", "getting or setting color", false) {
            @Override
            protected String callLTCinBackground() throws XmlRpcException {
                if (color == null) {
                    return LTC.get_color(name, email);
                } else {
                    LTC.set_color(name, email, color);
                    return null;
                }
            }

            @Override
            protected void done() {
                if (color == null)
                    try {
                        get(); // TODO: redirect to caller...
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    } catch (ExecutionException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
            }
        });
    }

    private void executeWorkerAndWait(final SwingWorker worker) {
        worker.execute();
        // yield thread and wait for worker to return
        do {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {}
        } while ((!worker.isDone()));
    }
}
