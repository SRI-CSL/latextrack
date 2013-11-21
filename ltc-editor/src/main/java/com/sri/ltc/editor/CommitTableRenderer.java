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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * @author linda
 */
public class CommitTableRenderer extends JLabel implements TableCellRenderer {

    public final static Color INACTIVE_COLOR = Color.gray;

    public CommitTableRenderer() {
        setOpaque(true);
    }

    // default implementations (to be overridden by more specific renderer):

    String renderText(Object object) {
        return object.toString();
    }

    Icon renderIcon(Object object, int height, Color foreground) {
        return null;
    }

    Color renderColor(Object object) {
        return null;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object object,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        // foreground determined by active status and rendering
        TableModel model = table.getModel();
        if (model instanceof CommitTableModel) {
            Color fg = object==null?null:renderColor(object);
            setForeground(
                    ((CommitTableModel) model).isActive(row)?
                            (fg==null?table.getForeground():fg) :
                            INACTIVE_COLOR
            );
        } else
            setForeground(table.getForeground());

        // text and icon
        setText(object==null?"":renderText(object));
        setIcon(object==null?null:renderIcon(object, table.getRowHeight(), getForeground()));

        // keep background
        setBackground(table.getBackground());

        // handle selection: draw only a thin border
        if (isSelected)
            setBorder(BorderFactory.createLineBorder(table.getSelectionBackground()));
        else
            setBorder(BorderFactory.createEmptyBorder());

        return this;
    }
}
