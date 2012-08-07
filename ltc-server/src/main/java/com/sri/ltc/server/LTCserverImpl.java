/**
 ************************ 80 columns *******************************************
 * LTCserverImpl
 *
 * Created on May 17, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.server;

import com.sri.ltc.ProgressReceiver;
import com.sri.ltc.filter.Author;
import com.sri.ltc.filter.Filtering;
import com.sri.ltc.git.Commit;
import com.sri.ltc.git.LimitedHistory;
import com.sri.ltc.git.Remote;
import com.sri.ltc.git.Self;
import com.sri.ltc.latexdiff.*;
import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.RepositoryFactory;
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
import java.net.URI;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author linda
 */
public final class LTCserverImpl implements LTCserverInterface {

    private final Preferences preferences = Preferences.userNodeForPackage(this.getClass());
    private final static List<Color> defaultColors = new ArrayList<Color>(Arrays.asList(
            Color.blue, Color.red, Color.green, Color.magenta, Color.orange, Color.cyan));
    private final static String KEY_COLOR = "author-color:";
    private final ProgressReceiver progressReceiver;
    private static String version = "<UNKNOWN>";

    public static String getVersion() {
        return version;
    }

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

    public int hello() {
        LOGGER.info("Server: Hello World!");
        return 42;
    }

    // TODO: apply locks to make block atomic...

