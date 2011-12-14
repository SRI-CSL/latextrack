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
public final class AuthorCellRenderer extends JLabel implements ListCellRenderer{

    public AuthorCellRenderer() {
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        if (value instanceof AuthorCell) {
            AuthorCell author = (AuthorCell) value;
            setText(author.label);
            Icon icon = new ColorIcon(author.getColor());
            setIcon(icon);
            setDisabledIcon(icon);
            setEnabled(author.limited);
        } else {
            setText(value.toString());
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

    private class ColorIcon implements Icon {

        private final Color color;
        private final int width = 32;
        private final int height = 11;

        private ColorIcon(Color color) {
            this.color = color;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            // draw background
            g2d.setColor(color);
            g2d.fillRect(x + 1, y + 1, width - 2, height - 2);
            // draw border
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x + 1, y + 1, width - 2, height - 2);
        }

        public int getIconWidth() {
            return width;
        }

        public int getIconHeight() {
            return height;
        }
    }
}
