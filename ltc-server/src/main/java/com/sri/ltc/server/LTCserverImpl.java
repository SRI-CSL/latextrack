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

import com.sri.ltc.CommonUtils;
import com.sri.ltc.ProgressReceiver;
import com.sri.ltc.filter.Author;
import com.sri.ltc.filter.Filtering;
import com.sri.ltc.versioncontrol.history.LimitedHistory;
import com.sri.ltc.versioncontrol.Remote;
import com.sri.ltc.latexdiff.*;
import com.sri.ltc.versioncontrol.*;
import org.apache.xmlrpc.XmlRpcException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.text.BadLocationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author linda
 */
public final class LTCserverImpl implements LTCserverInterface {

    private final Preferences preferences = Preferences.userNodeForPackage(this.getClass());
    private final static List<Color> defaultColors = new ArrayList<Color>(Arrays.asList(
            Color.blue, Color.red, Color.green, Color.magenta, Color.orange, Color.cyan));
    private final static String KEY_COLOR = "author-color:";
    private final ProgressReceiver progressReceiver;

    public LTCserverImpl() {
        this.progressReceiver = null;
    }

    public LTCserverImpl(ProgressReceiver progressReceiver) {
        this.progressReceiver = progressReceiver;
    }

    private void updateProgress(int progress) {
        if (progressReceiver != null)
            progressReceiver.updateProgress(progress);
    }

    private final static Logger LOGGER = Logger.getLogger(LTCserverImpl.class.getName());

    private void logAndThrow(int code, String message) throws XmlRpcException {
        XmlRpcException e = new XmlRpcException(code, message);
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
        throw e;
    }

    private Session getSession(int sessionID) throws XmlRpcException {
        Session session = SessionManager.getSession(sessionID);
        if (session == null)
            logAndThrow(1,"Cannot retrieve session with given ID");
        return session;
    }

    // -- begin API implementation
    // TODO: apply locks to make block atomic...?

    public int hello() {
        LOGGER.info("Server: Hello World!");
        return 42;
    }

    public int init_session(String path) throws XmlRpcException {
        if (path == null)
            logAndThrow(1,"Given path is NULL");

        LOGGER.info("Server: init_session with file \""+path+"\" called.");

        // is given path a valid file?
        File file = new File(path);
        if (!file.canRead())
            logAndThrow(2,"Cannot read given file");

        Repository repository = null;
        try {
            repository = RepositoryFactory.fromPath(file);
        } catch (Exception e) {
            logAndThrow(3, "Could not find version control (git or svn) for this file");
        }
        updateProgress(3);

        try {
            TrackedFile trackedFile = repository.getFile(file);
            // test whether file tracked under git
            switch (trackedFile.getStatus()) {
                case NotTracked:
                    logAndThrow(4, "File not tracked under version control");
                    break;
                case Removed:
                    logAndThrow(5, "File deleted or deleted to commit under version control");
                    break;
                case Unknown:
                    logAndThrow(6, "File status unknown under version control");
                    break;

                default:
                    break;
            }
            updateProgress(10);

            return SessionManager.createSession(trackedFile);
        } catch (IOException e) {
            logAndThrow(7,"IOException during tracked file creation: "+e.getMessage());
        } catch (ParseException e) {
            logAndThrow(8,"ParseException during tracked file creation: "+e.getMessage()+" (@"+e.getErrorOffset()+")");
        } catch (VersionControlException e) {
            logAndThrow(10,"VersionControlException during tracked file creation: "+e.getMessage());
        } catch (IllegalStateException e) {
            logAndThrow(11,"Tracked file already active under another session: "+e.getMessage());
        }

        return -1;
    }

    @SuppressWarnings("unchecked")
    public Map close_session(int sessionID, String currentText, List deletions, int caretPosition) throws XmlRpcException {
        Session session = SessionManager.finishSession(sessionID);
        if (session == null)
            logAndThrow(1,"Cannot close session with given ID");

        LOGGER.info("Server: close_session for file \""+session.getTrackedFile().getFile().getAbsolutePath()+"\", "+
                "text with "+currentText.length()+" characters, "+
                (deletions != null?
                        deletions.size()+" deletions, ":
                        "")+
                "and caret at "+caretPosition+" called.");

        // apply deletions to current text and update caret position
        try {
            if (deletions != null && !deletions.isEmpty()) {
                MarkedUpDocument document = new MarkedUpDocument(currentText, deletions, caretPosition);
                document.removeDeletions();
                currentText = document.getText(0, document.getLength());
                caretPosition = document.getCaretPosition();
            }
        } catch (BadLocationException e) {
            logAndThrow(2,"Cannot remove deletions at "+e.offsetRequested()+" while closing session: "+e.getMessage());
        }

        // create return value
        Map map = new HashMap();
        map.put(LTCserverInterface.KEY_TEXT, currentText);
        map.put(LTCserverInterface.KEY_CARET, caretPosition);
        return map;
    }

