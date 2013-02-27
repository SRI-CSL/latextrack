package com.sri.ltc.editor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
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
    private final static String KEY_LAST_DIR = "last directory of bug report";
    private final static String KEY_LAST_REPO = "last setting whether to include source repository";

    private final JFileChooser fileChooser = new JFileChooser();
    private final JTextField directoryField = new JTextField(30); // TODO: if empty, disable "CREATE" button
    private final JTextArea commentArea = new JTextArea(5, 20);
    private final JCheckBox includeRepo = new JCheckBox("include repository");

    public BugReportPanel() {
        super(new BorderLayout());

        // ~~~ components

// TODO:       fileChooser.setCurrentDirectory(new File(
//                preferences.get(KEY_LAST_DIR, System.getProperty("user.dir"))));
        fileChooser.setDialogTitle("Choose Directory To Store Report");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // TODO: restore last directory from prefs and register listener to save any edits to preferences
        // directoryField.

        includeRepo.setSelected(false); // TODO: read from preferences
        includeRepo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                // TODO: save to preferences
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
                    // TODO: save parent dir to preferences
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
            private synchronized void triggerValid() {
                boolean isValid = !directoryField.getText().isEmpty(); // TODO: propagate to property of panel?
            }
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                triggerValid();
            }
            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                triggerValid();
            }
            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                triggerValid();
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent componentEvent) {
                System.out.println(" === panel shown"); // TODO: does not work!!! delete text area text and select focus
            }
        });
    }

    public Data getData() {
        return new Data(directoryField.getText(),
                commentArea.getText(),
                includeRepo.isSelected());
    }


}
