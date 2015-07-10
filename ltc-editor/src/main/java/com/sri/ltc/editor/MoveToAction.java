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

import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author linda
 */
public final class MoveToAction extends AbstractAction {

    private static final long serialVersionUID = -4665355462576879541L;
    private final LatexPane latexPane;
    private final boolean isForward;
    private Point clickLocation;

    public MoveToAction(@Nonnull LatexPane latexPane, boolean forward, Point clickLocation) {
        super("Move to "+(forward?"next":"previous")+" change");
        if (latexPane == null)
            throw new IllegalArgumentException("Cannot instantiate move to action with NULL argument.");
        this.latexPane = latexPane;
        isForward = forward;
        this.clickLocation = clickLocation;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int index = latexPane.viewToModel(clickLocation);
        if (!isForward)
            index--; // start with previous character if going backward
        // test border
        if (testBorder(index))
            return;
        // go through current change with the same style and revision
        int start = index;
        do {
            if (isForward) index++;
            else           index--;
        } while (latexPane.areEqualIndices(start, index,
                Sets.immutableEnumSet(LatexPane.TextAttribute.Style, LatexPane.TextAttribute.Revision)));
        // test border
        if (testBorder(index))
            return;
        // go through any non-change: compare style at current index to end of document (always a non-change style)
        while (latexPane.areEqualIndices(latexPane.getDocument().getLength(), index,
                Sets.immutableEnumSet(LatexPane.TextAttribute.Style))) {
            if (isForward) index++;
            else           index--;
        }
        // jump to new position (if any)
        if (testBorder(index))
            return;
        if (!isForward)
            index++; // increment position if looking for end of previous change
        latexPane.setCaretPosition(index);
    }

    private boolean testBorder(int index) {
        boolean result;
        if (isForward)
            result = (index > latexPane.getDocument().getLength());
        else
            result = (index < 0);
        if (result)
            JOptionPane.showMessageDialog(latexPane,
                    "No change until "+(isForward?"end":"beginning")+" of document found.",
                    "No change found",
                    JOptionPane.INFORMATION_MESSAGE);
        return result;
    }
}
