/**
 ************************ 80 columns *******************************************
 * AuthorCellRenderer
 *
 * Created on Aug 6, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.editor;

import javax.swing.*;
import java.awt.*;

/**
 * @author linda
 */
@SuppressWarnings("serial")
public final class FileCellRenderer extends JLabel implements ListCellRenderer{

    public FileCellRenderer() {
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        setText(value.toString());
        if (value instanceof FileCell) {
            Color color = ((FileCell) value).getColor();
            if (color == null)
                color = Color.WHITE;
            setIcon(new ColorIcon(color));
        } else {
            setIcon(null);
        }

        // deal with default colors
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }
}