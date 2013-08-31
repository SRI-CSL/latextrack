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
package com.sri.ltc.server;

import org.apache.xmlrpc.XmlRpcException;

import java.util.List;
import java.util.Map;

/**
 * @author linda
 */
public interface LTCserverInterface {

    /** Local port for LTC server */
    public final static int PORT = 7777;

    /** Constants */
    public final static String KEY_TEXT = "text";
    public final static String KEY_STYLES = "styles";
    public final static String KEY_AUTHORS = "authors";
    public final static String KEY_CARET = "caret";
    public final static String KEY_REVS = "revs";
    public static enum BoolPrefs {SMALL, DELETIONS, PREAMBLE, COMMANDS, COMMENTS, COLLAPSE_AUTHORS, ALLOW_SIMILAR_COLORS};
    public final static String ON_DISK = "on disk"; // special name for version on disk (if file modified and not committed)
    public final static String MODIFIED = "modified"; // special name for text modified in editor
    public static enum VersionControlSystems {GIT, SVN};

    /**
     * Initialize a new track changes session with the base system.
     * The given path should point to the file being edited.  Relative
     * file names are allowed.
     *
     * @param path String containing the path to the file to be tracked
     * @return a session ID to be used in subsequent calls regarding this track session
     * @throws XmlRpcException <ul>
     *   <li>with error code = 2 if the given file is not readable.
     *   <li>with error code = 3 if the parent of the given file is not a git repository.
     *   <li>with error code = 4 if the given file is not being tracked under git.
     *   <li>with error code = 5 if an IOException occurred while traversing the file hierarchy.
     *   <li>with error code = 6 if a JavaGitException occurred during tracked file creation.
     *   <li>with error code = 7 if an IOException occurred during tracked file creation.
     *   <li>with error code = 8 if a ParseException occurred during session creation.
     *   <li>with error code = 10 if a VersionControlException occurred during session creation.
     *   <li>with error code = 11 if the indicated path denotes an already active tracked file
     * </ul>
     */
    public int init_session(String path) throws XmlRpcException;

    /**
     * Closes the session indicated by the given session identifier.
     * <p>
     * The call has to contain the current, raw text from the editor along with a list
     * of start and end positions, which indicate the occurrence of deletions in the
     * current text.  These are needed to properly convert the given caret position to
     * one in the newly computed text.
     * <p>
     * The return value contains the text without any changes under the key {@link #KEY_TEXT}.
     * The value under {@link #KEY_CARET} contains the cursor position transformed from
     * the one given as an argument to the method to the new text.
     *
     *
     * @param sessionID identifies the session
     * @param currentText current text in editor
     * @param deletions list of pairs with start and end position of deletions in <code>currentText</code> if any
     * @param caretPosition current cursor position to be transformed into new one
     * @return Map that contains the text without changes and the updated caret position
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 2 if a BadLocationException occurs while removing deletions from current text.
     * </ul>
     */
    public Map close_session(int sessionID, String currentText, List deletions, int caretPosition)
            throws XmlRpcException;

    /**
     * Save the current text (after removing any deletions given) to the file indicated
     * by the given session ID.
     *
     * @param sessionID identifies the session
     * @param currentText current text in editor
     * @param deletions list of pairs with start and end position of deletions in <code>currentText</code> if any
     * @return 0
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 2 if the given recent edits cannot be applied to last known text.
     *   <li>with error code = 3 if an IOException occurred during saving the file.
     *   <li>with error code = 4 if a FileNotFoundException occurred during saving the file.
     * </ul>
     */
    public int save_file(int sessionID, String currentText, List deletions) throws XmlRpcException;
    
