/**
 ************************ 80 columns *******************************************
 * CommitTable
 *
 * Created on Sep 24, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.viewer;

import com.sri.ltc.filter.Author;
import com.sri.ltc.git.Commit;

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
public class CommitTable extends JTable {

    private final Preferences preferences = Preferences.userNodeForPackage(this.getClass());
    private final static String KEY_PREF_WIDTH = "preferred width of column ";

    private final static Logger LOGGER = Logger.getLogger(CommitTable.class.getName());

    public CommitTable(CommitTableModel model) {
        super(model);

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
                return Commit.FORMATTER.format((Date) object);
            }
        });
        setDefaultRenderer(Author.class, new CommitTableRenderer() {
            @Override
            String renderText(Object object) {
                return ((Author) object).name;
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
                    preferences.putInt(KEY_PREF_WIDTH+i, width);
                }
                builder.append("]");
                LOGGER.config(builder.toString());
            }
        });
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