    private final static Logger LOGGER = Logger.getLogger(LTCserverImpl.class.getName());
    static {
        // obtain version information from Maven meta-information
        try {
            InputStream inputStream = LTCserverImpl.class.getClassLoader().getResourceAsStream("META-INF/maven/com.sri.ltc/ltc-server/pom.properties");
            if (inputStream != null) {
                Properties pomProperties = new Properties();
                pomProperties.load(inputStream);
                version = pomProperties.getProperty("version", version);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot obtain version information", e);
        }
    }

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

    public int init_session(String path) throws XmlRpcException {
        if (path == null)
            logAndThrow(1,"Given path is NULL");

        LOGGER.info("Server: init_session with file \""+path+"\" called.");

        // is given path a valid file?
        File file = new File(path);
        if (!file.canRead())
            logAndThrow(2,"Cannot read given file");

        try {
            Repository repository = RepositoryFactory.fromPath(file);
        } catch (IOException e) {
            logAndThrow(3, "Could not create repository at " + file + " Exception: " + e.getMessage());
        }
        updateProgress(3);

        try {
            // test whether file tracked under git
            GitFile gitFile = dotGit.getWorkingTree().getFile(file);
            switch (gitFile.getStatus()) {
                case UNTRACKED:
                    logAndThrow(4,"Given file not tracked under git");
                case DELETED:
                    logAndThrow(8,"Given file deleted or deleted to commit under git");
            }
            updateProgress(10);

            return SessionManager.createSession(gitFile);
        } catch (JavaGitException e) {
            logAndThrow(6,"JavaGitException during git file creation: "+e.getMessage());
        } catch (IOException e) {
            logAndThrow(7,"IOException during git file creation: "+e.getMessage());
        } catch (ParseException e) {
            logAndThrow(8,"ParseException during git file creation: "+e.getMessage()+" (@"+e.getErrorOffset()+")");
        } catch (BadLocationException e) {
            logAndThrow(9,"BadLocationException during git file creation: "+e.getMessage());
        }

        return -1;
    }

    @SuppressWarnings("unchecked")
    public Map close_session(int sessionID, String currentText, List deletions, int caretPosition) throws XmlRpcException {
        Session session = SessionManager.removeSession(sessionID);
        if (session == null)
            logAndThrow(1,"Cannot close session with given ID");

        LOGGER.info("Server: close_session for file \""+session.gitFile.getFile().getAbsolutePath()+"\", "+
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

        LOGGER.info("Server: save_file to file \""+session.gitFile.getFile().getAbsolutePath()+"\" "+
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
            BufferedWriter out = new BufferedWriter(new FileWriter(session.gitFile.getFile(), false));
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

        LOGGER.info("Server: get_changes for file \""+session.gitFile.getFile().getAbsolutePath()+"\", "+
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

        // obtain file history from GIT, disk, and session (obeying any filters):
        List<ReaderWrapper> readers = null;
        List<Author> authors = null;
        List<String> sha1 = new ArrayList<String>();
        try {
            // create history with limits and obtain SHA1s, authors, and readers:
            LimitedHistory history = new LimitedHistory(session.gitFile,
                    session.getLimitedAuthors(),
                    session.getLimitDate(),
                    session.getLimitRev());
            updateProgress(12);
            for (Commit commit : history.getCommitsList())
                sha1.add(commit.sha1);
            authors = history.getAuthorsList();
            updateProgress(15);
            readers = history.getReadersList();
            updateProgress(45);

            Author self = new Self(session.gitFile).getSelf();
            // add file on disk to list, if modified or new but not committed yet
            switch (session.gitFile.getStatus()) {
                case ADDED:
                case MODIFIED:
                case UPDATED:
                    ReaderWrapper fileReader = new FileReaderWrapper(session.gitFile.getFile().getCanonicalPath());
                    if (authors.size() > 0 && authors.get(authors.size()-1).equals(self)) {
                        // replace last reader and SHA1
                        readers.remove(readers.size()-1);
                        sha1.remove(sha1.size()-1);
                    } else
                        // add self as author
                        authors.add(self);
                    readers.add(fileReader);
                    sha1.add(LTCserverInterface.ON_DISK);
            }
            // add current text from editor, if modified since last save:
            if (isModified) {
                ReaderWrapper currentTextReader = new StringReaderWrapper(currentText);
                if (authors.size() > 0 && authors.get(authors.size()-1).equals(self)) {
                    // replace last reader and SHA1
                    readers.remove(readers.size()-1);
                    sha1.remove(sha1.size()-1);
                } else
                    // add self as author
                    authors.add(self);
                readers.add(currentTextReader);
                sha1.add(LTCserverInterface.MODIFIED);
            }
            // if no readers, then use text from file and self as author
            if (readers.size() == 0 && authors.size() == 0) {
                authors.add(self);
                readers.add(new FileReaderWrapper(session.gitFile.getFile().getCanonicalPath()));
                sha1.add("");
            }
            updateProgress(47);
        } catch (JavaGitException e) {
            logAndThrow(4,"JavaGitException during log retrieval: "+e.getMessage());
        } catch (IOException e) {
            logAndThrow(5,"IOException during log retrieval: "+e.getMessage());
        } catch (ParseException e) {
            logAndThrow(6,"ParseException during log retrieval: "+e.getMessage()+" (@"+e.getErrorOffset()+")");
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
            Filtering filter = Filtering.getInstance();
            map = session.getAccumulate().perform(
                    readers.toArray(new ReaderWrapper[readers.size()]),
                    indices.toArray(new Integer[indices.size()]),
                    Change.buildFlags(
                            filter.getShowingStatus(LTCserverInterface.Show.DELETIONS),
                            filter.getShowingStatus(LTCserverInterface.Show.SMALL),
                            filter.getShowingStatus(LTCserverInterface.Show.PREAMBLE),
                            filter.getShowingStatus(LTCserverInterface.Show.COMMENTS),
                            filter.getShowingStatus(LTCserverInterface.Show.COMMANDS)),
                    caretPosition);
            map.put(LTCserverInterface.KEY_AUTHORS, mappedAuthors); // add current author map
            map.put(LTCserverInterface.KEY_SHA1, sha1); // add list of SHA1s used
            session.getAccumulate().removePropertyChangeListener(listener);
        } catch (IOException e) {
            logAndThrow(2,"IOException during change accumulation: "+e.getMessage());
        } catch (BadLocationException e) {
            logAndThrow(3,"BadLocationException at "+e.offsetRequested()+" during change accumulation: "+e.getMessage());
        }
        updateProgress(90);

        return map;
    }

    public int commit_file(int sessionID, String message) throws XmlRpcException {
        Session session = getSession(sessionID);

        if (message == null || "".equals(message))
            logAndThrow(2, "Cannot commit file with an empty message");

        LOGGER.info("Server: commit_file to file \""+session.gitFile.getFile().getAbsolutePath()+"\" called.");

        try {
            GitCommitResponse response = Factory.createGitCommit().commitOnly(session.gitFile.getWorkingTree().getPath(),
                    message,
                    session.gitFile.getRelativePath());

            if (response.isError()) {
                String output = response.getOutput();
                if (output.startsWith("nothing to commit"))
                    logAndThrow(5, "Nothing to commit");
                else
                    logAndThrow(6, "Error during git commit: "+output);
            }
        } catch (JavaGitException e) {
            logAndThrow(4,"JavaGitException during git commit: "+e.getMessage());
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
        } catch (JavaGitException e) {
            logAndThrow(3,"JavaGitException during retrieval of commit graph: "+e.getMessage());
        } catch (IOException e) {
            logAndThrow(4,"IOException during retrieval of commit graph: "+e.getMessage());
        }
        return null;
    }

    public Object[] get_self(int sessionID) throws XmlRpcException {
        Session session = getSession(sessionID);
        try {
            return concatAuthorAndColor(new Self(session.gitFile).getSelf());
        } catch (IllegalArgumentException e) {
            return new Object[0]; // if self is undefined
        } catch (JavaGitException e) {
            logAndThrow(2,"JavaGitException during self retrieval: "+e.getMessage());
        } catch (IOException e) {
            logAndThrow(3,"IOException during self retrieval: "+e.getMessage());
        }
        return null;
    }

    public Object[] set_self(int sessionID, String name, String email) throws XmlRpcException {
        Session session = getSession(sessionID);
        try {
            Author a = new Author(name, email, null);
            new Self(session.gitFile).setSelf(a);
            session.addAuthors(Collections.singleton(a));
        } catch (IllegalArgumentException e) {
            logAndThrow(4,"Cannot create author to set as self with given arguments: "+e.getMessage());
        } catch (JavaGitException e) {
            logAndThrow(5,"JavaGitException during setting self: "+e.getMessage());
        } catch (IOException e) {
            logAndThrow(6,"IOException during setting self: "+e.getMessage());
        }
        return get_self(sessionID);
    }

    public Object[] reset_self(int sessionID) throws XmlRpcException {
        Session session = getSession(sessionID);
        try {
            new Self(session.gitFile).resetSelf();
        } catch (JavaGitException e) {
            logAndThrow(4,"JavaGitException during resetting self: "+e.getMessage());
        } catch (IOException e) {
            logAndThrow(5,"IOException during resetting self: "+e.getMessage());
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

    public boolean get_show(String key) throws XmlRpcException {
        try {
            Show show = Show.valueOf(key);
            return Filtering.getInstance().getShowingStatus(show);
        } catch (IllegalArgumentException e) {
            logAndThrow(1, e.getMessage());
        }
        return false;
    }

    public int set_show(String key, boolean value) throws XmlRpcException {
        try {
            Show show = Show.valueOf(key);
            Filtering.getInstance().setShowingStatus(show, value);
            LOGGER.info("Turning show of "+key+(value?" on.":" off."));
        } catch (IllegalArgumentException e) {
            logAndThrow(1, e.getMessage());
        }
        return 0;
    }

    public int reset_show() {
        Filtering.getInstance().resetShowingStatus();
        LOGGER.info("Resetting all show states to default.");
        return 0;
    }

    public Object[] get_show_items() {
        List<String> names = new ArrayList<String>();
        for (Show show : LTCserverInterface.Show.values())
            names.add(show.name());
        return names.toArray();
    }

    @Override
    public int add_remote(int sessionID, String name, String url) throws XmlRpcException {
        Session session = getSession(sessionID);
        try {
            if (session.getRemotes().addRemote(name, url) != 0)
                logAndThrow(3, "Underlying git-remote command exited with non-zero code.");
        } catch (JavaGitException e) {
            logAndThrow(2, "JavaGitException while adding a remote: "+e.getMessage());
        }
        return 0;
    }

    @Override
    public int rm_remote(int sessionID, String name) throws XmlRpcException {
        Session session = getSession(sessionID);
        try {
            if (session.getRemotes().rmRemote(name) != 0)
                logAndThrow(3, "Underlying git-remote command exited with non-zero code.");
        } catch (JavaGitException e) {
            logAndThrow(2, "JavaGitException while removing a remote: "+e.getMessage());
        }
        return 0;
    }

    @Override
    public List get_remotes(int sessionID) throws XmlRpcException {
        Session session = getSession(sessionID);
        List<String[]> remotes = new ArrayList<String[]>();
        try {
            for (Remote remote : session.getRemotes().updateAndGetRemotes())
                remotes.add(remote.toArray());
        } catch (JavaGitException e) {
            logAndThrow(2, "JavaGitException while obtaining remotes: "+e.getMessage());
        }
        return remotes;
    }

    @Override
    public String push(int sessionID, String repository) throws XmlRpcException {
        Session session = getSession(sessionID);

        LOGGER.info("Server: push to repository \""+repository+"\" called.");

        try {
            return session.getRemotes().push(repository);
        } catch (JavaGitException e) {
            logAndThrow(2, "JavaGitException while removing a remote: "+e.getMessage());
        }
        return "";
    }

    @Override
    public String pull(int sessionID, String repository) throws XmlRpcException {
        Session session = getSession(sessionID);

        LOGGER.info("Server: pull from repository \""+repository+"\" called.");

        try {
            return session.getRemotes().pull(repository);
        } catch (JavaGitException e) {
            logAndThrow(2, "JavaGitException while removing a remote: "+e.getMessage());
        }
        return "";
    }

    @Override
    public String create_bug_report(int sessionID, String message, String outputDirectory) throws XmlRpcException {
        create_bug_report_xml(sessionID, message, new File(outputDirectory, "report.xml").toString());
        return ""; // TODO: return path to zip file
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

        Author author = new Author(authorName, authorEmail, null);
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

        Author author = new Author(authorName, authorEmail, null);
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
            preferences.remove(getColorKey(new Author(authorName, authorEmail, null)));
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

    private void addSimpleTextNode(Document document, Element parent, String elementName, String elementText) {
        Element element = document.createElement(elementName);
        parent.appendChild(element);

        org.w3c.dom.Text text = document.createTextNode(elementText);
        element.appendChild(text);
    }

    private void addAuthor(Document document, Element parent, String elementName, Author author) {
        Element authorElement = document.createElement(elementName);
        parent.appendChild(authorElement);

        addSimpleTextNode(document, authorElement, "name", author.name);
        addSimpleTextNode(document, authorElement, "email", author.email);
    }

    private void addAuthorLimit(Document document, Element parent, String elementName, Set<Author> authorsList) {
        Element element = document.createElement(elementName);
        parent.appendChild(element);

        for (Author author : authorsList) {
            addAuthor(document, element, "author", author);
        }
    }

    private void addShowOptions(Document document, Element parent, String elementName, Set<Author> authorsList) {
        Element element = document.createElement(elementName);
        parent.appendChild(element);

        for (Author author : authorsList) {
            addAuthor(document, element, "author", author);
        }
    }

    private void create_bug_report_xml(int sessionID, String message, String outputFileNameAndPath) throws XmlRpcException {
        LOGGER.fine("Server: creating bug report as " + outputFileNameAndPath);

        Session session = getSession(sessionID);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            logAndThrow(1, "Could not create document builder:" + e.getMessage());
        }

        assert documentBuilder != null;
        org.w3c.dom.Document document = documentBuilder.newDocument();

        Element rootElement = document.createElement("bug-report");
        document.appendChild(rootElement);

        addSimpleTextNode(document, rootElement, "user-message", message);
        addSimpleTextNode(document, rootElement, "relative-file-path", session.gitFile.getRelativePath());

        // filters
        {
            Element element = document.createElement("filters");
            rootElement.appendChild(element);

            addSimpleTextNode(document, element, "revision", session.getLimitRev());
            addSimpleTextNode(document, element, "date", session.getLimitDate());
            addAuthorLimit(document, element, "authors", session.getLimitedAuthors());
        }

        // active-revisions
        {
            Element element = document.createElement("active-revisions");
            rootElement.appendChild(element);

            LimitedHistory history = null;
            try {
                history = new LimitedHistory(session.gitFile,
                        session.getLimitedAuthors(),
                        session.getLimitDate(),
                        session.getLimitRev());
            } catch (Exception e) {
                logAndThrow(2, "Could not create LimitedHistory:" + e.getMessage());
            }

            assert history != null;
            for (Commit commit : history.getCommitsList()) {
                 addSimpleTextNode(document, element, "sha", commit.sha1);
            }
        }

        // show-options
        {
            Element element = document.createElement("show-options");
            rootElement.appendChild(element);

            for (Show show : LTCserverInterface.Show.values()) {
                Element optionElement = document.createElement("show-option");
                element.appendChild(optionElement);

                addSimpleTextNode(document, optionElement, "key", show.name());
                addSimpleTextNode(document, optionElement, "value", Boolean.toString(get_show(show.name())));
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            logAndThrow(3, "Could not create transformer:" + e.getMessage());
        }
        assert transformer != null;

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(outputFileNameAndPath);
        } catch (IOException e) {
            logAndThrow(3, "Could not create output file:" + e.getMessage());
        }
        assert fileWriter != null;

        StreamResult streamResult = new StreamResult(fileWriter);
        Source source = new DOMSource(document);
        try {
            transformer.transform(source, streamResult);

            fileWriter.flush();
            fileWriter.close();
        } catch (TransformerException e) {
            logAndThrow(3, "Could not transform document:" + e.getMessage());
        } catch (IOException e) {
            logAndThrow(3, "Exception creating report xml file:" + e.getMessage());
        }
    }
}
