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

import com.sri.ltc.filter.Filtering;
import com.sri.ltc.server.LTCserverInterface;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author linda
 */
@SuppressWarnings("serial")
public final class BoolPrefCheckBox extends JCheckBox {

    public BoolPrefCheckBox(String text,
                            final LTCserverInterface.BoolPrefs boolPref,
                            final JButton updateButton) {
        super(text, Filtering.getInstance().getStatus(boolPref));
        addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                Filtering.getInstance().setStatus(
                        boolPref,
                        e.getStateChange() == ItemEvent.SELECTED);
                updateButton.doClick();
            }
        });
    }
}
