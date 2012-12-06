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
