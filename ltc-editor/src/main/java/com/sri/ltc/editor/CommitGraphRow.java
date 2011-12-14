/**
 ************************ 80 columns *******************************************
 * CommitGraphRow
 *
 * Created on Sep 27, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.editor;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Information of how to render the commit graph in a row.
 *
 * @author linda
 */
public final class CommitGraphRow {

    final CommitTableRow row;
    int circleColumn = Integer.MAX_VALUE;
    final SortedSet<Integer> incomingColumns = new TreeSet<Integer>();
    final SortedSet<Integer> outgoingColumns = new TreeSet<Integer>(); 
    final SortedSet<Integer> passingColumns = new TreeSet<Integer>();

    public CommitGraphRow(CommitTableRow row) {
        this.row = row;
    }

    @Override
    public String toString() {
        return row.sha1.substring(0,6)+" @ row "+circleColumn;
    }

    public Icon toIcon(int height, Color foreground) {
        return new CommitGraphIcon(height, foreground);
    }

    private class CommitGraphIcon implements Icon {

        private final static float PAD = 6f;
        private final static float MAX_DIAMETER = 8f;

        private final int width;
        private final int height;
        private final float diameter;
        private final Color foreground;

        private CommitGraphIcon(int height, Color foreground) {
            this.height = height;
            this.foreground = foreground;
            // calculate diameter
            this.diameter = Math.min(MAX_DIAMETER, ((float) height)/2f);
            // fill sets of incoming and outgoing columns
            if (row == null)
                this.width = 1;
            else {
                // calculate width as largest of own, incoming, outgoing, passing columns:
                int maxColumn = circleColumn;
                if (!incomingColumns.isEmpty())
                    maxColumn = Math.max(maxColumn, incomingColumns.last());
                if (!outgoingColumns.isEmpty())
                    maxColumn = Math.max(maxColumn, outgoingColumns.last());
                if (!passingColumns.isEmpty())
                    maxColumn = Math.max(maxColumn, passingColumns.last());
                this.width = (int) ((maxColumn+1)*columnWidth());
            }
        }

        private SortedSet<Integer> getColumnsAsSet(List<CommitTableRow> list) {
            SortedSet<Integer> set = new TreeSet<Integer>();
            for (CommitTableRow row : list)
                set.add(row.graph.circleColumn);
            return set;
        }

        private float columnWidth() {
            return 2f*PAD + diameter;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (row == null) return;

            // TODO: log character representation?

            // setup drawing
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setStroke(new BasicStroke(1.5f));

            float zeroX = circleColumn*columnWidth();
            float xCircle = zeroX + PAD;
            float circleColumnWidth = (circleColumn+1)*columnWidth();
            float yCircle = (height - diameter)/2;

            // draw lines in inactive color
            g2d.setColor(CommitTableRenderer.INACTIVE_COLOR);
            // draw incoming lines
            for (Integer otherColumn : incomingColumns) {
                if (circleColumn == otherColumn)
                    // draw straight line
                    g2d.draw(new Line2D.Float(
                            xCircle + diameter/2f, 0, // start
                            xCircle + diameter/2f, yCircle // end
                    ));
                else {
                    // draw curved lines
                    if (circleColumn > otherColumn) {
                        // draw in Q4: circleColumn segment and outside segment
                        g2d.draw(new QuadCurve2D.Float(
                                xCircle, yCircle, // start
                                xCircle, yCircle/2f, // ctrl
                                zeroX, yCircle/2f // end
                        ));
                        g2d.draw(new QuadCurve2D.Float(
                                zeroX, yCircle/2f, // start
                                otherColumn*columnWidth() + PAD + diameter/2f, yCircle/2f, // ctrl
                                otherColumn*columnWidth() + PAD + diameter/2f, 0 // end
                        ));
                    } else {
                        // draw in Q1: circleColumn segment and outside segment
                        g2d.draw(new QuadCurve2D.Float(
                                xCircle + diameter, yCircle, // start
                                xCircle + diameter, yCircle/2f, // ctrl
                                circleColumnWidth, yCircle/2f // end
                        ));
                        g2d.draw(new QuadCurve2D.Float(
                                circleColumnWidth, yCircle/2f, // start
                                otherColumn*columnWidth() + PAD + diameter/2f, yCircle/2f, // ctrl
                                otherColumn*columnWidth() + PAD + diameter/2f, 0 // end
                        ));
                    }
                }
            }
            // draw outgoing lines
            for (Integer otherColumn : outgoingColumns) {
                if (circleColumn == otherColumn)
                    // draw straight line
                    g2d.draw(new Line2D.Float(
                            xCircle + diameter/2f, yCircle + diameter, // start
                            xCircle + diameter/2f, height // end
                    ));
                else {
                    // draw curved line
                    if (circleColumn > otherColumn) {
                        // draw in Q3: circleColumn segment and outside segment
                        g2d.draw(new QuadCurve2D.Float(
                                xCircle, yCircle + diameter, // start
                                xCircle, yCircle + diameter + yCircle/2f, // ctrl
                                zeroX, yCircle + diameter + yCircle/2f // end
                        ));
                        g2d.draw(new QuadCurve2D.Float(
                                zeroX, yCircle + diameter + yCircle/2f, // start
                                otherColumn*columnWidth() + PAD + diameter/2f, yCircle + diameter + yCircle/2f, // ctrl
                                otherColumn*columnWidth() + PAD + diameter/2f, height // end
                        ));
                    } else {
                        // draw in Q2: circleColumn segment and outside segment
                        g2d.draw(new QuadCurve2D.Float(
                                xCircle + diameter, yCircle + diameter, // start
                                xCircle + diameter, yCircle + diameter + yCircle/2f, // ctrl
                                circleColumnWidth, yCircle + diameter + yCircle/2f // end
                        ));
                        g2d.draw(new QuadCurve2D.Float(
                                circleColumnWidth, yCircle + diameter + yCircle/2f, // start
                                otherColumn*columnWidth() + PAD + diameter/2f, yCircle + diameter + yCircle/2f, // ctrl
                                otherColumn*columnWidth() + PAD + diameter/2f, height // end
                        ));
                    }
                }
            }
            // draw passing lines
            for (Integer otherColumn : passingColumns) {
                // draw straight line
                g2d.draw(new Line2D.Float(
                        otherColumn*columnWidth() + PAD + diameter/2f, 0, // start
                        otherColumn*columnWidth() + PAD + diameter/2f, height // end
                ));
            }

            // draw circle in current foreground color
            g2d.setColor(foreground);
            Shape circle = new Ellipse2D.Float(
                    xCircle, yCircle,
                    diameter, diameter);
            g2d.draw(circle);
            if (row.isActive())
                g2d.fill(circle);

        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }
}
