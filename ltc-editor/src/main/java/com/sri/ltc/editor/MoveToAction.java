package com.sri.ltc.editor;

import com.google.common.collect.Sets;

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

    public MoveToAction(String name, LatexPane latexPane, boolean forward, Point clickLocation) {
        super(name);
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
