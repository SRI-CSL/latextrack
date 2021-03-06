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

import com.sri.ltc.CommonUtils;
import com.sri.ltc.filter.Author;

import javax.activation.DataHandler;
import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * @author linda
 */
public final class CommitTable extends JTable {

    private final Preferences preferences = Preferences.userNodeForPackage(this.getClass());
    private final static String KEY_PREF_WIDTH = "preferred width of column ";

    private final static Logger LOGGER = Logger.getLogger(CommitTable.class.getName());

    public CommitTable(CommitTableModel model, final AuthorListModel authorModel) {
        super(model);
        if (authorModel == null)
            throw new IllegalArgumentException("Cannot create commit table with NULL as author model");

        setFillsViewportHeight(true);
        getTableHeader().setReorderingAllowed(false);
        setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        setRowHeight(20); // taller rows
        setRowMargin(0); // but no space between

        // enabling cell selection for dragging:
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setColumnSelectionAllowed(true);
        setRowSelectionAllowed(true);
        setDragEnabled(true);
        setTransferHandler(new CommitTransferHandler()); // customized drag 'n drop

        // renderer
        setDefaultRenderer(CommitGraphRow.class, new CommitTableRenderer() {
            @Override
            Icon renderIcon(Object object, int height, Color foreground) {
                return ((CommitGraphRow) object).toIcon(height, foreground);
            }
            @Override
            String renderText(Object object) {
                return null;
            }
        });
        setDefaultRenderer(String.class, new CommitTableRenderer());
        setDefaultRenderer(Date.class, new CommitTableRenderer() {
            @Override
            String renderText(Object object) {
                return CommonUtils.serializeDate((Date) object);
            }
        });
        setDefaultRenderer(Author.class, new CommitTableRenderer() {
            @Override
            String renderText(Object object) {
                return object.toString(); // name and email
            }
            @Override
            Color renderColor(Object object) {
                return authorModel.getColorForAuthor((Author) object);
            }
        });

        // column widths (persistent)
        Component component;
        for (int i = 0; i < CommitTableRow.REF_VALUES.length; i++) {
            component = getDefaultRenderer(model.getColumnClass(i)).getTableCellRendererComponent(this,
                    CommitTableRow.REF_VALUES[i], // reference value
                    false, false, -1, i);
            getColumnModel().getColumn(i).setPreferredWidth(preferences.getInt(
                    KEY_PREF_WIDTH+i,
                    component.getPreferredSize().width));
        }

        // listen to changes of column widths:
        getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent event) {
                StringBuilder builder = new StringBuilder("Resized column widths = [");
                TableColumnModel tcm = getColumnModel();
                for (int i = 0; i < tcm.getColumnCount(); i++) {
                    int width = tcm.getColumn(i).getPreferredWidth();
                    builder.append(width);
                    if (i < tcm.getColumnCount() - 1)
                        builder.append(", ");
                    preferences.putInt(KEY_PREF_WIDTH + i, width);
                }
                builder.append("]");
                LOGGER.config(builder.toString());
            }
        });
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        // only the first row with authors is editable!
        if (row > 0)
            return false;
        Object o = getValueAt(row, col);
        if (o instanceof Author)
            return true;
        return false;
    }

    private class CommitTransferHandler extends TransferHandler {

        private static final long serialVersionUID = 7748279046129319272L;

        @Override
        public int getSourceActions(JComponent jComponent) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean canImport(TransferSupport transferSupport) {
            return false; // cannot import data
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            CommitTable table = (CommitTable) c;

            int column = table.getSelectedColumn();
            if (column == table.getColumnCount()-1) // ignore last column
                return null;

            Object selectedObject = table.getModel().getValueAt(table.getSelectedRow(), column);
            if (selectedObject instanceof String)
                return new StringSelection((String) selectedObject);
            try {
                String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=" + selectedObject.getClass().getName();
                if (selectedObject instanceof Date || selectedObject instanceof Author)
                    return new DataHandler(selectedObject, new DataFlavor(mimeType).getMimeType());
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }

            return null; // class not supported
        }
    }
}
