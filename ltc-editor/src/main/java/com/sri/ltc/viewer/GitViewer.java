/**
 ************************ 80 columns *******************************************
 * GitViewer
 *
 * Created on Jul 29, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.viewer;

import com.sri.ltc.git.Commit;
import com.sri.ltc.git.Remote;
import com.sri.ltc.logging.LevelOptionHandler;
import com.sri.ltc.logging.LogConfiguration;
import com.sri.ltc.server.LTCserverImpl;
import com.sri.ltc.server.LTCserverInterface;
import com.wordpress.tips4java.TextLineNumber;
import org.apache.xmlrpc.XmlRpcException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static java.awt.datatransfer.DataFlavor.javaJVMLocalObjectMimeType;
import static java.awt.datatransfer.DataFlavor.stringFlavor;

/**
 * @author linda
 */
@SuppressWarnings("serial")
public class GitViewer extends JFrame {

    // static initializations
    private final Preferences preferences = Preferences.userNodeForPackage(this.getClass());
    private final static String KEY_LAST_DIR = "last directory";
    private final static String KEY_LAST_DIVIDER_H = "last H divider location";
    private final static String KEY_LAST_DIVIDER_V = "last V divider location";
    private final static String KEY_LAST_WIDTH = "last width of window";
    private final static String KEY_LAST_HEIGHT = "last height of window";
    private final static String KEY_LAST_X = "last X of window";
    private final static String KEY_LAST_Y = "last Y of window";
    static final Logger LOGGER = Logger.getLogger(GitViewer.class.getName());
    private static DataFlavor DATE_FLAVOR;
    static {
        try {
            DATE_FLAVOR = new DataFlavor(javaJVMLocalObjectMimeType + ";class=java.util.Date");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private final LTCSession session = new LTCSession(this);

    // initializing GUI components
    private final LatexPane textPane = new LatexPane();
    private final AuthorListModel authorModel = new AuthorListModel(session);
    private final CommitTableModel commitModel = new CommitTableModel();
    private final SelfComboBoxModel selfModel = new SelfComboBoxModel(textPane, authorModel, session);
    private final JButton pullButton = new JButton("Pull");
    private final JButton pushButton = new JButton("Push");
    private final RemoteComboBoxModel remoteModel = new RemoteComboBoxModel(session, pushButton, pullButton);
    private final JFileChooser fileChooser = new JFileChooser();
    private final JTextField fileField = new JTextField();
    private final Action updateAction = new AbstractAction("Update") {
        private static final long serialVersionUID = -7081121785169995463L;
        public void actionPerformed(ActionEvent event) {
            String path = fileField.getText();
            try {
                // if empty path, close session (if any) and clear fields
                if ("".equals(path)) {
                    close();
                    return;
                }
                // non-empty path: now look at current status of session
                File file = new File(path);
                // if current session is valid, compare prior path to new one to decide whether to close and init
                // if current session is not valid, simply start a new one
                if (session.isValid()) {
                    if (!session.getCanonicalPath().equals(file.getCanonicalPath())) {
                        close();
                        session.startInitAndUpdate(file, dateField.getText(), revField.getText(), textPane.stopFiltering());
                    } else {
                        session.startUpdate(dateField.getText(), revField.getText(), textPane.stopFiltering());
                    }
                } else {
                    session.startInitAndUpdate(file, dateField.getText(), revField.getText(), textPane.stopFiltering());
                }
                // update file chooser and preference for next time:
                fileChooser.setCurrentDirectory(file.getParentFile());
                Preferences.userNodeForPackage(this.getClass()).put(KEY_LAST_DIR, file.getParent());
            } catch (Exception e) {
                clear();
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    };
    private final JButton updateButton = new JButton(updateAction); // not included but used programmatically
    private final JTextField dateField = new JTextField();
    private final JTextField revField = new JTextField();
    private final JTextField commitMsgField = new JTextField();
    private final JButton saveButton = new JButton(new AbstractAction("Save") {
        private static final long serialVersionUID = -6467093865092379559L;
        public void actionPerformed(ActionEvent event) {
            // disable editing and obtain current text/recent edits
            session.save(textPane.stopFiltering());
            // update commit list:
            commitModel.addOnDisk();
            textPane.startFiltering();
            saveButton.setEnabled(false); // set modified = false
        }
    });

    private void close() throws XmlRpcException {
        clear();
        // TODO: save unmarked text to file?  only if modified...
        session.close(textPane.stopFiltering());
    }

    protected void finishClose() {
        saveButton.setEnabled(false);
    }

    protected void finishInit(List<Object[]> authors, List<Object[]> commits, Object[] self) {
        authorModel.init(authors);
        commitModel.init(commits, true);
        selfModel.init(authors, self);
        saveButton.setEnabled(false); // start with modified = false
    }

    protected void finishUpdate(Map<Integer,Object[]> authors,
                                String text,
                                List<Integer[]> styles,
                                Set<String> sha1s,
                                List<Object[]> commits,
                                List<Object[]> remotes) {
        // update list of authors
        finishAuthors(new ArrayList<Object[]>(authors.values()));
        // update and markup text
        Map<Integer,Color> colors = new HashMap<Integer,Color>();
        for (Map.Entry<Integer,Object[]> entry : authors.entrySet())
            colors.put(entry.getKey(), Color.decode((String) entry.getValue()[2]));
        textPane.updateFromMaps(text, styles, colors);
        // update list of commits
        commitModel.init(commits, false);
        commitModel.update(sha1s);
        // update list of remotes
        remoteModel.update(remotes);
    }

    protected void finishAuthors(List<Object[]> authors) {
        authorModel.addAuthors(authors);
    }

    protected void finishCommit(Object[] last_commit) {
        commitMsgField.setText("");

        // update list of commits:
        if (last_commit == null)
            return;
        commitModel.removeOnDisk();
        commitModel.add(last_commit);
    }

    private void clear() {
        try {
            textPane.clearAndGetDocument();
        } catch (BadLocationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        authorModel.clear();
        commitModel.clear(true);
        selfModel.clear();
        dateField.setText("");
        revField.setText("");
        saveButton.setEnabled(false); // start with modified = false        
    }

    private void createUIComponents() {
        // file chooser
        String last_dir = Preferences.userNodeForPackage(this.getClass()).get(
                KEY_LAST_DIR, System.getProperty("user.dir"));
        fileChooser.setCurrentDirectory(new File(last_dir));

        // text fields
        fileField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateButton.doClick();
            }
        });
        dateField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateButton.doClick();
            }
        });
        // customize drag'n drop
        dateField.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                if (support.isDataFlavorSupported(DATE_FLAVOR))
                    return true;
                return false;
            }
            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support))
                    return false;

                // Fetch the Transferable and its data
                try {
                    Date data = (Date) support.getTransferable().getTransferData(DATE_FLAVOR);
                    // insert data
                    dateField.setText(Commit.FORMATTER.format(data));
                    // signal success
                    return true;
                } catch (UnsupportedFlavorException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
                return false;
            }
        });
        revField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateButton.doClick();
            }
        });
        // customize drag'n drop
        revField.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                if (!support.isDataFlavorSupported(DataFlavor.stringFlavor))
                    return false;
                return true;
            }
            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support))
                    return false;
                // Fetch the Transferable and its data
                try {
                    String data = (String) support.getTransferable().getTransferData(stringFlavor);
                    // insert data
                    revField.setText(data);
                    // signal success
                    return true;
                } catch (UnsupportedFlavorException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
                return false;
            }
        });
    }

    private JPanel createPanes() {

        // 1) file panel
        JPanel filePane = new JPanel(new BorderLayout(5, 0));
        filePane.add(new JLabel("File:"), BorderLayout.LINE_START);
        filePane.add(fileField, BorderLayout.CENTER);
        filePane.add(new JButton(new AbstractAction("Choose...") {
            private static final long serialVersionUID = 138311848972917973L;
            public void actionPerformed(ActionEvent e) {
                if (fileChooser.showOpenDialog(GitViewer.this) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    fileField.setText(file.getAbsolutePath());
                    updateButton.doClick();
                }
            }
        }), BorderLayout.LINE_END);

        // 2) latex panel
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(800, 300));
        TextLineNumber tln = new TextLineNumber(textPane);
        tln.setCurrentLineForeground(Color.black); // no highlighting
        scrollPane.setRowHeaderView(tln);

        // 3) split pane for filtering and content tracking
        final JSplitPane splitPaneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createFilteringPane(), createContentTrackingPane());
        splitPaneH.setDividerLocation(preferences.getInt(KEY_LAST_DIVIDER_H, 0));
        splitPaneH.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent e) {
                        preferences.putInt(KEY_LAST_DIVIDER_H, splitPaneH.getDividerLocation());
                        LOGGER.config("Divider location: "+splitPaneH.getDividerLocation());
                    }});
        splitPaneH.setBorder(null);

        // 3) split pane for latex panel and horizontal split pane
        final JSplitPane splitPaneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                scrollPane, splitPaneH);
        splitPaneV.setDividerLocation(preferences.getInt(KEY_LAST_DIVIDER_V, 0));
        splitPaneV.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent e) {
                        preferences.putInt(KEY_LAST_DIVIDER_V, splitPaneV.getDividerLocation());
                        LOGGER.config("Vertical divider location: "+splitPaneV.getDividerLocation());
                    }});
        splitPaneV.setBorder(null);

        // 4) content pane
        JPanel contentPane = new JPanel(new BorderLayout(0, 5));
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPane.add(filePane, BorderLayout.PAGE_START);
        contentPane.add(splitPaneV, BorderLayout.CENTER);
        return contentPane;
    }

    private JPanel createFilteringPane() {

        // 1) showing panel
        JPanel showPane = new JPanel();
        showPane.setLayout(new BoxLayout(showPane, BoxLayout.PAGE_AXIS));
        showPane.add(new JLabel("Showing:"));
        showPane.add(new ShowingCheckBox("deletions", LTCserverInterface.Show.DELETIONS, updateButton));
        showPane.add(new ShowingCheckBox("\"small\" changes", LTCserverInterface.Show.SMALL, updateButton));
        showPane.add(new ShowingCheckBox("changes in preamble", LTCserverInterface.Show.PREAMBLE, updateButton));
        showPane.add(new ShowingCheckBox("changes in comments", LTCserverInterface.Show.COMMENTS, updateButton));
        showPane.add(new ShowingCheckBox("changes in commands", LTCserverInterface.Show.COMMANDS, updateButton));
        final JCheckBox paraCheckBox = new JCheckBox("white space characters", textPane.getShowParagraphs());
        paraCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (paraCheckBox.isSelected()) {
                    textPane.getDocument().putProperty("show paragraphs","");
                } else {
                    textPane.getDocument().putProperty("show paragraphs",null);
                }
                textPane.repaint();
                textPane.setShowParagraphs(paraCheckBox.isSelected());
            }
        });
        showPane.add(paraCheckBox);

        // 2) authors panel
        JPanel authorPane = new JPanel(new BorderLayout(0,5));
        authorPane.add(new JLabel("Authors:"), BorderLayout.PAGE_START);
        final JList authorList = new JList(authorModel);
        authorList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        authorList.setVisibleRowCount(5);
        authorList.setCellRenderer(new AuthorCellRenderer());
        authorList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Object o = authorModel.getElementAt(authorList.locationToIndex(e.getPoint()));
                    if (o instanceof AuthorCell) {
                        AuthorCell ac = (AuthorCell) o;
                        Color newColor = JColorChooser.showDialog(GitViewer.this,
                                "Choose Author Color",
                                ac.getColor());
                        if (newColor != null) {
                            boolean changed = ac.setColor(newColor);
                            if (changed) {
                                session.colors(ac.author.name, ac.author.email, LTCserverImpl.convertToHex(newColor));
                                authorModel.fireChanged(ac); // propagate update to self combo
                                updateButton.doClick();
                            }
                        }
                    }
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(authorList);
        authorPane.add(scrollPane, BorderLayout.CENTER);
        final JPanel authorButtons = new JPanel();
        JButton button = (JButton) authorButtons.add(new JButton(new AbstractAction("Reset") {
            private static final long serialVersionUID = -7513335226809639324L;
            public void actionPerformed(ActionEvent e) {
                authorModel.resetAll();
                authorList.clearSelection();
            }
        }));
        button.setToolTipText("Reset All");
        final Component limitButton = authorButtons.add(new LimitingButton(
                "Limit", authorList, authorModel, true));
        final Component unlimitButton = authorButtons.add(new LimitingButton(
                "Unlimit", authorList, authorModel, false));
        limitButton.setEnabled(false);
        unlimitButton.setEnabled(false);
        authorList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    // enable or disable buttons based on whether anything selected
                    limitButton.setEnabled(authorList.getSelectedIndices().length > 0);
                    unlimitButton.setEnabled(authorList.getSelectedIndices().length > 0);
                }
            }
        });
        authorPane.add(authorButtons, BorderLayout.PAGE_END);

        // 3) date panel
        JPanel datePane = new JPanel(new BorderLayout(5,0));
        datePane.add(new JLabel("Until Date:"), BorderLayout.LINE_START);
        datePane.add(dateField, BorderLayout.CENTER);

        // 4) rev panel
        JPanel revPane = new JPanel(new BorderLayout(5,0));
        revPane.add(new JLabel("Until Revision:"), BorderLayout.LINE_START);
        revPane.add(revField, BorderLayout.CENTER);

        // layout
        JPanel filteringPane = new JPanel(new GridBagLayout());
        filteringPane.setBorder(BorderFactory.createTitledBorder(" Filtering "));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 5, 0, 5);
        c.fill = GridBagConstraints.BOTH;

        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 0;
        filteringPane.add(showPane, c);

        c.gridx = 1;
        c.weighty = 1.0;
        filteringPane.add(authorPane, c);

        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.8;
        c.weighty = 0.0;
        filteringPane.add(datePane, c);

        c.gridy = 2;
        filteringPane.add(revPane, c);

        c.gridy = 3;
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;
        filteringPane.add(updateButton, c);

        return filteringPane;
    }

    @SuppressWarnings("unchecked")
    private JPanel createContentTrackingPane() {
        JPanel contentTrackingPane = new JPanel(new BorderLayout(0,5));
        contentTrackingPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(" Content Tracking "),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)));

        // 1) self combo box
        JPanel selfPane = new JPanel(new BorderLayout(0,0));
        selfPane.add(new JLabel("Self: "), BorderLayout.LINE_START);
        final JComboBox selfCombo = new JComboBox(selfModel);
        selfCombo.setEditable(true);
        selfCombo.setRenderer(new MyComboRenderer());
        selfCombo.setEditor(new SelfComboBoxEditor(authorModel));
        selfPane.add(selfCombo, BorderLayout.CENTER);
        contentTrackingPane.add(selfPane, BorderLayout.PAGE_START);

        // 2) commit graph
        JScrollPane scrollPane = new JScrollPane(new CommitTable(commitModel));
        contentTrackingPane.add(scrollPane, BorderLayout.CENTER);

        // 3) save and commit
        final JButton commitButton = new JButton(new AbstractAction("Commit") {
            private static final long serialVersionUID = -6467093865092379559L;
            public void actionPerformed(ActionEvent e) {
                session.commit(commitMsgField.getText(), saveButton);
            }
        });
        commitButton.setEnabled(false);
        commitMsgField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                commitButton.setEnabled(e.getDocument().getLength()>0);
            }
            public void removeUpdate(DocumentEvent e) {
                commitButton.setEnabled(e.getDocument().getLength()>0);
            }
            public void changedUpdate(DocumentEvent e) {
                commitButton.setEnabled(e.getDocument().getLength()>0);
            }
        });
        commitMsgField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                commitButton.doClick();
            }
        });
        // enable save button upon first change
        saveButton.setEnabled(false);
        textPane.getDocumentFilter().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() instanceof java.util.List) {
                    if (!saveButton.isEnabled() && !((java.util.List) e.getSource()).isEmpty()) {
                        saveButton.setEnabled(true);
                        commitModel.updateFirst(LTCserverInterface.MODIFIED); // update commit list to "on disk" if not already
                    }
                }
            }
        });
        JPanel commitPane = new JPanel(new GridBagLayout());
        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridx = 0;
        commitPane.add(saveButton, c1);
        c1.gridx = 1;
        commitPane.add(commitButton, c1);
        c1.fill = GridBagConstraints.HORIZONTAL;
        c1.weightx = 1.0; // combo box gets all space
        c1.gridx = 2;
        commitPane.add(commitMsgField, c1);

        // 4) remotes and push/pull
        final JComboBox remoteCombo = new JComboBox(remoteModel);
        remoteCombo.setEditable(true);
        remoteCombo.setRenderer(new MyComboRenderer());
        remoteCombo.setEditor(new RemoteComboBoxEditor());
        JPanel remotePane = new JPanel(new GridBagLayout());
        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 0;
        remotePane.add(new JLabel("Remote: "), c2);
        // two buttons:
        pullButton.setAction(new PushOrPullAction("Pull", remoteCombo, true));
        c2.gridx = 2;
        remotePane.add(pullButton, c2);
        pushButton.setAction(new PushOrPullAction("Push", remoteCombo, false));
        c2.gridx = 3;
        remotePane.add(pushButton, c2);
        // combo box:
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.weightx = 1.0; // combo box gets all space
        c2.gridx = 1;
        remotePane.add(remoteCombo, c2);

        // combine 3) and 4) into one pane using BoxLayout
        JPanel boxedPane = new JPanel();
        boxedPane.setLayout(new BoxLayout(boxedPane, BoxLayout.PAGE_AXIS));
        boxedPane.add(commitPane);
        boxedPane.add(remotePane);
        contentTrackingPane.add(boxedPane, BorderLayout.PAGE_END);

        return contentTrackingPane;
    }

    public GitViewer() {
        super("LTC Editor");

        // create UI components and put everything together
        createUIComponents();
        JPanel contentPane = createPanes();
        contentPane.setOpaque(true); //content panes must be opaque
        setContentPane(contentPane);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent windowEvent) {
                GitViewer.this.setBounds(
                        preferences.getInt(KEY_LAST_X, 0),
                        preferences.getInt(KEY_LAST_Y, 0),
                        preferences.getInt(KEY_LAST_WIDTH, 1000),
                        preferences.getInt(KEY_LAST_HEIGHT, 650));
                LOGGER.config("Window opened: "+GitViewer.this.getSize()+" at "+GitViewer.this.getLocation());
                // after this resizing and moving events can now be recorded:
                GitViewer.this.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentMoved(ComponentEvent componentEvent) {
                        preferences.putInt(KEY_LAST_X, GitViewer.this.getX());
                        preferences.putInt(KEY_LAST_Y, GitViewer.this.getY());
                        LOGGER.config("Window position: "+GitViewer.this.getLocation());
                    }
                    @Override
                    public void componentResized(ComponentEvent componentEvent) {
                        preferences.putInt(KEY_LAST_WIDTH, GitViewer.this.getWidth());
                        preferences.putInt(KEY_LAST_HEIGHT, GitViewer.this.getHeight());
                        LOGGER.config("Window size: "+GitViewer.this.getSize());
                    }
                });
            }
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                // save size and location
                preferences.putInt(KEY_LAST_WIDTH, GitViewer.this.getWidth());
                preferences.putInt(KEY_LAST_HEIGHT, GitViewer.this.getHeight());
                preferences.putInt(KEY_LAST_X, GitViewer.this.getX());
                preferences.putInt(KEY_LAST_Y, GitViewer.this.getY());
                LOGGER.config("Window closing: "+GitViewer.this.getSize()+" at "+GitViewer.this.getLocation());
            }
        });
    }

    private void setFile(String path) {
        fileField.setText(path);
        updateButton.doClick(); // crude way to invoke ENTER on JTextField
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     *
     * @param frame Viewer instance to be displayed
     */
    private static void createAndShowGUI(JFrame frame) {
        //Create and set up the window.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    private static void printUsage(PrintStream out, CmdLineParser parser) {
        out.println("usage: java -cp ... com.sri.ltc.viewer.GitViewer [options...] [FILE] \nwith:");
        parser.printUsage(out);
    }

    public static void main(String[] args) {
        // parse arguments
        CmdLineParser.registerHandler(Level.class, LevelOptionHandler.class);
        final GitViewerOptions options = new GitViewerOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            printUsage(System.err, parser);
            return;
        }

        if (options.displayHelp) {
            printUsage(System.out, parser);
            System.exit(1);
        }

        // configure logging
        try {
            LogConfiguration logConfig = new LogConfiguration();
            logConfig.setProperty("java.util.logging.ConsoleHandler.level",options.consoleLogLevel.getName());
            LogManager.getLogManager().readConfiguration(logConfig.asInputStream());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot configure logging", e);
        }

        final GitViewer frame = new GitViewer();

        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createAndShowGUI(frame);
                }
            });
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (InvocationTargetException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        // process optional argument
        if (options.file != null)
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    frame.setFile(options.file.getAbsolutePath());
                }
            });
    }

    static class GitViewerOptions {
        @Option(name="-l", usage="set console log level")
        Level consoleLogLevel = Level.CONFIG;

        @Option(name="-h", usage="display usage and exit")
        boolean displayHelp = false;

        @Argument(required = false, metaVar = "FILE", usage = "load given file to track changes")
        File file;
    }

    private class PushOrPullAction extends AbstractAction {

        private final JComboBox comboBox;
        private final boolean isPull;

        PushOrPullAction(String s, JComboBox comboBox, boolean pull) {
            super(s);
            this.comboBox = comboBox;
            isPull = pull;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            Object o = comboBox.getSelectedItem();
            if (o instanceof Remote) {
                Remote r = (Remote) o;
                String repository = r.isAlias()?r.name:r.url;
                session.pullOrPush(repository, isPull, dateField.getText(), revField.getText(), textPane.stopFiltering());
            }
        }
    }
}
