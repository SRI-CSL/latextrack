/**
 ************************ 80 columns *******************************************
 * ColorIcon
 *
 * Created on 5/24/12.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.editor;

import javax.swing.*;
import java.awt.*;

/**
 * @author linda
 */
public final class ColorIcon implements Icon {

    private final Color color;
    private final int width = 32;
    private final int height = 11;

    public ColorIcon(Color color) {
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
