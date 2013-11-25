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
import com.sri.ltc.filter.Author;
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
import javax.swing.Timer;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
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
    static final Logger LOGGER = Logger.getLogger(LTCEditor.class.getName());
    private static DataFlavor DATE_FLAVOR;
    static {
        try {
            DATE_FLAVOR = new DataFlavor(javaJVMLocalObjectMimeType + ";class=java.util.Date");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    private static DataFlavor AUTHOR_FLAVOR;
    static {
        try {
            AUTHOR_FLAVOR = new DataFlavor(javaJVMLocalObjectMimeType + ";class=com.sri.ltc.filter.Author");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    private final static int CLICK_INTERVAL = (Integer)Toolkit.getDefaultToolkit().
            getDesktopProperty("awt.multiClickInterval");

    private final LTCSession session = new LTCSession(this);

    // initializing GUI components
    private final AuthorListModel authorModel = new AuthorListModel(session);
    private final CommitTableModel commitModel = new CommitTableModel();
    private final SelfComboBoxModel selfModel = new SelfComboBoxModel(session);
    private final JFileChooser fileChooser = new JFileChooser();
    private final JTextField fileField = new JTextField();
    private final Action updateAction = new AbstractAction("Update ("+'\u2318'+"U)") {
        @Override
        public void actionPerformed(ActionEvent event) {
            String path = fileField.getText();
            try {
                // if empty path, try to close session (if any) and clear fields, then return
                if ("".equals(path)) {
                    if (!close())
                        fileField.setText(session.getCanonicalPath()); // undo setting of file field
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
                        session.startInit(file);
                    } else {
                        session.startUpdate(dateField.getText(), revField.getText(), saveButton.isEnabled(),
                                textPane.getText(), textPane.stopFiltering(), textPane.getCaretPosition());
                    }
                } else {
                    clear();
                    session.startInit(file);
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
    private final Action bugReportAction = new AbstractAction("Bug Report... ("+'\u2318'+"R)") {
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
    private final JTextField authorField = new AuthorSetField(authorModel);
    private final DateField dateField = new DateField();
    private final JTextField revField = new JTextField();
    private final JButton saveButton = new JButton(new AbstractAction("Save ("+'\u2318'+"S)") {
        private static final long serialVersionUID = -6467093865092379559L;
        public void actionPerformed(ActionEvent event) {
            // disable editing and obtain current text/recent edits
            session.save(textPane.getText(), textPane.stopFiltering());
            textPane.getDocumentFilter().resetChanges(); // allow document listener to fire changes again
            // update commit list:
            commitModel.setFirstID(LTCserverInterface.ON_DISK);
            textPane.startFiltering();
            saveButton.setEnabled(false); // set modified = false
        }
    });

    // return false if close was canceled by user; otherwise (YES or NO) return true to proceed
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
        session.close();
        return true; // proceed with closing operation
    }

    protected void finishClose() {
        clear(); // this will also try to stop filtering
    }

    protected void finishInit(List<Object[]> authors, Object[] self) {
        // other initializations:
        authorModel.init(authors);
        commitModel.init(self);
        selfModel.init(authors, self);
        saveButton.setEnabled(false); // start with modified = false
        setFile(session.getCanonicalPath(), false);

        // start update:
        getUpdateButton().doClick();
    }

    protected void finishUpdate(Map<Integer, Object[]> authors,
                                String text,
                                List<Integer[]> styles,
                                int caretPosition,
                                List<String> orderedIDs,
                                List<Object[]> commits) {
        // update list of authors
        finishAuthors(new ArrayList<Object[]>(authors.values()), false); // don't run another update
        // update and markup text
        Map<Integer,Color> colors = new HashMap<Integer,Color>();
        for (Map.Entry<Integer,Object[]> entry : authors.entrySet())
            colors.put(entry.getKey(), Color.decode((String) entry.getValue()[2]));
        textPane.updateFromMaps(text, styles, colors, caretPosition, orderedIDs, commits);
        // update list of commits
        commitModel.update(commits, new HashSet<String>(orderedIDs));
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

    protected void finishAuthors(List<Object[]> authors, boolean doUpdate) {
        authorModel.addAuthors(authors);
        if (doUpdate)
            getUpdateButton().doClick();
    }

    protected void finishSetSelf(Object[] self) {
        commitModel.setSelf(self);
        // update authors and everything else:
        session.getAuthors();
    }

    private void clear() {
        fileField.setText(fileChooser.getCurrentDirectory().getAbsolutePath()+"/");
        try {
            textPane.clearAndGetDocument();
        } catch (BadLocationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        authorModel.clear();
        commitModel.clear(null);
        selfModel.clear();
        dateField.setText("");
        revField.setText("");
        saveButton.setEnabled(false); // start with modified = false        
    }

    private void createUIComponents() {
        // add action to update button
        getUpdateButton().setAction(updateAction);
        getUpdateButton().getActionMap().put(UPDATE_ACTION, updateAction);

        // file chooser
        fileChooser.setCurrentDirectory(new File(
                preferences.get(KEY_LAST_DIR, System.getProperty("user.dir"))));

        // text fields
        fileField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getUpdateButton().doClick();
            }
        });
        authorField.setEnabled(false); // TODO: re-enable once figuring out the editing!
        authorField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getUpdateButton().doClick();
            }
        });
        authorField.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                if (support.isDataFlavorSupported(AUTHOR_FLAVOR) ||
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
                    String authorName = "";
                    // first try author representation:
                    if (Author.class.equals(representationClass))
                        authorName = ((Author) t.getTransferData(AUTHOR_FLAVOR)).name;
                    // now try text representations:
                    DataFlavor bestTextFlavor = DataFlavor.selectBestTextFlavor(flavors);
                    if (bestTextFlavor != null)
                        authorName = ((String) t.getTransferData(stringFlavor)).trim();
                    // update text field if anything was successfully transfered:
                    if (!"".equals(authorName)) {
                        String currentText = authorField.getText().trim();
                        authorField.setText("".equals(currentText)?authorName:currentText+", "+authorName);
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
                    // now try text representations:
                    DataFlavor bestTextFlavor = DataFlavor.selectBestTextFlavor(flavors);
                    if (bestTextFlavor != null) {
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
        JButton chooseButton = new JButton(new AbstractAction("Choose... ("+'\u2318'+"O)") {
            private static final long serialVersionUID = 138311848972917973L;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fileChooser.showOpenDialog(getFrame()) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    LTCEditor.this.setFile(file.getAbsolutePath(), true);
                }
            }
        });
        // configuring key binding to CMD-O / CTRL-O for choose button:
        chooseButton.getActionMap().put("chooseAction", chooseButton.getAction());
        chooseButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                "chooseAction");
        filePane.add(chooseButton, BorderLayout.LINE_END);
        return filePane;
    }

    // for label + text field
    private JPanel createTextInputPane(String label, Component component) {
        JPanel panel = new JPanel(new BorderLayout(5,0));
        panel.add(new JLabel(label), BorderLayout.LINE_START);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFilteringPane() {
        JPanel filteringPane = new JPanel(new BorderLayout());

        // center pane: text boxes /w labels to limit by
        JPanel limitPane = new JPanel();
        limitPane.setLayout(new BoxLayout(limitPane, BoxLayout.PAGE_AXIS));
        limitPane.add(createTextInputPane("Limit to:", authorField));
        limitPane.add(createTextInputPane("Start at date:", dateField));
        limitPane.add(createTextInputPane("Start at revision:", revField));
        filteringPane.add(limitPane, BorderLayout.CENTER);

        // line end pane: check boxes
        JPanel checkboxesPane = new JPanel();
        checkboxesPane.setLayout(new BoxLayout(checkboxesPane, BoxLayout.PAGE_AXIS));
        checkboxesPane.add(new BoolPrefCheckBox("condense authors",
                LTCserverInterface.BoolPrefs.COLLAPSE_AUTHORS,
                getUpdateButton()));
        checkboxesPane.add(new BoolPrefCheckBox("allow similar colors",
                LTCserverInterface.BoolPrefs.ALLOW_SIMILAR_COLORS,
                getUpdateButton()));
        filteringPane.add(checkboxesPane, BorderLayout.LINE_END);

        // page end pane: buttons
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        // --- UPDATE button:
        buttonPane.add(getUpdateButton(), BorderLayout.LINE_START);
        // --- BUG REPORT button:
        // configuring key binding to CMD-R / CTRL-R for bug report button:
        bugReportAction.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        JButton bugReportButton = new JButton(bugReportAction);
        bugReportButton.getActionMap().put("createBugReport", bugReportAction);
        bugReportButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                (KeyStroke) bugReportAction.getValue(Action.ACCELERATOR_KEY),
                "createBugReport");
        buttonPane.add(bugReportButton, BorderLayout.LINE_END);
        // --- SAVE button:
        // enable save button upon first change
        saveButton.setEnabled(false);
        textPane.getDocumentFilter().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (!saveButton.isEnabled()) {
                    saveButton.setEnabled(true);
                    commitModel.setFirstID(LTCserverInterface.MODIFIED); // update commit list to "modified" if not already
                }
            }
        });
        // configuring key binding to CMD-S / CTRL-S for save button:
        saveButton.getActionMap().put("saveAction", saveButton.getAction());
        saveButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                "saveAction");
        buttonPane.add(saveButton);
        filteringPane.add(buttonPane, BorderLayout.PAGE_END);

        // 2) authors panel

