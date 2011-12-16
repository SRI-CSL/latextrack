/**
 ************************ 80 columns *******************************************
 * LTCserverInterface
 *
 * Created on May 17, 2010.
 *
 * Copyright 2009-2010, SRI International.
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
    public final static String KEY_SHA1 = "sha1";
    public static enum Show {SMALL, DELETIONS, PREAMBLE, COMMANDS, COMMENTS};
    public static enum EditType {INSERT, REMOVE, DELETE};
    public final static String ON_DISK = "on disk"; // special SHA1 for version on disk (if file modified and not committed)    
    public final static String MODIFIED = "modified"; // special SHA1 for text modified in editor

    /**
     * Initialize a new track changes session with the base system.
     * The given path should point to the file being edited.  Relative
     * file names are allowed.
     *
     * @param path String containing the path to the file to be tracked
     * @param currentText String containing the current text to be compared to the last
     * version of file on disk.  Use empty if only versions under git and on file
     * are relevant for comparison
     * @return a session ID to be used in subsequent calls regarding this track session
     * @throws XmlRpcException <ul>
     *   <li>with error code = 2 if the given file is not readable.
     *   <li>with error code = 3 if the parent of the given file is not a git repository.
     *   <li>with error code = 4 if the given file is not being tracked under git.
     *   <li>with error code = 5 if an IOException occurred while traversing the file hierarchy.
     *   <li>with error code = 6 if a JavaGitException occurred during git file creation.
     *   <li>with error code = 7 if an IOException occurred during git file creation.
     *   <li>with error code = 8 if a ParseException occurred during git file creation.
     * </ul>
     */
    public int init_session(String path, String currentText) throws XmlRpcException;

    /**
     * Closes the session indicated by the given session identifier.
     * The call returns the raw, unmarked up text after applying the most recent edits
     * (as given) to the current text in this session.
     *
     * @param sessionID identifies the session
     * @param recentEdits list of string 3-tuples that denote recent edits since the last
     * call in the format type as a constant from {@link com.sri.ltc.server.LTCserverInterface.EditType},
     * the (numeric) offset in text, and either the text itself (for INSERT or DELETE) or
     * the length (for REMOVE)
     * @return String with unmarked up text of this session after possibly applying given edits 
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 2 if the given recent edits cannot be applied to last known text.
     *   <li>with error code = 3 if a FileNotFoundException occurred.
     *   <li>with error code = 4 if an IOException occurred.
     * </ul>
     */
    public String close_session(int sessionID, List recentEdits) throws XmlRpcException;

    /**
     * Save the current, raw text (after applying any recent changes given) to the file indicated
     * by the given session ID.
     *
     * @param sessionID identifies the session
     * @param recentEdits list of string 3-tuples that denote recent edits since the last
     * call in the format type as a constant from {@link com.sri.ltc.server.LTCserverInterface.EditType},
     * the (numeric) offset in text, and either the text itself (for INSERT or DELETE) or
     * the length (for REMOVE)
     * @return 0
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 2 if the given recent edits cannot be applied to last known text.
     *   <li>with error code = 3 if an IOException occurred during saving the file.
     *   <li>with error code = 3 if a FileNotFoundException occurred during saving the file.
     * </ul>
     */
    public int save_file(int sessionID, List recentEdits) throws XmlRpcException;
    
    /**
     * Obtains the changes of the file indicated by the session ID.
     * The changes are limited to the currently set filter for this session and any
     * global preferences.
     * <p>
     * In addition, if the list of recent edits is not empty, these are applied to the
     * last known text from this session.
     * <p>
     * The return value contains the text including the changes (for example, deletions)
     * under the key {@link #KEY_TEXT} and the list of styles to be used under the key
     * {@link #KEY_STYLES}.  Each style is a 4-tuple of numbers that denote start and
     * end position as well as style (1 - addition, 2 - deletion) and an author index.
     * Start denotes the first character affected by this style, whereas the end position
     * is the first position of the next chunk (to be excluded).  Furthermore, the return
     * value under {@link #KEY_AUTHORS} also contains a map of numbers to authors and their
     * color, which
     * are given as an array of 3 Strings containing name, email address, and color name.
     * Another entry under {@link #KEY_SHA1} in the returned map is a list of SHA1 keys
     * that have been used to obtain the changes.  These could be matched to the list
     * of all commits from {@link #get_commits(int)}.
     *
     * @param sessionID identifies the session
     * @param recentEdits list of string 3-tuples that denote recent edits since the last
     * call in the format type as a constant from {@link com.sri.ltc.server.LTCserverInterface.EditType},
     * the (numeric) offset in text, and either the text itself (for INSERT or DELETE) or
     * the length (for REMOVE)
     * @return Map that contains the text with changes, list of styles to be applied to
     * this text, map of indices to authors, and list of SHA1 keys
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 2 if an IOException occurred during change accumulation:.
     *   <li>with error code = 3 if a BadLocationException occurred during change accumulation.
     *   <li>with error code = 4 if a JavaGitException occurred during log retrieval.
     *   <li>with error code = 5 if an IOException occurred during log retrieval.
     *   <li>with error code = 6 if a ParseException occurred during log retrieval.
     *   <li>with error code = 7 if the given recent edits cannot be applied to last known text.
     * </ul>
     */
    public Map get_changes(int sessionID, List recentEdits) throws XmlRpcException;

    /**
     * Commit the current file on disk to git.  The file is indicated by the session ID.
     * A non-null and non-empty message must be supplied.
     *
     * A special error case is the so-called "empty commit" when the file on disk has not
     * changed compared to the last commit.  In this case, an exception with code 5 is thrown.
     *
     * @param sessionID identifies the session
     * @param message A non-null and not empty message for the commit
     * @return 0
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 2 if the given message is NULL or empty.
     *   <li>with error code = 3 if an IOException occurred during committing.
     *   <li>with error code = 4 if a JavaGitException occurred during committing.
     *   <li>with error code = 5 if there was nothing to commit (empty commit).
     *   <li>with error code = 6 if an error occurred during performing <code>git commit</code>.
     * </ul>
     */
    public int commit_file(int sessionID, String message) throws XmlRpcException;

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
     * SHA1, message, author name, author email, date of the commit, and a list of
     * SHA1's of parent commits separated by space.  The list of parent SHA1's can be empty.
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
     * Obtain the currently set author for the git repository indicated through the given session.
     * If exists, the author is given as a 3-tuple of strings denoting the name (not empty), the
     * email address (possibly empty), and the color as obtained by {@link #get_color(String, String)}
     * with the name and email as the parameters.  If no author is set, the returned array is empty.
     *
     * @param sessionID identifies the session
     * @return 3-tuple of strings describing the current author or an empty array
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 2 if a JavaGitException occurred during self retrieval.
     *   <li>with error code = 3 if an IOException occurred during self retrieval.
     * </ul>
     */
    public Object[] get_self(int sessionID) throws XmlRpcException;

    /**
     * Set the current author to the given name and email for the git repository indicated
     * by given session.  The name cannot be empty.  To unset any author for this git
     * repository use {@link #reset_self(int)}.  The email address can be empty.  This
     * function returns the newly set author as if calling {@link #get_self(int)}.
     *
     * @param sessionID identifies the session
     * @param name non-empty String as the name of the current author
     * @param email String with the email address of the current author (can be empty)
     * @return 3-tuple of strings describing the current author
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 2 if a JavaGitException occurred during self retrieval.
     *   <li>with error code = 3 if an IOException occurred during self retrieval.
     *   <li>with error code = 4 if an author with the given arguments cannot be created.
     *   <li>with error code = 5 if a JavaGitException occurred during setting self.
     *   <li>with error code = 6 if an IOException occurred during setting self.
     * </ul>
     */
    public Object[] set_self(int sessionID, String name, String email) throws XmlRpcException;

    /**
     * Unsets any current author for the git repository indicated by given session.
     * This function returns the current notion of self if a global configuration of git
     * has been made.
     *
     * @param sessionID identifies the session
     * @return 3-tuple of strings describing the current author or an empty array
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 2 if a JavaGitException occurred during self retrieval.
     *   <li>with error code = 3 if an IOException occurred during self retrieval.
     *   <li>with error code = 4 if a JavaGitException occurred during resetting self.
     *   <li>with error code = 5 if an IOException occurred during resetting self.
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
     * Otherwise, the string can take any form that <code>git-log</code> understands such as
     * "2 weeks ago" or "2010-7-29".
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
     * The revision can be a unique substring at the beginning of any valid SHA1 hash.
     * If the given string for rev is empty, then no limit is applied.
     * Otherwise, if <code>git</code> does not know the given revision, all
     * revisions are included (unless limited by other filters).
     *
     * @param sessionID identifies the session
     * @param rev string describing the revision to limit the commit graph (as understood by
     *    <code>git-log</code>) or "" (empty string) if no limit should be applied
     * @return the set revision as a string
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
     * Obtain the current setting of filtering for the given showing item.
     * @param key String that identifies which showing item is requested.  Should denote
     *            one of the constants of {@link com.sri.ltc.server.LTCserverInterface.Show}
     * @return boolean value of current status of given showing item
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given String is not one of the constants of
     *       {@link com.sri.ltc.server.LTCserverInterface.Show}.
     * </ul>
     */
    public boolean get_show(String key) throws XmlRpcException;

    /**
     * Set new filtering status of given showing item to given value.
     * @param key String that identifies which showing item is to be set.  Should denote
     *            one of the constants of {@link com.sri.ltc.server.LTCserverInterface.Show}
     * @param value boolean that denotes the new filtering status of given showing item
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given String is not one of the constants of
     *       {@link com.sri.ltc.server.LTCserverInterface.Show}.
     * </ul>
     * @return 0
     */
    public int set_show(String key, boolean value) throws XmlRpcException;

    /**
     * Reset all showing items to their default values.
     * @return 0
     */
    public int reset_show();

    /**
     * Get all permissible showing item names.
     * @return An array of permissible showing item names
     */
    public Object[] get_show_items();

    /**
     * Get currently set remote aliases from git.  The list can be empty.
     *
     * @param sessionID identifies the session
     * @return a list of String[3] objects that each denote a remote: the first String is the
     *   name of the alias, the second String is the URL, and the third String is either "true"
     *   or "false" depending on whether the remote is read-only 
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 2 if a JavaGitException occurs while running the git command
     * </ul>
     */
    public List get_remotes(int sessionID) throws XmlRpcException;

    /**
     * Add a new alias to a given remote repository.  Both the alias and the given URL
     * must be non-null and non-empty.  The operation typically fails when the given
     * alias already exists.
     *
     * @param sessionID identifies the session
     * @param name String for the alias
     * @param url URL of the remote repository
     * @return 0
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 2 if a JavaGitException occurs while running the git command
     *   <li>with error code = 3 if the underlying command exited with a non-null value
     *         but did not generate and exception
     * </ul>
     */
    public int add_remote(int sessionID, String name, String url) throws XmlRpcException;
    
    /**
     * Remove an alias from a given remote repository.  The alias must be non-null and
     * non-empty.  
     *
     * @param sessionID identifies the session
     * @param name String for the alias
     * @return 0
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 2 if a JavaGitException occurs while running the git command
     *   <li>with error code = 3 if the underlying command exited with a non-null value
     *         but did not generate and exception
     * </ul>
     */
    public int rm_remote(int sessionID, String name) throws XmlRpcException;

    /**
     * Push to a given remote repository.  The name must be non-null and
     * non-empty.
     *
     * @param sessionID identifies the session
     * @param repository describes the remote repository (alias or URL)
     * @return an empty String when no exception nor error occurred during running the git
     *   command.  If String is not empty, an error (but no exception) occurred.
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 2 if a JavaGitException occurs while running the git command
     * </ul>
     */
    public String push(int sessionID, String repository) throws XmlRpcException;

    /**
     * Pull from a given remote repository.  The name must be non-null and
     * non-empty.
     *
     * @param sessionID identifies the session
     * @param repository describes the remote repository (alias or URL)
     * @return an empty String when no exception nor error occurred during running the git
     *   command.  If String is not empty, an error (but no exception) occurred.
     * @throws XmlRpcException <ul>
     *   <li>with error code = 1 if the given identifier does not denote a known session.
     *   <li>with error code = 2 if a JavaGitException occurs while running the git command
     * </ul>
     */
    public String pull(int sessionID, String repository) throws XmlRpcException;

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
     * Test method for implementing XML-RPC clients.
     *
     * @return 42
     * @throws XmlRpcException if something went wrong
     */
    public int hello() throws XmlRpcException;
}
