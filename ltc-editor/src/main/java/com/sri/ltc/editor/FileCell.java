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

import java.awt.*;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * @author linda
 */
public final class FileCell {

    private final Preferences preferences; // = Preferences.userRoot().node(this.getClass().getCanonicalName().replaceAll("\\.", "/"));
    private final File file;
    private Color color = Color.blue;

    public FileCell(Preferences preferences, File file) {
        if (preferences == null)
            this.preferences = Preferences.userNodeForPackage(this.getClass());
        else
            this.preferences = preferences;
        if (file == null)
            throw new IllegalArgumentException("Cannot create file cell with NULL file");
        this.file = file;
        String colorPref = this.preferences.get(file.getAbsolutePath(), null);
        if (colorPref == null)
            this.color = null;
        else
            this.color = Color.decode(colorPref);
    }

    public Color getColor() {
        return color;
    }

    public boolean setColor(Color color) {
        if (color == null) {
            preferences.put(file.getAbsolutePath(), null);
            return false;
        }
        boolean changed = !color.equals(this.color);
        this.color = color;
        preferences.put(file.getAbsolutePath(), Integer.toString(color.getRGB()));
        return changed;
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileCell fileCell = (FileCell) o;

        if (color != null ? !color.equals(fileCell.color) : fileCell.color != null) return false;
        if (!file.equals(fileCell.file)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = file.hashCode();
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }
}
