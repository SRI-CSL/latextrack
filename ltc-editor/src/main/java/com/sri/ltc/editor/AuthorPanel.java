package com.sri.ltc.editor;

import com.sri.ltc.filter.Author;

import javax.swing.*;
import java.awt.*;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Small panel with currently selected authors for limiting.
 *
 * The authors are displayed in alphabetical order.  They can be added, removed, or reset (remove all).
 *
 * @author linda
 */
public class AuthorPanel extends JPanel {

    private final int HEIGHT = new JLabel("T").getPreferredSize().height; // TODO; adjust when using custom labels!
    private final SortedSet<Author> model = new TreeSet<Author>();

    public AuthorPanel() {
        super(); // using FLowLayout!
        Component c = add(new JLabel("T")); // TODO: remove
//        add(new JLabel("dudi")); // TODO: remove
        setBackground(Color.yellow); // TODO: remove
//        authorPanel.setBackground(authorField.getBackground()); // TODO: uncomment
    }

    public boolean addAuthor(Author author) {
        boolean result = model.add(author);
        if (result) {
            add(new JLabel(author.toString()));
            setSize(getPreferredSize()); // copying what CompoundBorder does...
        }
        return result;
    }

//    @Override
//    public Dimension getPreferredSize() {
//        int width = 0;
//        Component[] components = getComponents();
//        for (Component c : components)
//            width += (c.getPreferredSize().width);
//        width += (components.length - 1) * ((FlowLayout) getLayout()).getHgap(); // add spaces in between
//        return new Dimension(width, HEIGHT);
//    }
}
