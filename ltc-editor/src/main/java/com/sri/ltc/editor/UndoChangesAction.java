package com.sri.ltc.editor;

import com.sun.istack.internal.NotNull;

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
        ByAuthor("Undo changes in same color", LatexPane.TextAttribute.Author),
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

    public UndoChangesAction(@NotNull LatexPane latexPane, @NotNull UndoType type, Point clickLocation, DotMark selectionLocation) {
        super(type.name);
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
                System.out.println(" --- Undo!");
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
                System.out.println(" --- Undo in ["+index+", "+Math.max(dot, mark)+"[!");
                while (index >= 0 && index < Math.max(dot, mark))  // fail silently if index = -1 returned
                    index = latexPane.undoChange(index, Math.max(dot, mark)-1, type.mode);
                break;
        }
    }
}