    @SuppressWarnings("unchecked")
    public int save_file(int sessionID, String currentText, List deletions) throws XmlRpcException {
        Session session = getSession(sessionID);

        LOGGER.info("Server: save_file to file \""+session.getTrackedFile().getFile().getAbsolutePath()+"\" "+
                "text with "+currentText.length()+" characters"+
                (deletions != null?
                        " and "+deletions.size()+" deletions":
                        "")+
                " called.");

        try {
            if (deletions != null && !deletions.isEmpty()) {
                MarkedUpDocument document = new MarkedUpDocument(currentText, deletions, 0);
                document.removeDeletions();
                currentText = document.getText(0, document.getLength());
            }
            // write current text to file
            BufferedWriter out = new BufferedWriter(new FileWriter(session.getTrackedFile().getFile(), false));
            out.write(currentText);
            out.close();
        } catch (BadLocationException e) {
            logAndThrow(2,"Cannot apply recent edits at "+e.offsetRequested()+" while saving file: "+e.getMessage());
        } catch (FileNotFoundException e) {
            logAndThrow(4,"FileNotFoundException during saving file: "+e.getMessage());
        } catch (IOException e) {
            logAndThrow(3,"IOException during saving file: "+e.getMessage());
        }

        return 0;
    }

    @SuppressWarnings (value={"unchecked","fallthrough"})
    public Map get_changes(int sessionID, boolean isModified, String currentText, List deletions, int caretPosition) throws XmlRpcException {
        Session session = getSession(sessionID);

        LOGGER.info("Server: get_changes for file \""+session.getTrackedFile().getFile().getAbsolutePath()+"\", "+
                (isModified?"":"not ")+"modified, "+
                "text with "+currentText.length()+" characters, "+
                (deletions != null?
                        deletions.size()+" deletions, ":
                        "")+
                "and caret at "+caretPosition+" called.");

        // apply deletions to current text and update caret position
        try {
            Map<MarkedUpDocument.KEYS,Object> map =
                    MarkedUpDocument.applyDeletions(currentText, deletions, caretPosition);
            currentText = (String) map.get(MarkedUpDocument.KEYS.TEXT);
            caretPosition = (Integer) map.get(MarkedUpDocument.KEYS.POSITION);
        } catch (BadLocationException e) {
            logAndThrow(7,"Cannot remove deletions at "+e.offsetRequested()+" while getting changes: "+e.getMessage());
        }
        updateProgress(10);

        Filtering filter = Filtering.getInstance();

        // obtain file history from GIT, disk, and session (obeying any filters):
        List<ReaderWrapper> readers = null;
        List<Author> authors = null;
        List<String> revisions = new ArrayList<String>();
        try {
            // TODO: whether to condense authors or not (from Filtering)
            // create history with limits and obtain revision IDs, authors, and readers:
            LimitedHistory history = new LimitedHistory(session.getTrackedFile(),
                    session.getLimitedAuthors(),
                    session.getLimitDate(),
                    session.getLimitRev(),
                    filter.getStatus(BoolPrefs.COLLAPSE_AUTHORS));
            updateProgress(12);
            revisions = history.getIDs(); // these are the IDs used in the accumulation
            authors = history.getAuthorsList();
            updateProgress(15);
            readers = history.getReadersList();
            updateProgress(45);

            Author self = session.getTrackedFile().getRepository().getSelf();
            // add file on disk to list, if modified or new but not committed yet
            switch (session.getTrackedFile().getStatus()) {
                case Added:
                case Modified:
                case Changed:
                    ReaderWrapper fileReader = new FileReaderWrapper(session.getTrackedFile().getFile().getCanonicalPath());
                    if (authors.size() > 0 && authors.get(authors.size()-1).equals(self))
                        // replace last reader
                        readers.remove(readers.size()-1);
                    else
                        // add self as author
                        authors.add(self);
                    readers.add(fileReader);
                    revisions.add(LTCserverInterface.ON_DISK);
            }
            // add current text from editor, if modified since last save:
            if (isModified) {
                ReaderWrapper currentTextReader = new StringReaderWrapper(currentText);
                if (authors.size() > 0 && authors.get(authors.size()-1).equals(self))
                    // replace last reader
                    readers.remove(readers.size()-1);
                else
                    // add self as author
                    authors.add(self);
                readers.add(currentTextReader);
                // replace ON_DISK if present, otherwise append MODIFIED
                if (revisions.size() > 0
                        && LTCserverInterface.ON_DISK.equals(revisions.get(revisions.size()-1))) {
                    revisions.remove(revisions.size()-1);
                }
                revisions.add(LTCserverInterface.MODIFIED);
            }
            // if no readers, then use text from file and self as author
            if (readers.size() == 0 && authors.size() == 0) {
                authors.add(self);
                readers.add(new FileReaderWrapper(session.getTrackedFile().getFile().getCanonicalPath()));
                revisions.add("");
            }
            updateProgress(47);
        } catch (IOException e) {
            logAndThrow(5,"IOException during log retrieval: "+e.getMessage());
        } catch (ParseException e) {
            logAndThrow(6,"ParseException during log retrieval: "+e.getMessage()+" (@"+e.getErrorOffset()+")");
        } catch (Exception e) {
            logAndThrow(7,"General Exception during log retrieval: "+e.getMessage());
        }

        session.addAuthors(new HashSet<Author>(authors));
        // compute indices of authors
        Map<Integer,Object[]> mappedAuthors = new HashMap<Integer,Object[]>();
        List<Integer> indices = new ArrayList<Integer>();
        if (authors != null && authors.size() > 0) {
            List<Author> sortedAuthors = new ArrayList<Author>(new TreeSet<Author>(authors));
            // build up map: index -> author and color (as list)
            for (Author a : sortedAuthors)
                mappedAuthors.put(sortedAuthors.indexOf(a), concatAuthorAndColor(a));
            // build up list of indices 
            for (Author a : authors)
                indices.add(sortedAuthors.indexOf(a));
        }
        updateProgress(50);

        // do diffs and accumulate changes:
        Map map = null;
        try {
            PropertyChangeListener listener = null;
            if (progressReceiver != null) {
                listener = new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent e) {
                        if (Accumulate.PROGRESS_PROPERTY.equals(e.getPropertyName())) {
                            updateProgress(50+(int) (((Float) e.getNewValue())*40f));
                        }
                    }
                };
                session.getAccumulate().addPropertyChangeListener(listener);
            }
            map = session.getAccumulate().perform(
                    readers.toArray(new ReaderWrapper[readers.size()]),
                    indices.toArray(new Integer[indices.size()]),
                    Change.buildFlagsToHide(
                            filter.getStatus(BoolPrefs.DELETIONS),
                            filter.getStatus(BoolPrefs.SMALL),
                            filter.getStatus(BoolPrefs.PREAMBLE),
                            filter.getStatus(BoolPrefs.COMMENTS),
                            filter.getStatus(BoolPrefs.COMMANDS)),
                    caretPosition);
            map.put(LTCserverInterface.KEY_AUTHORS, mappedAuthors); // add current author map
            map.put(LTCserverInterface.KEY_REVS, revisions); // add list of revisions used in accumulation
            session.getAccumulate().removePropertyChangeListener(listener);
        } catch (Exception e) {
            logAndThrow(2,"Exception during change accumulation: "+e.getMessage());
        }
        updateProgress(90);

        return map;
    }

    public int commit_file(int sessionID, String message) throws XmlRpcException {
        Session session = getSession(sessionID);

        if (message == null || "".equals(message))
            logAndThrow(2, "Cannot commit file with an empty message");

        LOGGER.info("Server: commit_file to file \""+session.getTrackedFile().getFile().getAbsolutePath()+"\" called.");

        try {
            session.getTrackedFile().commit(message);
        } catch (Exception e) {
            logAndThrow(4, "JavaGitException during git commit: " + e.getMessage());
        }

        return 0;
    }

    private Object[] concatAuthorAndColor(Author author) throws XmlRpcException {
        Object[] authorAndColor = new Object[3];
        System.arraycopy(author.asList(), 0, authorAndColor, 0, 2);
        authorAndColor[2] = get_color(author.name, author.email);
        return authorAndColor;
    }

    public List get_authors(int sessionID) throws XmlRpcException {
        Session session = getSession(sessionID);
        List<Object[]> result = new ArrayList<Object[]>();
        for (Author author : session.getAuthors())
            result.add(concatAuthorAndColor(author));
        return result;
    }

    public List get_commits(int sessionID) throws XmlRpcException {
        Session session = getSession(sessionID);
        try {
            return session.getCommitGraphAsList();
        } catch (ParseException e) {
            logAndThrow(2, "ParseException during retrieval of commit graph: "+e.getMessage()+" (offset = "+e.getErrorOffset()+")");
        } catch (IOException e) {
            logAndThrow(4,"IOException during retrieval of commit graph: "+e.getMessage());
        } catch (Exception e) {
            logAndThrow(4,"General Exception during retrieval of commit graph: "+e.getMessage());
        }
        return null;
    }

    public Object[] get_self(int sessionID) throws XmlRpcException {
        Session session = getSession(sessionID);
        try {
            return concatAuthorAndColor(session.getTrackedFile().getRepository().getSelf());
        } catch (IllegalArgumentException e) {
            return new Object[0]; // if self is undefined
        }
    }

    public Object[] set_self(int sessionID, String name, String email) throws XmlRpcException {
        Session session = getSession(sessionID);
        try {
            Author a = new Author(name, email);
            session.getTrackedFile().getRepository().setSelf(a);  // TODO: if not implemented by repository (e.g., svn) then don't add to authors!
            session.addAuthors(Collections.singleton(a));
        } catch (IllegalArgumentException e) {
            logAndThrow(4,"Cannot create author to set as self with given arguments: "+e.getMessage());
        }
        return get_self(sessionID);
    }

    public Object[] reset_self(int sessionID) throws XmlRpcException {
        Session session = getSession(sessionID);
        try {
            session.getTrackedFile().getRepository().resetSelf();
        } catch (Exception e) {
            logAndThrow(5,"Exception during resetting self: "+e.getMessage());
        }
        return get_self(sessionID);
    }

    public List get_limited_authors(int sessionID) throws XmlRpcException {
        Session session = getSession(sessionID);
        // build up list as intersection of currently limited authors and known ones
        List<Object[]> result = new ArrayList<Object[]>();
        Set<Author> limitedAuthors = session.getLimitedAuthors();
        if (limitedAuthors.isEmpty())
            return result;
        for (Author author : session.getAuthors())
            if (limitedAuthors.contains(author))
                result.add(concatAuthorAndColor(author));
        return result;
    }

    @SuppressWarnings("unchecked")
    public List set_limited_authors(int sessionID, List authors) throws XmlRpcException {
        Session session = getSession(sessionID);
        session.resetLimitedAuthors();
        try {
            for (Object[] authorAsList : (List<Object[]>) authors)
                session.addLimitedAuthor(Author.fromList(authorAsList));
        } catch (Exception e) {
            logAndThrow(2, e.getMessage());
        }
        return get_limited_authors(sessionID);
    }

    public int reset_limited_authors(int sessionID) throws XmlRpcException {
        Session session = getSession(sessionID);
        session.resetLimitedAuthors();
        return 0;
    }

    public String set_limited_date(int sessionID, String date) throws XmlRpcException {
        Session session = getSession(sessionID);
        session.setLimitDate(date);
        return session.getLimitDate();
    }

    public String get_limited_date(int sessionID) throws XmlRpcException {
        Session session = getSession(sessionID);
        return session.getLimitDate();
    }

    public int reset_limited_date(int sessionID) throws XmlRpcException {
        set_limited_date(sessionID, null);
        return 0;
    }

    public String set_limited_rev(int sessionID, String rev) throws XmlRpcException {
        Session session = getSession(sessionID);
        session.setLimitRev(rev);
        return session.getLimitRev();
    }

    public String get_limited_rev(int sessionID) throws XmlRpcException {
        Session session = getSession(sessionID);
        return session.getLimitRev();
    }

    public int reset_limited_rev(int sessionID) throws XmlRpcException {
        set_limited_rev(sessionID, null);
        return 0;
    }

    public boolean get_bool_pref(String key) throws XmlRpcException {
        try {
            BoolPrefs boolPref = BoolPrefs.valueOf(key);
            return Filtering.getInstance().getStatus(boolPref);
        } catch (IllegalArgumentException e) {
            logAndThrow(1, e.getMessage());
        }
        return false;
    }

    public int set_bool_pref(String key, boolean value) throws XmlRpcException {
        try {
            BoolPrefs boolPref = BoolPrefs.valueOf(key);
            Filtering.getInstance().setStatus(boolPref, value);
            LOGGER.info("Server: turning boolean preference for \""+key+(value?"\" on.":"\" off."));
        } catch (IllegalArgumentException e) {
            logAndThrow(1, e.getMessage());
        }
        return 0;
    }

    public int reset_bool_prefs() {
        Filtering.getInstance().resetAllStatus();
        LOGGER.info("Server: resetting all boolean preferences to default.");
        return 0;
    }

    public Object[] get_bool_pref_items() {
        List<String> names = new ArrayList<String>();
        for (BoolPrefs boolPref : BoolPrefs.values())
            names.add(boolPref.name());
        return names.toArray();
    }

    @Override
    public int add_remote(int sessionID, String name, String url) throws XmlRpcException {
        Session session = getSession(sessionID);
        try {
            if (session.getRemotes().addRemote(name, url) != 0)
                logAndThrow(3, "Underlying git-remote command exited with non-zero code.");
        } catch (Exception e) {
            logAndThrow(2, "JavaGitException while adding a remote: "+e.getMessage());
        }
        return 0;
    }

    @Override
    public int rm_remote(int sessionID, String name) throws XmlRpcException {
        Session session = getSession(sessionID);
        try {
            if (session.getRemotes().removeRemote(name) != 0)
                logAndThrow(3, "Underlying git-remote command exited with non-zero code.");
        } catch (Exception e) {
            logAndThrow(2, "Exception while removing a remote: "+e.getMessage());
        }
        return 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List get_remotes(int sessionID) throws XmlRpcException {
        Session session = getSession(sessionID);
        List<String[]> remotes = new ArrayList<String[]>();
        try {
            Set<Remote> remoteSet = session.getRemotes().get();
            for (Remote remote : remoteSet)
                remotes.add(remote.toArray());
        } catch (Exception e) {
            logAndThrow(2, "Exception while obtaining remotes: "+e.getMessage());
        }
        return remotes;
    }

    @Override
    public String push(int sessionID, String repository) throws XmlRpcException {
        Session session = getSession(sessionID);

        LOGGER.info("Server: push to repository \""+repository+"\" called.");

        try {
            session.getRemotes().push(repository);
        } catch (Exception e) {
            logAndThrow(2, "Exception while removing a remote: "+e.getMessage());
        }
        return "";
    }

    @Override
    public String pull(int sessionID, String repository) throws XmlRpcException {
        Session session = getSession(sessionID);

        LOGGER.info("Server: pull from repository \""+repository+"\" called.");

        try {
            session.getRemotes().pull(repository);
        } catch (Exception e) {
            logAndThrow(2, "JavaGitException while removing a remote: "+e.getMessage());
        }
        return "";
    }

    @Override
    public String create_bug_report(int sessionID, String message, boolean includeRepository, String outputDirectory) throws XmlRpcException {
        LOGGER.fine("Server: creating bug report in directory \""+outputDirectory+"\"");

        if (outputDirectory == null || outputDirectory.isEmpty())
            logAndThrow(5, "Cannot create bug report with empty or NULL output directory");

        File outputDirectoryFile = new File(outputDirectory);
        if (outputDirectoryFile.mkdirs()) {
            LOGGER.fine("Created report directory " + outputDirectory);
        }

        // bundle repository
        File bundle = null;
        if (includeRepository)
            try {
                bundle = getSession(sessionID).getTrackedFile().getRepository().getBundle(new File(outputDirectory));
            } catch (IOException e) {
                logAndThrow(4, "IOException while creating repository bundle: " + e.getMessage());
            }

        // create an XML with current settings etc.
        File xmlFile = new File(outputDirectory, "report.xml");
        create_bug_report_xml(sessionID, message, bundle, xmlFile.getAbsolutePath());

        // create zip
        File zipFile = new File(outputDirectory, "report.zip");
        LOGGER.fine("Server: zipping up bug report as \""+zipFile.getAbsolutePath()+"\"");
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile, false));

            // add "report.xml"
            copyToZip(zos, xmlFile);

            // add log file(s), if they exist
            File[] logFiles = new File(System.getProperty("user.home")).listFiles(CommonUtils.LOG_FILE_FILTER);
            if (logFiles != null)
                for (File logFile : logFiles)
                    copyToZip(zos, logFile);

            // add bundle file (if exists)
            if (bundle != null)
                copyToZip(zos, bundle);

            zos.close();
        } catch (FileNotFoundException e) {
            logAndThrow(2, "FileNotFoundException while creating report as ZIP file: " + e.getMessage());
        } catch (IOException e) {
            logAndThrow(3, "IOException while creating report as ZIP file: "+e.getMessage());
        }

        return zipFile.getAbsolutePath();
    }

    private void copyToZip(ZipOutputStream zos, File file) throws IOException {
        zos.putNextEntry(new ZipEntry(file.getName()));
        FileInputStream fis = new FileInputStream(file);
        byte[] buf = new byte[1024];
        int len;
        while ((len = fis.read(buf)) >= 0)
            zos.write(buf, 0, len);
        zos.closeEntry();
    }

    private String getColorKey(Author author) {
        return KEY_COLOR + author.toString();
    }

    public static String convertToHex(Color color) {
        return "#"+Integer.toHexString((color.getRGB() & 0xffffff) | 0x1000000).substring(1);
    }

    public String get_color(String authorName, String authorEmail) throws XmlRpcException {
        if (authorName == null || "".equals(authorName))
            logAndThrow(10, "Cannot get color with NULL or empty author name");

        Author author = new Author(authorName, authorEmail);
        LOGGER.fine("Server: getting color for author \""+author.gitRepresentation()+"\" called.");

        // define random color based on randomized hue
        Color randomColor = Color.getHSBColor((float) Math.random(), 0.85f, 1.0f);
        // obtain any stored color from preferences
        Color storedColor;
        synchronized (preferences) {
            storedColor = new Color(preferences.getInt(
                    getColorKey(author),
                    randomColor.getRGB()));
        }
        // if no stored color and at least one default color left, use that one
        // then store new color for future use
        if (storedColor.equals(randomColor)) {
            synchronized (defaultColors) {
                if (!defaultColors.isEmpty()) {
                    storedColor = defaultColors.get(0);
                    defaultColors.remove(0);
                }
            }
            set_color(authorName, authorEmail, convertToHex(storedColor));
        }

        // ignore alpha channel of RGB value for storage
        return convertToHex(storedColor);
    }

    public int set_color(String authorName, String authorEmail, String hexColor) throws XmlRpcException {
        if (authorName == null || "".equals(authorName))
            logAndThrow(1, "Cannot set color with NULL or empty author name");

        Author author = new Author(authorName, authorEmail);
        LOGGER.fine("Server: setting color for author \"" + author.gitRepresentation() + "\" to " + hexColor + ".");

        synchronized (preferences) {
            try {
                preferences.putInt(
                        getColorKey(author),
                        Color.decode(hexColor).getRGB());
                preferences.flush();
            } catch (NumberFormatException e) {
                logAndThrow(2, "NumberFormatException while decoding given color: "+e.getMessage());
            } catch (BackingStoreException e) {
                logAndThrow(3, "BackingStoreException while setting author color: "+e.getMessage());;
            }
        }
        return 0;
    }

    public int reset_color(String authorName, String authorEmail) throws XmlRpcException {
        if (authorName == null || "".equals(authorName))
            logAndThrow(1, "Cannot reset color with NULL or empty author name");
        synchronized (preferences) {
            preferences.remove(getColorKey(new Author(authorName, authorEmail)));
        }
        return 0;
    }

    public int reset_all_colors() throws XmlRpcException {
        synchronized (preferences) {
            try {
                for (String key : preferences.keys()) {
                    if (key.startsWith(KEY_COLOR))
                        preferences.remove(key);
                }
            } catch (BackingStoreException e) {
                logAndThrow(1, "BackingStoreException while removing stored colors: "+e.getMessage());
            }
        }
        return 0;
    }
    // --- end API implementation

    private void addSimpleTextNode(Document document, Element parent, String elementName, String elementText,
                                   Map<String,String> attributes) {
        Element element = document.createElement(elementName);
        parent.appendChild(element);

        if (attributes != null)
            for (Map.Entry<String,String> e : attributes.entrySet())
                element.setAttribute(e.getKey(), e.getValue());

        org.w3c.dom.Text text = document.createTextNode(elementText);
        element.appendChild(text);
    }

    private void addAuthor(Document document, Element parent, String elementName, Author author) {
        Element authorElement = document.createElement(elementName);
        parent.appendChild(authorElement);

        addSimpleTextNode(document, authorElement, "name", author.name, null);
        addSimpleTextNode(document, authorElement, "email", author.email, null);
    }

    private void addAuthorLimit(Document document, Element parent, String elementName, Set<Author> authorsList) {
        Element element = document.createElement(elementName);
        parent.appendChild(element);

        for (Author author : authorsList) {
            addAuthor(document, element, "author", author);
        }
    }

    // assumes: the directory exists already
    private void create_bug_report_xml(int sessionID, String message, File bundle, String outputFileNameAndPath) throws XmlRpcException {
        LOGGER.fine("Server: creating XML report as " + outputFileNameAndPath);

        Session session = getSession(sessionID);
        assert session != null;

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            logAndThrow(2, "Could not create document builder:" + e.getMessage());
        }

        assert documentBuilder != null;
        org.w3c.dom.Document document = documentBuilder.newDocument();

        Element rootElement = document.createElement("bug-report");
        document.appendChild(rootElement);

        addSimpleTextNode(document, rootElement, "user-message", message, null);
        // TODO: this is probably not relative any more! need to check
        addSimpleTextNode(document, rootElement, "relative-file-path", session.getTrackedFile().getFile().getPath(), null);

        // version information
        {
            Element element = document.createElement("build-properties");
            rootElement.appendChild(element);

            Properties properties = CommonUtils.getBuildProperties();
            Map<String,String> attributes = new HashMap<String, String>();
            for (String name : properties.stringPropertyNames()) {
                attributes.put("key", name);
                addSimpleTextNode(document, element, "entry", properties.getProperty(name), attributes);
            }
        }

        // filters
        {
            Element element = document.createElement("filters");
            rootElement.appendChild(element);

            addSimpleTextNode(document, element, "revision", session.getLimitRev(), null);
            addSimpleTextNode(document, element, "date", session.getLimitDate(), null);
            addAuthorLimit(document, element, "authors", session.getLimitedAuthors());
        }

        // active-revisions
        {
            Element element = document.createElement("active-revisions");
            rootElement.appendChild(element);

            LimitedHistory history = null;
            try {
                history = new LimitedHistory(session.getTrackedFile(),
                        session.getLimitedAuthors(),
                        session.getLimitDate(),
                        session.getLimitRev(),
                        get_bool_pref(BoolPrefs.COLLAPSE_AUTHORS.name()));
            } catch (Exception e) {
                logAndThrow(3, "Could not create LimitedHistory:" + e.getMessage());
            }

            assert history != null;
            for (Commit commit : history.getCommitsList()) {
                addSimpleTextNode(document, element, "sha", commit.getId(), null);
            }
        }

        // show-options
        {
            Element element = document.createElement("show-options");
            rootElement.appendChild(element);

            for (BoolPrefs boolPref : BoolPrefs.values()) {
                Element optionElement = document.createElement("show-option");
                element.appendChild(optionElement);

                addSimpleTextNode(document, optionElement, "key", boolPref.name(), null);
                addSimpleTextNode(document, optionElement, "value", Boolean.toString(get_bool_pref(boolPref.name())), null);
            }
        }

        // bundle name (if any):
        if (bundle != null)
            addSimpleTextNode(document, rootElement, "bundle-name", bundle.getName(), null);

        // create XML file:

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            logAndThrow(4, "Could not create transformer:" + e.getMessage());
        }
        assert transformer != null;

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        FileWriter fileWriter = null;
        try {
            File file = new File(outputFileNameAndPath);
            fileWriter = new FileWriter(file, false);
        } catch (IOException e) {
            logAndThrow(5, "Could not create output file: " + e.getMessage());
        }
        assert fileWriter != null;

        StreamResult streamResult = new StreamResult(fileWriter);
        Source source = new DOMSource(document);
        try {
            transformer.transform(source, streamResult);

            fileWriter.flush();
            fileWriter.close();
        } catch (TransformerException e) {
            logAndThrow(6, "Could not transform document: " + e.getMessage());
        } catch (IOException e) {
            logAndThrow(7, "Exception creating report xml file:" + e.getMessage());
        }
    }
}
