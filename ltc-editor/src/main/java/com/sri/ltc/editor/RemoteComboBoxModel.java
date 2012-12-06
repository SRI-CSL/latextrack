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

import com.sri.ltc.versioncontrol.Remote;

import javax.swing.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * @author linda
 */
public final class RemoteComboBoxModel extends AbstractListModel implements ComboBoxModel {

    private static final long serialVersionUID = -2109423219501987243L;
    private final static String KEY_CURRENT_ITEM = "last known current item";

    private final Preferences preferences = Preferences.userNodeForPackage(this.getClass());
    private final SortedMap<String,Remote> remotes = new TreeMap<String,Remote>();
    private Remote currentItem = null;
    private final JButton push, pull;
    private final LTCSession session;

    public RemoteComboBoxModel(LTCSession session, JButton push, JButton pull) {
        this.session = session;
        this.push = push;
        this.pull = pull;
        // instantiate current item from preferences
        String lastItem = preferences.get(KEY_CURRENT_ITEM, "");
        if (!"".equals(lastItem))
            try {
                currentItem = Remote.parse(lastItem);
            } catch (ParseException e) {
                Logger.getLogger(RemoteComboBoxModel.class.getName()).log(Level.SEVERE,
                        "ParseException while reading last remote from preferences", e);
            }
    }

    public void update(List<Object[]> remotes) {
        clear();
        if (remotes != null && !remotes.isEmpty()) {
            for (Object[] a : remotes) {
                Remote r = Remote.fromArray(a);
                this.remotes.put(r.name, r);
            }
            fireIntervalAdded(this, 0, getSize()-1);
        }
        if (currentItem != null && currentItem.isAlias() && !this.remotes.containsKey(currentItem.name)) {
            setSelectedItem(null); // prior current item not in list of aliases anymore
            fireContentsChanged(this, 0, 0); // trigger updating of text?
        }
    }

    private void clear() {
        int oldSize = getSize();
        remotes.clear();
        if (oldSize > 0)
            fireIntervalRemoved(this, 0, oldSize-1);
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if (anItem instanceof Remote) {
            currentItem = (Remote) anItem;
            if (currentItem.isAlias()) {
                Remote oldAlias = remotes.put(currentItem.name,currentItem);
                if (oldAlias != null) { // alias with this name already known
                    if (!oldAlias.equals(currentItem)) {
                        int index = new ArrayList<Remote>(remotes.values()).indexOf(currentItem);
                        fireContentsChanged(this, index, index);
                        // rm and add remote to git
                        if (session.isValid())
                            session.updateRemote(currentItem.name, currentItem.url, true);
                    }
                } else {
                    int index = new ArrayList<Remote>(remotes.values()).indexOf(currentItem);
                    fireIntervalAdded(this, index, index);
                    // add remote to git
                    if (session.isValid())
                        session.updateRemote(currentItem.name, currentItem.url, false);
                }
            }
            preferences.put(KEY_CURRENT_ITEM, currentItem.toString());
        } else {
            currentItem = null;
            preferences.put(KEY_CURRENT_ITEM, "");
        }
        // set enabled status of buttons
        pull.setEnabled(currentItem != null);
        push.setEnabled(currentItem != null &&
                (!currentItem.isAlias() || (currentItem.isAlias() && !currentItem.isReadOnly())));
    }

    @Override
    public Object getSelectedItem() {
        return currentItem;
    }

    @Override
    public int getSize() {
        return remotes.size();
    }

    @Override
    public Object getElementAt(int index) {
        return new ArrayList<Remote>(remotes.values()).get(index);
    }
}
