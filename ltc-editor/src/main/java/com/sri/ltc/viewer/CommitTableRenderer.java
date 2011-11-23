/**
 ************************ 80 columns *******************************************
 * CommitTableRenderer
 *
 * Created on Sep 22, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.viewer;

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

    @Override
    public Component getTableCellRendererComponent(JTable table, Object object,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        // foreground determined by active status
        TableModel model = table.getModel();
        if (model instanceof CommitTableModel) {
            setForeground(
                    ((CommitTableModel) model).isActive(row)?
                            table.getForeground():
                            INACTIVE_COLOR
            );
        } else
            setForeground(table.getForeground());

        // text and icon
        setText(object==null?"":renderText(object));
        setIcon(object==null?null:renderIcon(object, table.getRowHeight(), getForeground()));

        // keep background
        setBackground(table.getBackground());

        // handle selection
        if (isSelected)
            setBorder(BorderFactory.createLineBorder(table.getSelectionBackground()));
        else
            setBorder(BorderFactory.createEmptyBorder());

        return this;
    }
}
