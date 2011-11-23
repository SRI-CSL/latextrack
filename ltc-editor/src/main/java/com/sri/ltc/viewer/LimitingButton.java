/**
 ************************ 80 columns *******************************************
 * LimitingButton
 *
 * Created on Aug 6, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.viewer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author linda
 */
@SuppressWarnings("serial")
public final class LimitingButton extends JButton {

    public LimitingButton(String text,
                          final JList list,
                          final AuthorListModel model,
                          final boolean limited) {
        super(text);
        setToolTipText(text + " Selected");
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] indices = list.getSelectedIndices();
                if (indices.length > 0)
                    model.setLimited(indices, limited);
                list.clearSelection();
            }
        });
    }
}