    /**
     * Obtains the changes of the file indicated by the session ID.
     * The changes are limited to the currently set filter for this session and any
     * global preferences.
     * <p>
     * The call has to indicate whether the user has made any changes since the last save
     * operation.  It also needs the current, raw text from the editor along with a list
     * of start and end positions, which indicate the occurrence of deletions in the
     * current text.  These are needed to properly convert the given caret position to
     * one in the newly computed text.
     * <p>
     * The return value contains the text including the changes (for example, deletions)
     * under the key {@link #KEY_TEXT} and the list of styles to be used under the key
     * {@link #KEY_STYLES}.  Each style is a 5-tuple of numbers that denote start and
     * end position as well as style (1 - addition, 2 - deletion), an author index and
     * an index into the list of revisions as returned under key {@link #KEY_REVS}.
     * Start denotes the first character affected by this style, whereas the end position
     * is the first position of the next chunk (to be excluded).
     * <p>
     * Furthermore, the return value under {@link #KEY_AUTHORS} also contains a map of
     * numbers to authors and their color, which are given as an array of 3 Strings
     * containing name, email address, and color name.  If this map contains the key -1,
     * then this denotes the current author ("self"), which may have changed if
     * {@link BoolPrefs.ALLOW_SIMILAR_COLORS} is set to <code>false</code> and a new color
     * for the current author had to be found.
     * <p>
     * The value under {@link #KEY_CARET} contains the transformed cursor
     * position into the new text of the one given as an argument to the method.
     * <p>
     * Finally, an entry under {@link #KEY_REVS} in the returned map is a list of revision
     * names from newest to oldest that have been used to obtain the changes.
     *
     * @param sessionID identifies the session
     * @param isModified whether the text has been modified since the last save operation
     * @param currentText current text in editor (cannot be <code>null</code>)
     * @param deletions list of pairs with start and end position of deletions in <code>currentText</code> if any;
     *                  <code>null</code> or empty list if no deletions
     * @param caretPosition current cursor position to be transformed into new one (should be a valid position in
     *                      <code>currentText</code>)
     * @return Map that contains the text with changes, list of styles to be applied to
     * this text, map of indices to authors, updated caret position and lists of revision names
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 2 if an IOException occurred during change accumulation:.
     *   <li>with error code = 3 if a BadLocationException occurred during change accumulation.
     *   <li>with error code = 4 if a JavaGitException occurred during log retrieval.
     *   <li>with error code = 5 if an IOException occurred during log retrieval.
     *   <li>with error code = 6 if a ParseException occurred during log retrieval.
     *   <li>with error code = 7 if a BadLocationException occurs while removing deletions from current text.
     *   <li>with error code = 8 if a BackingStoreException occurs while dealing with author colors.
     * </ul>
     */
    public Map get_changes(int sessionID, boolean isModified, String currentText, List deletions, int caretPosition)
            throws XmlRpcException;

    /**
     * Obtain the name of the version control system that tracks the current file of the given session.
     *
     * @param sessionID identifies the session
     * @return String that denotes one of the possible version control systems as contained in
     *   {@link VersionControlSystems}
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     * </ul>
     */
    public String get_VCS(int sessionID) throws XmlRpcException;

    /**
     * Obtain all known authors for the file indicated by the session ID.  The returned
     * list contains authors given as a 3-tuple of Strings denoting the full
     * name, the email address, and the color as obtained by {@link #get_color(String, String)}
     * with the name and email as the parameters.
     *
     * @param sessionID identifies the session
     * @return a list of authors that are known for the file in session
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     * </ul>
     */
    public List get_authors(int sessionID) throws XmlRpcException;

    /**
     * Obtain the complete commit history of the file indicated by the session ID.
     * The returned list contains for each commit a 6-tuple string that denote
     * revision ID, message, author name, author email, date of the commit, and a list of
     * revision IDs of parent commits separated by space.  The list of parent ID's can be empty.
     * 
     * @param sessionID identifies the session
     * @return list of string 6-tuples that denote all commits known for the file in session 
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 2 if a ParseException occurred during retrieval of commit graph.
     *   <li>with error code = 3 if a JavaGitException occurred during retrieval of commit graph.
     *   <li>with error code = 4 if an IOException occurred during retrieval of commit graph.
     * </ul>
     */
    public List get_commits(int sessionID) throws XmlRpcException;

    /**
     * Obtain the current author for the given session.
     * If exists, the author is given as a 3-tuple of strings denoting the name (not empty), the
     * email address (possibly empty), and the color as obtained by {@link #get_color(String, String)}
     * with the name and email as the parameters.  If no author is set, the returned array is empty.
     *
     * @param sessionID identifies the session
     * @return 3-tuple of strings describing the current author or an empty array
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 2 if another unrecoverable exception occurred.
     * </ul>
     */
    public Object[] get_self(int sessionID) throws XmlRpcException;

