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

import com.sri.ltc.CommonUtils;
import com.sri.ltc.versioncontrol.Remote;
import com.sri.ltc.logging.LevelOptionHandler;
import com.sri.ltc.logging.LogConfiguration;
import com.sri.ltc.server.LTCserverImpl;
import com.sri.ltc.server.LTCserverInterface;
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
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static java.awt.datatransfer.DataFlavor.javaJVMLocalObjectMimeType;
import static java.awt.datatransfer.DataFlavor.stringFlavor;

/**
 * @author linda
 */
@SuppressWarnings("serial")
public final class LTCEditor extends LTCGui {

    // static initializations
    static {
        // first thing is to configure Mac OS X before AWT gets loaded:
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "LTC Editor");
    }
    private final Preferences preferences = Preferences.userRoot().node(this.getClass().getCanonicalName().replaceAll("\\.","/"));
    private final static String KEY_LAST_DIR = "last directory";
    private final static String KEY_LAST_DIVIDER_H = "last H divider location";
    static final Logger LOGGER = Logger.getLogger(LTCEditor.class.getName());
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
    private final AuthorListModel authorModel = new AuthorListModel(session);
    private final CommitTableModel commitModel = new CommitTableModel();
    private final SelfComboBoxModel selfModel = new SelfComboBoxModel(textPane, authorModel, session);
    private final JPanel cards = new JPanel(new CardLayout());
    private final SelfTextField selfField = new SelfTextField(authorModel);
    private final JButton pullButton = new JButton("Pull");
    private final JButton pushButton = new JButton("Push");
    private final RemoteComboBoxModel remoteModel = new RemoteComboBoxModel(session, pushButton, pullButton);
    private final JPanel remotePane = new JPanel(new GridBagLayout());
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
                        if (!close()) {
                            fileField.setText(session.getCanonicalPath()); // undo setting of file field
                            return;
                        }
                        clear();
                        session.startInitAndUpdate(file, dateField.getText(), revField.getText(), textPane.getCaretPosition());
                    } else {
                        session.startUpdate(dateField.getText(), revField.getText(), false, textPane.getText(), textPane.stopFiltering(), textPane.getCaretPosition());
                    }
                } else {
                    clear();
                    session.startInitAndUpdate(file, dateField.getText(), revField.getText(), textPane.getCaretPosition());
                }
                // update file chooser and preference for next time:
                fileChooser.setCurrentDirectory(file.getParentFile());
                preferences.put(KEY_LAST_DIR, file.getParent());
            } catch (Exception e) {
                clear();
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    };
    private final BugReportPanel bugReportPanel = new BugReportPanel();
    private final Action bugReportAction = new AbstractAction("Bug Report...") {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (session.isValid()) {
                // 1) open dialog to obtain DIRECTORY, optional COMMENT, and whether to INCLUDE REPO
                final Object[] options = {"Create", "Cancel"};
                final JOptionPane optionPane = new JOptionPane(
                        bugReportPanel,
                        JOptionPane.PLAIN_MESSAGE,
                        JOptionPane.OK_CANCEL_OPTION,
                        null,
                        options, options[0]);
                final JDialog dialog = optionPane.createDialog(getFrame(), "Create Bug Report");
                dialog.pack();
                dialog.setVisible(true);
                // 2) if user clicked "Create", call session and pop up final location upon finishing
                if (options[0].equals(optionPane.getValue())) {
                    BugReportPanel.Data data = bugReportPanel.getData();
                    if (data.directory == null || "".equals(data.directory))
                        JOptionPane.showMessageDialog(getFrame(),
                                "Cannot create bug report in empty directory",
                                "Error Creating Bug Report",
                                JOptionPane.ERROR_MESSAGE);
                    else
                        session.createBugReport(data.comment, data.repository, data.directory);
                }
            }
        }
    };
    private final DateField dateField = new DateField();
    private final JTextField revField = new JTextField();
    private final JTextField commitMsgField = new JTextField();
    private final JButton saveButton = new JButton(new AbstractAction("Save") {
        private static final long serialVersionUID = -6467093865092379559L;
        public void actionPerformed(ActionEvent event) {
            // disable editing and obtain current text/recent edits
            session.save(textPane.getText(), textPane.stopFiltering());
            textPane.getDocumentFilter().resetChanges(); // allow document listener to fire changes again
            // update commit list:
            commitModel.addOnDisk();
            textPane.startFiltering();
            saveButton.setEnabled(false); // set modified = false
        }
    });

    // return false if close was canceled by user
    private boolean close() throws XmlRpcException {
        if (saveButton.isEnabled()) {
            // dialog if unsaved edits
            switch (JOptionPane.showConfirmDialog(getFrame(),
                    "Save file first before closing?",
                    "Closing while unsaved edits",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE)) {
                case JOptionPane.CANCEL_OPTION:
                    return false;
                case JOptionPane.YES_OPTION:
                    saveButton.doClick();
            }
        }
        clear();
        session.close();
        textPane.stopFiltering();
        return true;
    }

    protected void finishClose() {
        saveButton.setEnabled(false);
    }

    protected void finishInit(List<Object[]> authors, List<Object[]> commits, Object[] self, String VCS) {
        // decide here whether we have SVN or GIT and change GUI elements accordingly
        LTCserverInterface.VersionControlSystems vcs = null;
        try {
            vcs = LTCserverInterface.VersionControlSystems.valueOf(VCS);
        } catch (IllegalArgumentException e) {
            // VCS was not a proper name: make svn the default
            vcs = LTCserverInterface.VersionControlSystems.SVN;
        }
        // switch self panel:
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, vcs.name());
        // enable or disable remote panel
        enableComponents(remotePane, LTCserverInterface.VersionControlSystems.GIT.equals(vcs));

        // other initializations:
        authorModel.init(authors);
        commitModel.init(commits, true);
        selfModel.init(authors, self);
        selfField.setSelf(self);
        saveButton.setEnabled(false); // start with modified = false
        setFile(session.getCanonicalPath(), false);
    }

    protected void finishUpdate(Map<Integer, Object[]> authors,
                                String text,
                                List<Integer[]> styles,
                                int caretPosition,
                                List<String> orderedIDs,
                                List<Object[]> commits,
                                List<Object[]> remotes) {
        // update list of authors
        finishAuthors(new ArrayList<Object[]>(authors.values()));
        // update and markup text
        Map<Integer,Color> colors = new HashMap<Integer,Color>();
        for (Map.Entry<Integer,Object[]> entry : authors.entrySet())
            colors.put(entry.getKey(), Color.decode((String) entry.getValue()[2]));
        textPane.updateFromMaps(text, styles, colors, caretPosition, orderedIDs);
        // update list of commits
        commitModel.init(commits, false);
        commitModel.update(new HashSet<String>(orderedIDs));
        // update list of remotes
        remoteModel.update(remotes);
        // update date field
        String date = dateField.getText();
        if (!date.isEmpty())
            try {
                dateField.setText(CommonUtils.serializeDate(CommonUtils.deSerializeDate(date)));
            } catch (ParseException e) {
                // ignore and reset to old value:
                dateField.setText(date);
            }
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
        fileField.setText(fileChooser.getCurrentDirectory().getAbsolutePath()+"/");
        try {
            textPane.clearAndGetDocument();
        } catch (BadLocationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        authorModel.clear();
        commitModel.clear(true);
        selfModel.clear();
        selfField.setSelf(null);
        dateField.setText("");
        revField.setText("");
        saveButton.setEnabled(false); // start with modified = false        
    }

    private void createUIComponents() {
        // add action to update button
        getUpdateButton().setAction(updateAction);

        // file chooser
        fileChooser.setCurrentDirectory(new File(
                preferences.get(KEY_LAST_DIR, System.getProperty("user.dir"))));

        // text fields
        fileField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getUpdateButton().doClick();
            }
        });
        dateField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getUpdateButton().doClick();
            }
        });
        // customize drag'n drop
        dateField.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                if (support.isDataFlavorSupported(DATE_FLAVOR) ||
                        support.isDataFlavorSupported(DataFlavor.stringFlavor))
                    return true;
                return false;
            }
            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support))
                    return false;
                // Fetch the Transferable and its data
                try {
                    Transferable t = support.getTransferable();
                    DataFlavor[] flavors = t.getTransferDataFlavors();
                    if (flavors == null || flavors.length < 1)
                        return false; // cannot get flavor
                    Class representationClass = flavors[0].getRepresentationClass();
                    if (Date.class.equals(representationClass)) {
                        Date data = (Date) t.getTransferData(DATE_FLAVOR);
                        dateField.setText(CommonUtils.serializeDate(data)); // insert data
                        return true; // signal success
                    }
                    if (String.class.equals(representationClass)) {
                        dateField.setText((String) t.getTransferData(stringFlavor));
                        return true; // signal success
                    }
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
                getUpdateButton().doClick();
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

    private JPanel createFilePane() {
        JPanel filePane = new JPanel(new BorderLayout(5, 0));
        filePane.add(new JLabel("File:"), BorderLayout.LINE_START);
        filePane.add(fileField, BorderLayout.CENTER);
        filePane.add(new JButton(new AbstractAction("Choose...") {
            private static final long serialVersionUID = 138311848972917973L;

            public void actionPerformed(ActionEvent e) {
                if (fileChooser.showOpenDialog(getFrame()) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    LTCEditor.this.setFile(file.getAbsolutePath(), true);
                }
            }
        }), BorderLayout.LINE_END);
        return filePane;
    }

    private JPanel createFilteringPane() {

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
                        Color newColor = JColorChooser.showDialog(getFrame(),
                                "Choose Author Color",
                                ac.getColor());
                        if (newColor != null) {
                            boolean changed = ac.setColor(newColor);
                            if (changed) {
                                session.colors(ac.author.name, ac.author.email, LTCserverImpl.convertToHex(newColor));
                                authorModel.fireChanged(ac); // propagate update to self combo
                                getUpdateButton().doClick();
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
        datePane.add(new JLabel("Start at date:"), BorderLayout.LINE_START);
        datePane.add(dateField, BorderLayout.CENTER);

        // 4) rev panel
        JPanel revPane = new JPanel(new BorderLayout(5,0));
        revPane.add(new JLabel("Start at revision:"), BorderLayout.LINE_START);
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
        c.weighty = 1.0;
        filteringPane.add(authorPane, c);

        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridy = 1;
        filteringPane.add(new BoolPrefCheckBox("condense authors",
                LTCserverInterface.BoolPrefs.COLLAPSE_AUTHORS,
                getUpdateButton()), c);

        c.weightx = 0.8;
        c.gridy = 2;
        filteringPane.add(datePane, c);

        c.gridy = 3;
        filteringPane.add(revPane, c);

        c.gridy = 4;
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;
        filteringPane.add(getUpdateButton(), c);

        // pane to include filtering and bug report button
        JPanel leftPane = new JPanel(new BorderLayout()); // no gaps
        leftPane.add(filteringPane, BorderLayout.CENTER);
        // configuring key binding to CMD-R / CTRL-R for bug report button:
        bugReportAction.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        JButton bugReportButton = new JButton(bugReportAction);
        bugReportButton.getActionMap().put("createBugReport", bugReportAction);
        bugReportButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                (KeyStroke) bugReportAction.getValue(Action.ACCELERATOR_KEY),
                "createBugReport");

        leftPane.add(bugReportButton, BorderLayout.PAGE_END);

        return leftPane;
    }

    @SuppressWarnings("unchecked")
    private JPanel createContentTrackingPane() {
        JPanel contentTrackingPane = new JPanel(new BorderLayout(0,5));
        contentTrackingPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(" Content Tracking "),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)));

        // 1) self combo box/label: as card layout
        final JPanel selfPane = new JPanel(new BorderLayout(0, 0));
        selfPane.add(new JLabel("Self: "), BorderLayout.LINE_START);
        final JComboBox selfCombo = new JComboBox(selfModel);
        selfCombo.setEditable(true);
        selfCombo.setRenderer(new MyComboRenderer());
        selfCombo.setEditor(new SelfComboBoxEditor(authorModel));
        cards.add(selfCombo, LTCserverInterface.VersionControlSystems.GIT.name());
        cards.add(selfField, LTCserverInterface.VersionControlSystems.SVN.name());
        selfPane.add(cards, BorderLayout.CENTER);
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
                if (!saveButton.isEnabled()) {
                    saveButton.setEnabled(true);
                    commitModel.updateFirst(LTCserverInterface.MODIFIED); // update commit list to "on disk" if not already
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
        enableComponents(remotePane, false); // by default disabled

        // combine 3) and 4) into one pane using BoxLayout
        JPanel boxedPane = new JPanel();
        boxedPane.setLayout(new BoxLayout(boxedPane, BoxLayout.PAGE_AXIS));
        boxedPane.add(commitPane);
        boxedPane.add(remotePane);
        contentTrackingPane.add(boxedPane, BorderLayout.PAGE_END);

        return contentTrackingPane;
    }

    private void createLowerRightPane(JPanel panel) {
        final JSplitPane splitPaneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createFilteringPane(), createContentTrackingPane());
        splitPaneH.setDividerLocation(preferences.getInt(KEY_LAST_DIVIDER_H, 0));
        splitPaneH.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent e) {
                        preferences.putInt(KEY_LAST_DIVIDER_H, splitPaneH.getDividerLocation());
                        LOGGER.config("Divider location: " + splitPaneH.getDividerLocation());
                    }
                });
        splitPaneH.setBorder(null);
        panel.add(splitPaneH, BorderLayout.CENTER);
    }

    private void enableComponents(Container container, boolean enable) {
        for (Component component : container.getComponents()) {
            component.setEnabled(enable);
            if (component instanceof Container)
                enableComponents((Container) component, enable);
        }
    }

    public LTCEditor() {
        super(true, "LTC Editor");

        createUIComponents();
        // add custom panels to content pane
        getContentPane().add(createFilePane(), BorderLayout.PAGE_START);
        createLowerRightPane(getLowerRightPane());
    }

    private void setFile(String path, boolean doUpdate) {
        fileField.setText(path);
        if (doUpdate)
            getUpdateButton().doClick(); // crude way to invoke ENTER on JTextField
    }

    private static void printUsage(PrintStream out, CmdLineParser parser) {
        out.println("usage: java -cp ... " + LTCEditor.class.getCanonicalName() + " [options...] [FILE] \nwith");
        parser.printUsage(out);
    }

    public static void main(String[] args) {
        // parse arguments
        CmdLineParser.registerHandler(Level.class, LevelOptionHandler.class);
        final LTCEditorOptions options = new LTCEditorOptions();
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

        if (options.displayLicense) {
            System.out.println("LTC is licensed under:\n\n" + CommonUtils.getLicense());
            return;
        }

        // configure logging
        try {
            LogConfiguration logConfig = new LogConfiguration();
            logConfig.setProperty("java.util.logging.ConsoleHandler.level",options.consoleLogLevel.getName());
            logConfig.setProperty("java.util.logging.FileHandler.level",options.consoleLogLevel.getName());
            logConfig.setProperty("java.util.logging.FileHandler.pattern","%h/.LTCEditor.log");
            LogManager.getLogManager().readConfiguration(logConfig.asInputStream());
            LOGGER.config("Logging configured to level " + options.consoleLogLevel.getName());
            LOGGER.config("Logging output written to file " + logConfig.getProperty("java.util.logging.FileHandler.pattern"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot configure logging", e);
        }

        final LTCEditor editor = new LTCEditor();

        if (options.resetDefaults) {
            try {
                LOGGER.config("Resetting preferences to defaults");
                editor.preferences.clear();
            } catch (BackingStoreException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "setting UI look & feel", e);
        }

        // customize for operating system:
        CommonUtils.customizeApp("/images/LTC-editor-icon.png");

        //creating and showing this application's GUI.
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createAndShowGUI(editor);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        // process optional argument
        if (options.file != null)
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    editor.setFile(options.file.getAbsolutePath(), true);
                }
            });
    }

    static class LTCEditorOptions {
        @Option(name="-l",usage="set console log level\nSEVERE, WARNING, INFO, CONFIG (default), FINE, FINER, FINEST")
        Level consoleLogLevel = Level.CONFIG;

        @Option(name="-h", usage="display usage and exit")
        boolean displayHelp = false;

        @Option(name="-c",usage="display copyright/license information and exit")
        boolean displayLicense = false;

        @Option(name = "-r", usage = "reset to default settings")
        boolean resetDefaults = false;

        @Argument(required=false, metaVar="FILE", usage="load given file to track changes")
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
                session.pullOrPush(repository, isPull, dateField.getText(), revField.getText(),
                        saveButton.isEnabled(), textPane.getText(), textPane.stopFiltering(), textPane.getCaretPosition());
            }
        }
    }

    private class DateField extends JTextField {
        @Override
        public void setText(String s) {
            super.setText(s);
            setCaretPosition(0);
        }
    }
}
