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

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author linda
 */
public final class UndoChangesAction extends AbstractAction {

    private static final long serialVersionUID = 8110470392248172958L;

    enum UndoType {
        ByRev("Undo this change", LatexPane.TextAttribute.Revision),
        ByAuthor("Undo change in same color", LatexPane.TextAttribute.Author),
        InRegion("Undo all changes in region", null);
        final String name;
        final LatexPane.TextAttribute mode;
        UndoType(String name, LatexPane.TextAttribute mode) {
            this.name = name;
            this.mode = mode;
        }
    }

    private final LatexPane latexPane;
    private final UndoType type;
    private Point clickLocation;
    private DotMark selectionLocation;

    public UndoChangesAction(@Nonnull LatexPane latexPane, @Nonnull UndoType type, Point clickLocation, DotMark selectionLocation) {
        super(type.name);
        if (latexPane == null || type == null)
            throw new IllegalArgumentException("Cannot instantiate undo changes action with NULL arguments.");
        this.latexPane = latexPane;
        this.type = type;
        this.clickLocation = clickLocation;
        this.selectionLocation = selectionLocation;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int index = latexPane.viewToModel(clickLocation);
        switch (type) {
            case ByRev:
            case ByAuthor:
                latexPane.undoChange(index, latexPane.getDocument().getLength(), type.mode);
                break;
            case InRegion:
                int dot = selectionLocation.getDot();
                int mark = selectionLocation.getMark();
                if (dot == mark) {
                    JOptionPane.showMessageDialog(latexPane,
                            "Cannot undo changes in region if no region is selected.",
                            "No region selected",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                // TODO: decide to bail out if click was outside of region,
                //       i.e., index < Math.min(dot, mark) && index > Math.max(dot, mark)
                // determine start and end of region to perform undo:
                index = Math.min(dot, mark); // use beginning of region
                while (index >= 0 && index < Math.max(dot, mark))  // fail silently if index = -1 returned
                    index = latexPane.undoChange(index, Math.max(dot, mark)-1, type.mode);
                break;
        }
    }
}
