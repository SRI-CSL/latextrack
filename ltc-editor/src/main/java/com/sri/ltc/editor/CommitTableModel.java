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

import com.sri.ltc.server.LTCserverInterface;

import javax.swing.table.AbstractTableModel;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author linda
 */
public final class CommitTableModel extends AbstractTableModel {

    private final static Logger LOGGER = Logger.getLogger(CommitTableModel.class.getName());
    private final List<CommitTableRow> commits = new ArrayList<CommitTableRow>();
    private CommitTableRow firstCell = null;

    private static final long serialVersionUID = -923583506868039590L;

    @Override
    public int getColumnCount() {
        return CommitTableRow.COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int i) {
        return CommitTableRow.COLUMN_NAMES[i];
    }

    @Override
    public Class<?> getColumnClass(int i) {
        return CommitTableRow.getColumnClass(i);
    }

    @Override
    public int getRowCount() {
        synchronized (commits) {
            return commits.size()+(firstCell==null?0:1);
        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        return getRow(row).getColumn(column);
    }

    private CommitTableRow getRow(int row) {
        synchronized (commits) {
            if (firstCell == null)
                return commits.get(row);
            if (row == 0)
                return firstCell;
            return commits.get(row - 1);
        }
    }

    public void init(List<Object[]> rawCommits, boolean clearFirstCell) {
        synchronized (commits) {
            clear(clearFirstCell);

            if (rawCommits == null)
                return;

            // 1) build up list of commits from given list
            Map<String,CommitTableRow> commitMap = new HashMap<String,CommitTableRow>();
            for (Object[] c : rawCommits) {
                try {
                    CommitTableRow ctr = new CommitTableRow(c);
                    commits.add(ctr);
                    commitMap.put(c[0].toString(), ctr); // remember in map for later
                } catch (ParseException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            // 2) build up graph structure going through given list a second time
            for (Object[] c : rawCommits) {
                CommitTableRow node = commitMap.get(c[0].toString());
                String[] tokens = c[5].toString().split("\\s+");
                for (String sha1 : tokens) {
                    CommitTableRow parent = commitMap.get(sha1);
                    if (parent != null) {
                        node.parents.add(parent);
                        parent.children.add(node);
                    }
                }
            }
            // 3) update graph column locations
            commits.get(0).graph.circleColumn = 0; // start with first column
            SortedSet<Integer> currentColumns = new TreeSet<Integer>(); // keep track of passing lines
            for (CommitTableRow node : commits) {
                // set of passing lines is difference currentColumns\{circleColumn}
                node.graph.passingColumns.clear();
                node.graph.passingColumns.addAll(currentColumns);
                node.graph.passingColumns.remove(node.graph.circleColumn);
                // add own column to incoming (if not first)
                if (!node.children.isEmpty())
                    node.graph.incomingColumns.add(node.graph.circleColumn);
                // determine columns of parents:
                int currentParentColumn = node.graph.circleColumn;
                for (CommitTableRow parent : node.parents) {
                    if (parent.graph.circleColumn > currentParentColumn) {
                        if (currentColumns.remove(parent.graph.circleColumn))
                            // if actually removed then add to incoming columns:
                            parent.graph.incomingColumns.add(parent.graph.circleColumn);
                        parent.graph.circleColumn = currentParentColumn;
                        // update outgoing columns:
                        node.graph.outgoingColumns.add(currentParentColumn);
                        // maintain current columns
                        currentColumns.add(currentParentColumn);
                        currentParentColumn++;
                    }
                }
            }
            fireTableDataChanged();
        }
    }

    public void update(Set<String> sha1) {
        synchronized (commits) {
            firstCell = null;
            if (sha1.contains(LTCserverInterface.ON_DISK)) {
                initFirstCell(LTCserverInterface.ON_DISK);
            }
            if (sha1.contains(LTCserverInterface.MODIFIED)) {
                initFirstCell(LTCserverInterface.MODIFIED);
            }
            for (CommitTableRow row : commits)
                row.setActive(sha1.contains(row.sha1));
        }
        fireTableDataChanged();
    }

    public void add(Object[] last_commit) {
        if (last_commit != null)
            synchronized (commits) {
                try {
                    commits.add(0, new CommitTableRow(last_commit));
                    // TODO: update graph columns etc.
                    int insertedRow = 0 + (firstCell==null?0:1);
                    fireTableRowsInserted(insertedRow, insertedRow);
                } catch (ParseException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
    }
    
    // return true if something changed
    private void initFirstCell(String sha1) {
        boolean updated = firstCell != null && !firstCell.sha1.equals(sha1);
        if (firstCell == null || updated)
            firstCell = new CommitTableRow(sha1);
        if (updated)
            fireTableRowsUpdated(0, 0);
        else
            fireTableRowsInserted(0, 0);
    }

    public void addOnDisk() {
        initFirstCell(LTCserverInterface.ON_DISK);
    }

    public void removeOnDisk() {
        if (firstCell != null && firstCell.sha1.equals(LTCserverInterface.ON_DISK)) {
            firstCell = null;
            fireTableRowsDeleted(0, 0);
        }
    }

    public void updateFirst(String sha1) {
        initFirstCell(sha1);
    }

    public void clear(boolean clearFirstCell) {
        synchronized (commits) {
            int size = getRowCount();
            commits.clear();
            if (clearFirstCell)
                firstCell = null;
            if (size > 0)
                fireTableRowsDeleted(firstCell==null?0:1, size-1);
        }
    }

    public boolean isActive(int row) {
        if (row < 0)
            return false;
        return getRow(row).isActive();
    }
}
