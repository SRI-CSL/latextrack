/**
 ************************ 80 columns *******************************************
 * MyComboRenderer
 *
 * Created on Aug 12, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.editor;

import com.sri.ltc.filter.Author;
import com.sri.ltc.git.Remote;

import javax.swing.*;
import java.awt.*;

/**
 * @author linda
 */
@SuppressWarnings("serial")
public final class MyComboRenderer extends JLabel implements ListCellRenderer{

    public MyComboRenderer() {
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        if (value instanceof Author) {
            setText(((Author) value).gitRepresentation());
            return this;
        }

        if (value instanceof Remote) {
            setText(((Remote) value).toString());
            return this;
        }

        setText(value == null?"":value.toString());
        return this;
    }
}