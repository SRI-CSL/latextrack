/**
 ************************ 80 columns *******************************************
 * ProgressDialog
 *
 * Created on Oct 11, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Modal dialog while updating track changes.
 * Starts with indeterminate mode first and switches to real progress once
 * numbers become available.  When user hits "Cancel" button,
 *
 * @author linda
 */
public class ProgressDialog extends JDialog implements ActionListener {

    private static ProgressDialog dialog;
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final SwingWorker worker;

    /**
     * Set up and show the dialog.  The first Component argument
     * determines which frame the dialog depends on; it should be
     * a component in the dialog's controlling frame.
     *
     * @param frame Frame used as ancestor and to position the dialog
     * @param title Title of dialog window
     * @param labelText Text of label above progress bar
     * @param worker SwingWorker used for cancelling (if NULL, no cancel button is displayed)
     */
    public static void showDialog(Frame frame,
                                  String title,
                                  String labelText,
                                  SwingWorker worker) {
        done(); // dispose any ongoing dialog
        dialog = new ProgressDialog(frame, title, labelText, worker);
        // have to invoke this in Runnable as JDialog.setVisible(true) blocks for modal dialogs:
        // even if this is already the event dispatching thread, invokeLater will flush all other pending events first
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // don't show dialog if the underlying process has already finished and disposed of dialog
                if (dialog.isDisplayable())
                    dialog.setVisible(true);
            }
        });
    }

    private ProgressDialog(Frame frame,
                           String title,
                           String labelText,
                           SwingWorker worker) {
        super(frame, title, true);
        this.worker = worker;

        // Initialize the progress bar
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(true);

        //Create and initialize the button.
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        getRootPane().setDefaultButton(cancelButton);
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelButton);

        //Put everything together, using the content pane's BorderLayout.
        // Create another pane to be able to set border etc.
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel(labelText), BorderLayout.PAGE_START);
        panel.add(progressBar, BorderLayout.CENTER);
        if (worker != null)
            panel.add(buttonPane, BorderLayout.PAGE_END);

        getContentPane().add(panel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(frame);
    }

    public static void setProgress(int progress) {
        if (progress > 0 && dialog != null) {
            dialog.progressBar.setIndeterminate(false);
            dialog.progressBar.setValue(progress);
        }
    }

    public static void done() {
        if (dialog != null && dialog.isVisible()) {
            dialog.setVisible(false);
            dialog.dispose();
        }
    }

    //Handle clicks on the Cancel button.
    @Override
    public void actionPerformed(ActionEvent e) {
        if (worker != null)
            worker.cancel(false); // don't send an interrupt
        done();
    }
}