//        JButton button = (JButton) authorButtons.add(new JButton(new AbstractAction("Reset") {
//            private static final long serialVersionUID = -7513335226809639324L;
//            public void actionPerformed(ActionEvent e) {
//                authorModel.resetAll();
//                authorList.clearSelection();
//            }
//        }));

//        final Component limitButton = authorButtons.add(new LimitingButton(
//                "Limit", authorList, authorModel, true));
//        final Component unlimitButton = authorButtons.add(new LimitingButton(
//                "Unlimit", authorList, authorModel, false));
//        limitButton.setEnabled(false);
//        unlimitButton.setEnabled(false);
//        authorList.addListSelectionListener(new ListSelectionListener() {
//            public void valueChanged(ListSelectionEvent e) {
//                if (!e.getValueIsAdjusting()) {
//                    // enable or disable buttons based on whether anything selected
//                    limitButton.setEnabled(authorList.getSelectedIndices().length > 0);
//                    unlimitButton.setEnabled(authorList.getSelectedIndices().length > 0);
//                }
//            }
//        });

        return filteringPane;
    }

    private void createLowerRightPane(JPanel panel) {
        // commit table
        final JTable table = new CommitTable(commitModel, authorModel);
        table.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
        // provide editing of authors (only first row though!)
        JComboBox selfCombo = new JComboBox(selfModel);
        selfCombo.setEditable(true);
        selfCombo.setRenderer(new SelfComboBoxRenderer());
        selfCombo.setEditor(new SelfComboBoxEditor(authorModel, textPane));
        selfCombo.setBorder(BorderFactory.createEmptyBorder());
        table.setDefaultEditor(Author.class, new DefaultCellEditor(selfCombo) {
            Timer timer;
            boolean wasDoubleClick = false;

            @Override
            public boolean isCellEditable(final EventObject e) {
                if (e instanceof MouseEvent) {
                    final MouseEvent event = (MouseEvent) e;
                    if (event.getClickCount() == 2) {
                        wasDoubleClick = true;
                        return true;
                    } else {
                        timer = new Timer(CLICK_INTERVAL, new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent evt) {
                                if (wasDoubleClick) {
                                    wasDoubleClick = false; // reset flag
                                } else {
                                    int row = ((JTable) event.getSource()).rowAtPoint(event.getPoint());
                                    int col = ((JTable) event.getSource()).columnAtPoint(event.getPoint());
                                    chooseAuthorColor(event.getComponent(), (Author) table.getValueAt(row, col));
                                }
                            }
                        });
                        timer.setRepeats(false);
                        timer.start();
                    }
                    return false;
                }
                return false;
            }
        });
        // listen to double-clicks in the author column:
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent event) {
                Point p = event.getPoint();
                int row = table.rowAtPoint(p);
                int col = table.columnAtPoint(p);
                final Object o = table.getValueAt(row, col);
                if (o instanceof Author) {
                    if (!table.isCellEditable(row, col) && event.getClickCount() == 1)
                        chooseAuthorColor(event.getComponent(), (Author) o);
                }
            }
        });

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(" Content Tracking & Filtering "),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)));
        panel.setLayout(new BorderLayout(0, 5));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(createFilteringPane(), BorderLayout.PAGE_END);
    }

    private void chooseAuthorColor(Component component, Author author) {
        AuthorCell ac = authorModel.getCellForAuthor(author);
        Color newColor = JColorChooser.showDialog(component,
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

    private class DateField extends JTextField {
        @Override
        public void setText(String s) {
            super.setText(s);
            setCaretPosition(0);
        }
    }
}
