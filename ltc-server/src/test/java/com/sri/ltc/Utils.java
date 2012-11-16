/**
 ************************ 80 columns *******************************************
 * Utils
 *
 * Created on 11/16/12.
 *
 * Copyright 2009-2010, SRI International.
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
