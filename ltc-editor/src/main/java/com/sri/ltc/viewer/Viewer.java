/**
 ************************ 80 columns *******************************************
 * Viewer
 *
 * Created on Jan 8, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.viewer;

import com.sri.ltc.server.LTCserverInterface;
import com.wordpress.tips4java.TextLineNumber;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;

/**
 * @author linda
 */
@SuppressWarnings("serial")
public class Viewer extends JFrame {

    final LatexPane textPane = new LatexPane();
    final JFileChooser fileChooser = new JFileChooser();
    {
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
    }

    public Viewer(String[] paths) throws HeadlessException {
        super("LatexDiff Viewer");

        // initialize buttons and check boxes
        final JButton addButton = new JButton("Add...");
        final JButton removeButton = new JButton("Remove");
        removeButton.setEnabled(false);
        final JButton upButton = new JButton("Up");
        upButton.setEnabled(false);
        final JButton downButton = new JButton("Down");
        downButton.setEnabled(false);
        final JButton updateButton = new JButton("Update");
        updateButton.setEnabled(false);
        final JCheckBox paraCheckBox = new JCheckBox("display white space characters", textPane.getShowParagraphs());

        // pane with current file selections
        JPanel filePane = new JPanel();
        filePane.setLayout(new BoxLayout(filePane, BoxLayout.LINE_AXIS));
        filePane.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        filePane.add(new JLabel("Files:"));
        filePane.add(Box.createRigidArea(new Dimension(5, 0)));
        // list
        final DefaultListModel model = new DefaultListModel();
        final JList list = new JList(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        list.setCellRenderer(new FileRenderer());
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    upButton.setEnabled(false);
                    downButton.setEnabled(false);
                    if (list.getSelectedIndex() == -1) {
                        //No selection, disable remove, up, and down button.
                        removeButton.setEnabled(false);
                    } else {
                        //Selection, enable the remove button.
                        removeButton.setEnabled(true);
                        // enable up and down only if not the first/last one
                        if (list.getSelectedIndex() > 0)
                            upButton.setEnabled(true);
                        if (list.getSelectedIndex() < model.getSize()-1)
                            downButton.setEnabled(true);
                    }
                }
            }
        });
        model.addListDataListener(new ListDataListener() {
            private void updateList() {
                // enable Update button, if at least 2 entries
                updateButton.setEnabled(model.getSize() >= 2);
                // TODO: trigger evaluation of up, down and remove buttons (as in selection listener)
            }
            public void intervalAdded(ListDataEvent listDataEvent) {
                updateList();
            }
            public void intervalRemoved(ListDataEvent listDataEvent) {
                updateList();
            }
            public void contentsChanged(ListDataEvent listDataEvent) {
                updateList(); // shouldn't occur
            }
        });
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 80));
        filePane.add(listScroller);
        filePane.add(Box.createRigidArea(new Dimension(5, 0)));
        // buttons
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.PAGE_AXIS));
        buttonPane.add(Box.createVerticalGlue());
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (fileChooser.showOpenDialog(Viewer.this) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    model.addElement(file.getAbsolutePath().replaceFirst(
                            System.getProperty("user.dir")+File.separator,""));
                    list.setSelectedIndex(model.getSize()-1);
                    list.putClientProperty(LatexPane.KEY_COLORS, null); // remove color scheme (if any)
                }
            }
        });
        buttonPane.add(addButton);
        buttonPane.add(Box.createRigidArea(new Dimension(2, 0)));
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                int index;
                if ((index = list.getSelectedIndex()) != -1) {
                    model.removeElementAt(index);
                    if (!model.isEmpty())
                        list.setSelectedIndex(index==0?0:index-1);
                    list.putClientProperty(LatexPane.KEY_COLORS, null); // remove color scheme (if any)
                }

            }
        });
        buttonPane.add(removeButton);
        buttonPane.add(Box.createRigidArea(new Dimension(2, 0)));
        upButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                int index;
                if ((index = list.getSelectedIndex()) != -1 &&
                        index > 0) {
                    Object element = model.getElementAt(index);
                    model.removeElementAt(index);
                    model.add(index-1,element);
                    list.setSelectedIndex(index-1);
                    list.putClientProperty(LatexPane.KEY_COLORS, null); // remove color scheme (if any)
                }
            }
        });
        buttonPane.add(upButton);
        buttonPane.add(Box.createRigidArea(new Dimension(2, 0)));
        downButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                int index;
                if ((index = list.getSelectedIndex()) != -1 &&
                        index < model.getSize()-1) {
                    Object element = model.getElementAt(index);
                    model.removeElementAt(index);
                    model.add(index+1,element);
                    list.setSelectedIndex(index+1);
                    list.putClientProperty(LatexPane.KEY_COLORS, null); // remove color scheme (if any)
                }
            }
        });
        buttonPane.add(downButton);
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                textPane.updateFromPaths(model.toArray());
                // obtain colors from text pane
                list.putClientProperty(LatexPane.KEY_COLORS,
                        textPane.getChangeColors());
                list.clearSelection();
                list.repaint(); // render list elements again to use color scheme
            }
        });
        buttonPane.add(Box.createRigidArea(new Dimension(2, 0)));
        buttonPane.add(updateButton);
        filePane.add(buttonPane);

        // scroll pane with text pane:
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        TextLineNumber tln = new TextLineNumber(textPane);
        tln.setCurrentLineForeground(Color.black); // no highlighting
        scrollPane.setRowHeaderView(tln);

        // pane with options
        JPanel optionPane = new JPanel();
        optionPane.setLayout(new BoxLayout(optionPane, BoxLayout.PAGE_AXIS));
        optionPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        optionPane.add(new JLabel("Options:"));
        optionPane.add(new ShowingCheckBox("show deletions", LTCserverInterface.Show.DELETIONS, updateButton));
        optionPane.add(new ShowingCheckBox("show \"small\" changes", LTCserverInterface.Show.SMALL, updateButton));
        optionPane.add(new ShowingCheckBox("show changes in preamble", LTCserverInterface.Show.PREAMBLE, updateButton));
        optionPane.add(new ShowingCheckBox("changes in comments", LTCserverInterface.Show.COMMENTS, updateButton));
        optionPane.add(new ShowingCheckBox("changes in commands", LTCserverInterface.Show.COMMANDS, updateButton));
        paraCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (paraCheckBox.isSelected()) {
                    textPane.getDocument().putProperty("show paragraphs","");
                } else {
                    textPane.getDocument().putProperty("show paragraphs", null);
                }
                textPane.repaint();
            }
        });
        optionPane.add(paraCheckBox);

        // Create a panel and add components to it.
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPane.add(filePane, BorderLayout.PAGE_START);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(optionPane, BorderLayout.PAGE_END);
        contentPane.setOpaque(true); //content panes must be opaque        
        setContentPane(contentPane);

        // initialize list of paths
        if (paths != null) {
            for (int i = 0; i < paths.length; i++)
                if (paths[i] != null) model.addElement(paths[i]);
            if (model.getSize() > 1)
                updateButton.doClick();
        }
    }

    @SuppressWarnings("serial")
    class FileRenderer extends DefaultListCellRenderer {
        @Override
        @SuppressWarnings("unchecked")
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean hasFocus) {
            JLabel label = (JLabel)super.getListCellRendererComponent(list,
                    value,
                    index,
                    isSelected,
                    hasFocus);
            Map<Integer,Color> colors = (Map<Integer, Color>) list.getClientProperty(LatexPane.KEY_COLORS);
            if (colors != null) {
                Color c = colors.get(index);
                if (c == null)
                    label.setForeground(Color.black);
                else
                    label.setForeground(c);
            } else
                label.setForeground(Color.black);
            return label;
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     *
     * @param frame Viewer instance to be displayed
     */
    private static void createAndShowGUI(Viewer frame) {
        //Create and set up the window.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        final Viewer frame = new Viewer(args);

        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(frame);
            }
        });
    }
}
