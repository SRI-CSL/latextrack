/*
 * #%L
 * LaTeX Track Changes (LTC) allows collaborators on a version-controlled LaTeX writing project to view and query changes in the .tex documents.
 * %%
 * Copyright (C) 2009 - 2013 SRI International
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
package com.sri.ltc.editor;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * A re-usable panel to be displayed when user wants to create a bug report.
 * Three data points are recorded: the directory where to store the report,
 * an optional comment string from the user and whether to include the source
 * repository of the writing project.  The last directory used and the last
 * setting of whether to include the repository are also stored in preferences.
 *
 * @author linda
 */
final public class BugReportPanel extends JPanel {

    public final class Data {
        public final String directory;
        public final String comment;
        public final Boolean repository;

        public Data(String directory, String comment, Boolean repository) {
            this.directory = directory;
            this.comment = comment;
            this.repository = repository;
        }

        @Override
        public String toString() {
            return "[" + directory + ", " + comment + ", " + (repository?"X":"O") + "]";
        }
    }

    // persistent preferences:
    private final Preferences preferences = Preferences.userRoot().node(this.getClass().getCanonicalName().replaceAll("\\.","/"));
    private final static String KEY_LAST_PARENT_DIR = "parent of last directory of bug report";
    private final static String KEY_LAST_DIR = "last directory of bug report";
    private final static String KEY_LAST_REPO = "last setting whether to include source repository";

    private final JFileChooser fileChooser = new JFileChooser();
    private final JTextField directoryField = new JTextField(30);
    private final JTextArea commentArea = new JTextArea(5, 20);
    private final JCheckBox includeRepo = new JCheckBox("include repository");

    public BugReportPanel() {
        super(new BorderLayout());

        // ~~~ components

        fileChooser.setCurrentDirectory(new File(
                preferences.get(KEY_LAST_PARENT_DIR, System.getProperty("user.dir"))));
        fileChooser.setDialogTitle("Choose Directory To Store Report");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        directoryField.setText(preferences.get(KEY_LAST_DIR, ""));

        includeRepo.setSelected(preferences.getBoolean(KEY_LAST_REPO, false));
        includeRepo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                preferences.putBoolean(KEY_LAST_REPO, includeRepo.isSelected());
            }
        });

        // ~~~ layout

        // file panel:
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        filePanel.add(new JLabel("Save in:"));
        filePanel.add(directoryField);
        filePanel.add(new JButton(new AbstractAction("Choose...") {
            private static final long serialVersionUID = 138311848972917973L;
            public void actionPerformed(ActionEvent e) {
                if (fileChooser.showDialog(BugReportPanel.this, "Choose") == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    directoryField.setText(file.getAbsolutePath());
                }
            }
        }));
        add(filePanel, BorderLayout.PAGE_START);
        // comment panel:
        JPanel commentPanel = new JPanel(new BorderLayout(0, 2));
        commentPanel.add(new JLabel("Comments (optional):"), BorderLayout.PAGE_START);
        commentPanel.add(new JScrollPane(commentArea), BorderLayout.CENTER);
        add(commentPanel, BorderLayout.CENTER);
        // repo panel:
        add(includeRepo, BorderLayout.PAGE_END);

        // ~~~ listeners
        directoryField.getDocument().addDocumentListener(new DocumentListener() {
            private synchronized void newDirectory() {
                String directory = directoryField.getText();
                // save value to preferences
                preferences.put(KEY_LAST_DIR, directory);
                // determine parent dir (if exists) and save to preferences
                File newParentDirectory = new File(directory).getParentFile();
                if (newParentDirectory != null && newParentDirectory.isDirectory())
                    preferences.put(KEY_LAST_PARENT_DIR, newParentDirectory.getAbsolutePath());
                else
                    preferences.put(KEY_LAST_PARENT_DIR, System.getProperty("user.dir"));
            }
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                newDirectory();
            }
            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                newDirectory();
            }
            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                newDirectory();
            }
        });
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent ancestorEvent) {
                // dialog is realized and made visible
                // decide focus on state of directory field:
                if ("".equals(directoryField.getText()))
                    directoryField.requestFocusInWindow();
                else
                    commentArea.requestFocusInWindow();
            }
            @Override
            public void ancestorRemoved(AncestorEvent ancestorEvent) { }
            @Override
            public void ancestorMoved(AncestorEvent ancestorEvent) { }
        });
    }

    public Data getData() {
        return new Data(directoryField.getText(),
                commentArea.getText(),
                includeRepo.isSelected());
    }
}
