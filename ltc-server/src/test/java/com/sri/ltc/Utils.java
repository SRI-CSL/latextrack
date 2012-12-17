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
package com.sri.ltc;

import java.io.File;

/**
 * Some utilities for various tests.
 *
 * @author linda
 */
public class Utils {

    public static void deleteFolder(File folder) {
        if (!folder.isDirectory())
            return;
        File[] files = folder.listFiles();
        if (files!=null)  //some JVMs return null for empty dirs
            for (File f: files) {
                if (f.isDirectory())
                    deleteFolder(f);
                else
                    if (!f.delete())
                        throw new RuntimeException("Cannot delete file "+f.getAbsolutePath());
            }
        if (!folder.delete())
            throw new RuntimeException("Cannot delete folder "+folder.getAbsolutePath());
    }

}