    /**
     * Set the current author to the given name and email for the given session.
     * The name cannot be empty.  To unset any author for this repository use {@link #reset_self(int)}.
     * The email address can be empty.  This function returns the newly set author as if
     * calling {@link #get_self(int)}.
     *
     * @param sessionID identifies the session
     * @param name non-empty String as the name of the current author
     * @param email String with the email address of the current author (can be empty)
     * @return 3-tuple of strings describing the current author
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 4 if an author with the given arguments cannot be created.
     * </ul>
     */
    public Object[] set_self(int sessionID, String name, String email) throws XmlRpcException;

    /**
     * Unsets any current author for the repository indicated by given session.
     * This function returns the current notion of self before any calls to {@link #set_self(int, String, String)}.
     *
     * @param sessionID identifies the session
     * @return 3-tuple of strings describing the current author or an empty array
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 5 if an Exception occurred during resetting self.
     * </ul>
     */
    public Object[] reset_self(int sessionID) throws XmlRpcException;

    /**
     * Obtain authors that are currently used to limit the commit graph.  If none are
     * set, returns an empty list.  Otherwise, the returned list contains authors as
     * a triple of full name and email address and color assigned by the system.
     *  
     * @param sessionID identifies the session
     * @return a list of authors that are currently set to limit the commit graph
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     * </ul>
     */
    public List get_limited_authors(int sessionID) throws XmlRpcException;

    /**
     * Set those authors, which are meant to limit the commit graph.  These authors are
     * given as a list of pairs of full name and email address.
     * If the list is empty, no authors are limiting
     * the commit graph.  This function returns the list of currently limiting authors
     * after setting them as if calling {@link #get_limited_authors(int)}.
     *
     * @param sessionID identifies the session
     * @param authors a list of authors to be used for limiting the commit graph
     * @return a list of authors that are currently set to limit the commit graph
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 2 if the given list of authors could not be parsed.
     * </ul>
     */
    public List set_limited_authors(int sessionID, List authors) throws XmlRpcException;

    /**
     * Reset authors that are limiting the commit graph to the empty set.
     *
     * @param sessionID identifies the session
     * @return 0
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     * </ul>
     */
    public int reset_limited_authors(int sessionID) throws XmlRpcException;

    /**
     * Set a date to limit the commit graph for the given session.
     * If the given string for date is empty, then no limit is applied.
     * Otherwise, the string could contain a natural language representation, which
     * will be parsed using {@link com.sri.ltc.CommonUtils.deSerializeDate()}.
     *
     * @param sessionID identifies the session
     * @param date string describing the date to limit the commit graph (as understood by
     *    <code>git-log</code>) or "" (empty string) if no limit should be applied
     * @return the set date as a string
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     * </ul>
     */
    public String set_limited_date(int sessionID, String date) throws XmlRpcException;

    /**
     * Get the currently set date to limit the commit graph for the given session.
     * If the returned string is empty, this means no limit should be applied.
     * 
     * @param sessionID identifies the session
     * @return a string describing the current date to limit the commit graph, which
     *   is empty if no limit is desired
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     * </ul>
     */
    public String get_limited_date(int sessionID) throws XmlRpcException;

    /**
     * Remove the date limit for commit graphs.  This is the same as calling
     * {@link #set_limited_date(int, String)} with an empty string.
     *
     * @param sessionID identifies the session
     * @return 0
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     * </ul>
     */
    public int reset_limited_date(int sessionID) throws XmlRpcException;

    /**
     * Set a revision to limit the commit graph for the given session.
     * The revision can be a unique substring at the beginning of any valid revision ID.
     * If the given string for rev is empty, then no limit is applied.
     * If the revision control does not know the given revision, all
     * revisions are included (unless limited by other filters).
     *
     * @param sessionID identifies the session
     * @param rev string describing the revision to limit the commit graph (can be a unique
     *            substring at the beginning of the revision ID)
     *            or "" (empty string) if no limit should be applied
     * @return the given revision string
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     * </ul>
     */
    public String set_limited_rev(int sessionID, String rev) throws XmlRpcException;

    /**
     * Get the currently set revision to limit the commit graph for the given session.
     * If the returned string is empty, this means no limit should be applied.
     *
     * @param sessionID identifies the session
     * @return a string describing the current revision to limit the commit graph, which
     *   is empty if no limit is desired
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     * </ul>
     */
    public String get_limited_rev(int sessionID) throws XmlRpcException;

    /**
     * Remove the revision limit for commit graphs.  This is the same as calling
     * {@link #set_limited_rev(int, String)} (int, String)} with an empty string.
     *
     * @param sessionID identifies the session
     * @return 0
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     * </ul>
     */
    public int reset_limited_rev(int sessionID) throws XmlRpcException;

