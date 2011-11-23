/**
 ************************ 80 columns *******************************************
 * ShowingCheckBox
 *
 * Created on Aug 6, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.viewer;

import com.sri.ltc.filter.Filtering;
import com.sri.ltc.server.LTCserverInterface;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author linda
 */
@SuppressWarnings("serial")
public final class ShowingCheckBox extends JCheckBox {

    public ShowingCheckBox(String text,
                           final LTCserverInterface.Show show,
                           final JButton updateButton) {
        super(text, Filtering.getInstance().getShowingStatus(show));
        addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                Filtering.getInstance().setShowingStatus(
                        show,
                        e.getStateChange() == ItemEvent.SELECTED);
                updateButton.doClick();
            }
        });
    }
}
