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
import com.sri.ltc.logging.LogConfiguration;
import com.sri.ltc.server.LTCserverInterface;
import com.wordpress.tips4java.TextLineNumber;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Basic class for LTC editor and viewer applications.  Creates a frame, content pane with customizable
 * lower-right panel in the CENTER (open positions at the other locations), and the update button.
 * <p>
 * Sub-classes should define and set the update button action.
 *
 * @author linda
 */
@SuppressWarnings("serial")
public abstract class LTCGui {

    // static initializations
    static {
        // first thing is to configure Mac OS X before AWT gets loaded:
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "LTC GUI");
        System.setProperty("apple.awt.showGrowBox", "true");
        // print NOTICE on command line
        System.out.println(CommonUtils.getNotice()); // output notice
        // default configuration for logging
        try {
            LogManager.getLogManager().readConfiguration(new LogConfiguration().asInputStream());
            Logger.getLogger(LTCGui.class.getName()).fine("Default logging configuration complete");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private final static int DEFAULT_HEIGHT = 650;
    private final Preferences preferences = Preferences.userRoot().node(this.getClass().getCanonicalName().replaceAll("\\.","/"));
    private final static String KEY_LAST_DIVIDER_V = "last V divider location";
    private final static String KEY_LAST_WIDTH = "last width of window";
    private final static String KEY_LAST_HEIGHT = "last height of window";
    private final static String KEY_LAST_X = "last X of window";
    private final static String KEY_LAST_Y = "last Y of window";
    static final Logger LOGGER = Logger.getLogger(LTCGui.class.getName());

    final static String UPDATE_ACTION = "updateAction"; // action command to be used when binding keys in sub-classes
    private final JFrame frame;
    private final JPanel contentPane;
    private final JPanel lowerRightPane = new JPanel(new BorderLayout());
    private final JButton updateButton = new JButton();
    {
        // preparing update button for CTRL-U/CMD-U key stroke accelerator:
        updateButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                UPDATE_ACTION);
    }

    final LatexPane textPane;

    public JFrame getFrame() {
        return frame;
    }

    public JPanel getContentPane() {
        return contentPane;
    }

    public JPanel getLowerRightPane() {
        return lowerRightPane;
    }

    public JButton getUpdateButton() {
        return updateButton;
    }

    private final JPanel createContentPane() {
        // 1) latex panel
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(800, 300));
        TextLineNumber tln = new TextLineNumber(textPane);
        tln.setCurrentLineForeground(Color.black); // no highlighting
        scrollPane.setRowHeaderView(tln);

        // 2) lower panes
        JPanel lowerPanes = new JPanel(new BorderLayout());
        lowerPanes.add(createShowingPane(), BorderLayout.LINE_START);
        lowerPanes.add(lowerRightPane, BorderLayout.CENTER);

        // 3) split pane for latex panel and lower panes
        final JSplitPane splitPaneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                scrollPane, lowerPanes);
        splitPaneV.setDividerLocation(preferences.getInt(KEY_LAST_DIVIDER_V, DEFAULT_HEIGHT));
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
        contentPane.add(splitPaneV, BorderLayout.CENTER);
        contentPane.setOpaque(true); //content panes must be opaque
        return contentPane;
    }

    private final JPanel createShowingPane() {
        JPanel showPane = new JPanel();
        showPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(" Showing "),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)));
        showPane.setLayout(new BoxLayout(showPane, BoxLayout.PAGE_AXIS));
        showPane.add(new BoolPrefCheckBox("deletions", LTCserverInterface.BoolPrefs.DELETIONS, updateButton));
        showPane.add(new BoolPrefCheckBox("\"small\" changes", LTCserverInterface.BoolPrefs.SMALL, updateButton));
        showPane.add(new BoolPrefCheckBox("changes in preamble", LTCserverInterface.BoolPrefs.PREAMBLE, updateButton));
        showPane.add(new BoolPrefCheckBox("changes in comments", LTCserverInterface.BoolPrefs.COMMENTS, updateButton));
        showPane.add(new BoolPrefCheckBox("changes in commands", LTCserverInterface.BoolPrefs.COMMANDS, updateButton));
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
        return showPane;
    }

    public LTCGui(boolean editable, String title) {
        LOGGER.info("Using LTC version: "+CommonUtils.getVersion());

        textPane = new LatexPane(editable);
        contentPane = createContentPane();
        frame = new JFrame(title);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent windowEvent) {
                frame.setBounds(
                        preferences.getInt(KEY_LAST_X, 0),
                        preferences.getInt(KEY_LAST_Y, 0),
                        preferences.getInt(KEY_LAST_WIDTH, 1000),
                        preferences.getInt(KEY_LAST_HEIGHT, DEFAULT_HEIGHT));
                LOGGER.fine("Get window opened: " + frame.getSize() + " at " + frame.getLocation());
                // after this resizing and moving events can now be recorded:
                frame.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentMoved(ComponentEvent componentEvent) {
                        preferences.putInt(KEY_LAST_X, frame.getX());
                        preferences.putInt(KEY_LAST_Y, frame.getY());
                        LOGGER.fine("Put window position: " + frame.getLocation());
                    }

                    @Override
                    public void componentResized(ComponentEvent componentEvent) {
                        preferences.putInt(KEY_LAST_WIDTH, frame.getWidth());
                        preferences.putInt(KEY_LAST_HEIGHT, frame.getHeight());
                        LOGGER.fine("Put window size: " + frame.getSize());
                    }
                });
            }

            @Override
            public void windowClosing(WindowEvent windowEvent) {
                // save size and location
                preferences.putInt(KEY_LAST_WIDTH, frame.getWidth());
                preferences.putInt(KEY_LAST_HEIGHT, frame.getHeight());
                preferences.putInt(KEY_LAST_X, frame.getX());
                preferences.putInt(KEY_LAST_Y, frame.getY());
                LOGGER.fine("Put window closing: " + frame.getSize() + " at " + frame.getLocation());
            }
        });
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     *
     * @param frame Editor instance to be displayed
     */
    final static void createAndShowGUI(LTCGui gui) {
        //Create and set up the window.
        gui.getFrame().setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        gui.getFrame().setContentPane(gui.getContentPane());

        //Display the window.
        gui.getFrame().pack();
        gui.getFrame().setVisible(true);
    }
}
