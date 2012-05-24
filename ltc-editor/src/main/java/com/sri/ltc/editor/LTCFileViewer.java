/**
 ************************ 80 columns *******************************************
 * LTCFileViewer
 *
 * Created on 5/24/12.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.editor;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * A non-editable version of LTC that uses a list of files instead of a GIT repository.
 *
 * @author linda
 */
public final class LTCFileViewer extends LTCGui implements ListSelectionListener {

    private final Preferences preferences = Preferences.userRoot().node(this.getClass().getCanonicalName().replaceAll("\\.", "/"));
    private final static String KEY_LAST_DIR = "last directory";
    private final static String KEY_LAST_FILE = "last file";

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
                BorderFactory.createTitledBorder(" File List "),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)));

        // 1) list of files
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(this);
        list.setVisibleRowCount(5);
        // TODO: if focus lost, clear list selection?
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
                    listModel.addElement(file);
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

        // TODO: define action for update button
        getUpdateButton().setAction(new AbstractAction("U") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("update!");

                list.clearSelection();
            }
        });
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
            listModel.addElement(f);
    }

    private static void printUsage(PrintStream out, CmdLineParser parser) {
        out.println("usage: java -cp ... com.sri.ltc.editor.LTCEditor [options...] [FILE] \nwith");
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

        final LTCFileViewer viewer = new LTCFileViewer();

        if (options.resetDefaults) {
            try {
                LOGGER.config("Resetting preferences to defaults");
                viewer.preferences.clear();
            } catch (BackingStoreException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }

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
        @Option(name="-h", usage="display usage and exit")
        boolean displayHelp = false;

        @Option(name = "-r", usage = "reset to default settings")
        boolean resetDefaults = false;

        @Argument(required=false, metaVar="FILES", usage="initialize list of files")
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
