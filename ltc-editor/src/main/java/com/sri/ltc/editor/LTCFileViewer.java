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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sri.ltc.CommonUtils;
import com.sri.ltc.filter.Filtering;
import com.sri.ltc.latexdiff.Accumulate;
import com.sri.ltc.latexdiff.Change;
import com.sri.ltc.latexdiff.FileReaderWrapper;
import com.sri.ltc.latexdiff.ReaderWrapper;
import com.sri.ltc.logging.LogConfiguration;
import com.sri.ltc.server.LTCserverInterface;
import org.apache.commons.codec.binary.Base64;
import org.kohsuke.args4j.*;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * A non-editable version of LTC that uses a list of files instead of a GIT repository.
 *
 * @author linda
 */
public final class LTCFileViewer extends LTCGui implements ListSelectionListener {

    static {
        // first thing is to configure Mac OS X before AWT gets loaded:
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "LTC File Viewer");
    }
    private final Preferences preferences = Preferences.userRoot().node(this.getClass().getCanonicalName().replaceAll("\\.", "/"));
    private final static String KEY_LAST_DIR = "last directory";
    private final static String KEY_LAST_FILE = "last file";

    private final Accumulate accumulate = new Accumulate();

    // UI components
    private final DefaultListModel listModel = new DefaultListModel();
    {
        listModel.addListDataListener(new ListDataListener() {
            private void checkListLength() {
                getUpdateButton().setEnabled(listModel.getSize() >= 2);
            }
            @Override
            public void intervalAdded(ListDataEvent e) {checkListLength();}
            @Override
            public void intervalRemoved(ListDataEvent e) {checkListLength();}
            @Override
            public void contentsChanged(ListDataEvent e) {checkListLength();}
        });
    }
    private final JList list = new JList(listModel);
    private final JButton removeButton = new MyButton(new AbstractAction("-") {
        private static final long serialVersionUID = -174301387070254054L;
        @Override
        public void actionPerformed(ActionEvent e) {
            int index = list.getSelectedIndex();
            listModel.remove(index);
            // adjust list:
            int size = listModel.getSize();
            if (size == 0) { // nothing left, deselect
                list.clearSelection();
            } else { // select index below
                if (index == size) // removed item in last position
                    index--;
                list.setSelectedIndex(index);
                list.ensureIndexIsVisible(index);
            }
        }
    });
    private final JButton upButton = new MyButton(new AbstractAction("^") {
        private static final long serialVersionUID = -5355762567428511758L;
        @Override
        public void actionPerformed(ActionEvent e) {
            int index = list.getSelectedIndex();
            if (index > 0) {
                Object moved = listModel.remove(index);
                index--;
                listModel.add(index, moved);
                list.setSelectedIndex(index);
                list.ensureIndexIsVisible(index);
            }
        }
    });
    private final JButton downButton = new MyButton(new AbstractAction("v") {
        private static final long serialVersionUID = -2275191906803714895L;
        @Override
        public void actionPerformed(ActionEvent e) {
            int index = list.getSelectedIndex();
            if (index < listModel.size()-1) {
                Object moved = listModel.remove(index);
                index++;
                listModel.add(index, moved);
                list.setSelectedIndex(index);
                list.ensureIndexIsVisible(index);
            }
        }
    });
    private final JFileChooser fileChooser = new JFileChooser();
    {
        fileChooser.setCurrentDirectory(new File(preferences.get(KEY_LAST_DIR, System.getProperty("user.dir"))));
        String last_file = preferences.get(KEY_LAST_FILE, null);
        if (last_file != null)
            fileChooser.setSelectedFile(new File(last_file));
    }

    private void createLowerRightPane(JPanel panel) {
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(" File List (oldest first) "),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)));

        // 1) list of files
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(this);
        list.setVisibleRowCount(5);
        list.setCellRenderer(new FileCellRenderer());
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Object o = listModel.getElementAt(list.locationToIndex(e.getPoint()));
                    if (o instanceof FileCell) {
                        FileCell fc = (FileCell) o;
                        Color c = fc.getColor();
                        if (c == null)
                            c = Color.WHITE; // default
                        Color newColor = JColorChooser.showDialog(getFrame(),
                                "Choose File Color", c);
                        if (newColor != null) {
                            boolean changed = fc.setColor(newColor);
                            if (changed)
                                getUpdateButton().doClick();
                        }
                    }
                }
            }
        });
        JScrollPane listScrollPane = new JScrollPane(list);
        panel.add(listScrollPane, BorderLayout.CENTER);

        // 2) buttons
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.PAGE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        buttonPane.add(new JButton(new AbstractAction("+") {
            private static final long serialVersionUID = 138311848972917973L;
            public void actionPerformed(ActionEvent e) {
                if (fileChooser.showOpenDialog(getFrame()) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    listModel.addElement(new FileCell(preferences, file));
                    list.setSelectedIndex(listModel.getSize()-1); // select newly added file
                    preferences.put(KEY_LAST_DIR, file.getParent());
                    preferences.put(KEY_LAST_FILE, file.getAbsolutePath());
                }
            }
        }));
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(removeButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(upButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(downButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(getUpdateButton());
        panel.add(buttonPane, BorderLayout.LINE_END);
    }

    public LTCFileViewer() {
        super(false, "LTC File Viewer");

        // define action for update button
        getUpdateButton().setAction(new AbstractAction('\u2318'+"U") {
            private static final long serialVersionUID = 2695697870010131445L;
            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent event) {
                // build list of readers and map of colors
                java.util.List<ReaderWrapper> readers = Lists.newArrayList();
                Map<Integer,Color> colors = Maps.newHashMap();
                int i = 0;
                for (Enumeration<?> en = listModel.elements(); en.hasMoreElements(); i++) {
                    Object o = en.nextElement();
                    if (o instanceof FileCell) {
                        readers.add(new FileReaderWrapper(o.toString()));
                        Color color = ((FileCell) o).getColor();
                        if (color == null)
                            color = Color.BLACK; // default
                        colors.put(i, color);
                    }
                }

                // perform accumulation
                if (!readers.isEmpty())
                    try {
                        Filtering filter = Filtering.getInstance();
                        Map map = accumulate.perform(
                                Iterables.toArray(readers, ReaderWrapper.class),
                                null, // this will cause style indices to use order of readers
                                // this will cause revision indices to use the order of readers
                                Change.buildFlagsToHide(
                                        filter.getStatus(LTCserverInterface.BoolPrefs.DELETIONS),
                                        filter.getStatus(LTCserverInterface.BoolPrefs.SMALL),
                                        filter.getStatus(LTCserverInterface.BoolPrefs.PREAMBLE),
                                        filter.getStatus(LTCserverInterface.BoolPrefs.COMMENTS),
                                        filter.getStatus(LTCserverInterface.BoolPrefs.COMMANDS)),
                                null, textPane.getCaretPosition()
                        );
                        textPane.updateFromMaps(
                                new String(Base64.decodeBase64((byte[]) map.get(LTCserverInterface.KEY_TEXT))),
                                (java.util.List<Integer[]>) map.get(LTCserverInterface.KEY_STYLES),
                                colors,
                                (Integer) map.get(LTCserverInterface.KEY_CARET),
                                null, null);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }

                list.clearSelection();
            }
        });
        getUpdateButton().getActionMap().put(UPDATE_ACTION, getUpdateButton().getAction());
        getUpdateButton().setEnabled(false); // only enabled once more than 2 files

        createLowerRightPane(getLowerRightPane());
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            // enable or disable buttons
            removeButton.setEnabled(!list.isSelectionEmpty());
            upButton.setEnabled(!list.isSelectionEmpty() && list.getSelectedIndex() > 0);
            downButton.setEnabled(!list.isSelectionEmpty() && list.getSelectedIndex() < listModel.getSize()-1);
        }
    }

    private void setFiles(java.util.List<File> files) {
        for (File f : files)
            listModel.addElement(new FileCell(preferences, f));
        getUpdateButton().doClick();
    }

    private static void printUsage(PrintStream out, CmdLineParser parser) {
        out.println("usage: java -cp ... "+LTCFileViewer.class.getCanonicalName()+" [options...] [FILES] \nwith");
        parser.printUsage(out);
    }

    public static void main(String[] args) {
        // parse arguments
        final LTCFileViewerOptions options = new LTCFileViewerOptions();
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
            logConfig.setProperty("java.util.logging.FileHandler.pattern","%h/.LTCFileViewer.log");
            LogManager.getLogManager().readConfiguration(logConfig.asInputStream());
            LOGGER.config("Logging output written to file " + logConfig.getProperty("java.util.logging.FileHandler.pattern"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot configure logging", e);
        }

        final LTCFileViewer viewer = new LTCFileViewer();

        if (options.resetDefaults) {
            try {
                LOGGER.config("Resetting preferences to defaults");
                viewer.preferences.clear();
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

        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createAndShowGUI(viewer);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        // process optional arguments
        if (options.files != null)
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    viewer.setFiles(options.files);
                }
            });
    }

    static class LTCFileViewerOptions {
        @Option(name = "-h", usage = "display usage and exit")
        boolean displayHelp = false;

        @Option(name="-c",usage="display copyright/license information and exit")
        boolean displayLicense = false;

        @Option(name = "-r", usage = "reset to default settings")
        boolean resetDefaults = false;

        @Argument(required=false, metaVar="FILES", usage="initialize list of files (oldest first)")
        java.util.List<File> files;
    }

    // initially disabled button
    private class MyButton extends JButton {
        private MyButton(Action a) {
            super(a);
            setEnabled(false);
        }
    }
}
