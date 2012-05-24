/**
 ************************ 80 columns *******************************************
 * AuthorCell
 *
 * Created on Aug 6, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.editor;

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