    /**
     * Obtain the current status for the given boolean preference item.
     * @param key String that identifies which boolean preference item is requested.  Should denote
     *            one of the constants of {@link com.sri.ltc.server.LTCserverInterface.BoolPrefs}
     * @return boolean value of current status of given boolean preference item
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given String is not one of the constants of
     *       {@link com.sri.ltc.server.LTCserverInterface.BoolPrefs}.
     * </ul>
     */
    public boolean get_bool_pref(String key) throws XmlRpcException;

    /**
     * Set new status of given boolean preference item to given value.
     * @param key String that identifies which boolean preference item is to be set.  Should denote
     *            one of the constants of {@link com.sri.ltc.server.LTCserverInterface.BoolPrefs}
     * @param value boolean that denotes the new status of given boolean preference item
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given String is not one of the constants of
     *       {@link com.sri.ltc.server.LTCserverInterface.BoolPrefs}.
     * </ul>
     * @return 0
     */
    public int set_bool_pref(String key, boolean value) throws XmlRpcException;

    /**
     * Reset all boolean preference items to their default values.
     * @return 0
     */
    public int reset_bool_prefs();

    /**
     * Get all permissible boolean preference item names.
     * @return An array of permissible boolean preference item names
     */
    public Object[] get_bool_pref_items();

    /**
     * Get current color for given author as name and email.  The returned color is
     * given as a 24-bit hex string such as #0000ff for blue.
     * <p>
     * If the color has not been set, the system chooses a default color.  It first
     * attempts to assign a default color from a fixed list of a few defined colors.
     * Once these colors are exhausted during the runtime of the system, a random
     * color is assigned.  Once a color has been assigned, it is persistently stored
     * and used in future invocations unless {@link #reset_color(String, String)} for
     * the same author or {@link #reset_all_colors()} is called.
     * 
     * @param authorName String with full name of author (cannot be NULL or empty)
     * @param authorEmail String with email of author (can be NULL or empty)
     * @return String with color in hex format such as #0000ff for blue
     * @throws XmlRpcException <ul>
     *   <li>with error code = 10 if the given name is NULL or empty.
     * </ul>
     */
    public String get_color(String authorName, String authorEmail) throws XmlRpcException;

    /**
     * Set current color for given author.  The color should be in 24-bit hex format
     * such as #0000ff for blue.
     * <p>
     * The setting is persistently stored until {@link #reset_color(String, String)} for
     * the same author or {@link #reset_all_colors()} is called.
     *
     * @param authorName String with full name of author (cannot be NULL or empty)
     * @param authorEmail String with email of author (can be NULL or empty)
     * @param hexColor String with color in hex format such as #0000ff for blue
     * @return 0
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given name is NULL or empty.
     *   <li>with error code = 2 if the given color code cannot be converted and a NumberFormatException was thrown.
     *   <li>with error code = 3 if an exception occurs while writing color to the backing store of the preference system.
     * </ul>
     */
    public int set_color(String authorName, String authorEmail, String hexColor) throws XmlRpcException;

    /**
     * Reset current color for given author.  This removes any persistently stored value.
     * Subsequent calls to {@link #get_color(String, String)} for the same author will
     * result in choosing default or random colors for this author.
     *
     * @param authorName String with full name of author (cannot be NULL or empty)
     * @param authorEmail String with email of author (can be NULL or empty)
     * @return 0
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given name is NULL or empty.
     * </ul>
     */
    public int reset_color(String authorName, String authorEmail) throws XmlRpcException;

    /**
     * Reset current colors for all persistently stored authors.
     *
     * @return 0
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if a BackingStoreException occurred while accessing the persistent storage.
     * </ul>
     */
    public int reset_all_colors() throws XmlRpcException;

    /**
     * Create a bug report with current system state.
     *
     * @param sessionID identifies the session
     * @param message any user-entered comments describing the bug
     * @param includeRepository whether to include a bundle of the source repository
     * @param outputDirectory the path that the zip file should be created in (cannot be empty or NULL)
     * @return the name of the zip file created
     */
    public String create_bug_report(int sessionID, String message, boolean includeRepository, String outputDirectory) throws XmlRpcException;

    /**
     * Test method for implementing XML-RPC clients.
     *
     * @return 42
     * @throws XmlRpcException if something went wrong
     */
    public int hello() throws XmlRpcException;
}
