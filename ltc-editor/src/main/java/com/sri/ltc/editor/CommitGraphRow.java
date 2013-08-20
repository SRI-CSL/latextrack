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

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Information of how to render the commit graph in a row.
 * <p>
 * An inner class <code>CommitGraphIcon</code> handles the rendering according to the image below.
 * <img src="doc-files/CommitGraphIcon-1.png" alt="icon rendering details"/>
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
        return row.ID.substring(0,6)+" @ row "+(circleColumn==Integer.MAX_VALUE?"MAX":circleColumn);
    }

    public Icon toIcon(int height, Color foreground) {
        return new CommitGraphIcon(height, foreground);
    }

    private class CommitGraphIcon implements Icon {

        private final static float PAD = 6f; // padding in x-dimension
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

        private float columnWidth() {
            return 2f*PAD + diameter;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (row == null) return;

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
                    Point2D.Float startPoint = new Point2D.Float(xCircle + diameter/2f, yCircle);
                    Point2D.Float endPoint = new Point2D.Float(otherColumn*columnWidth() + PAD + diameter/2f, 0);
                    if (circleColumn > otherColumn) {
                        // draw in Q4:
                        Point2D.Float midPoint = new Point2D.Float(zeroX, yCircle/2f);
                        // circleColumn segment
                        g2d.draw(new QuadCurve2D.Float(
                                startPoint.x, startPoint.y, // start of
                                xCircle + diameter/4f, midPoint.y, // ctrl
                                midPoint.x, midPoint.y // end
                        ));
                        // outside segment
                        g2d.draw(new QuadCurve2D.Float(
                                midPoint.x, midPoint.y, // start
                                otherColumn*columnWidth() + PAD + diameter - diameter/4f, midPoint.y, // ctrl
                                endPoint.x, endPoint.y // end
                        ));
                    } else {
                        // draw in Q1:
                        Point2D.Float midPoint = new Point2D.Float(circleColumnWidth, yCircle/2f);
                        // circleColumn segment
                        g2d.draw(new QuadCurve2D.Float(
                                startPoint.x, startPoint.y, // start
                                xCircle + diameter - diameter/4f, midPoint.y, // ctrl
                                midPoint.x, midPoint.y // end
                        ));
                        // outside segment
                        g2d.draw(new QuadCurve2D.Float(
                                midPoint.x, midPoint.y, // start
                                otherColumn*columnWidth() + PAD + diameter/4f, midPoint.y, // ctrl
                                endPoint.x, endPoint.y // end
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
                    Point2D.Float startPoint = new Point2D.Float(xCircle + diameter/2f, yCircle + diameter);
                    Point2D.Float endPoint = new Point2D.Float(otherColumn*columnWidth() + PAD + diameter/2f, height);
                    if (circleColumn > otherColumn) {
                        // draw in Q3:
                        Point2D.Float midPoint = new Point2D.Float(zeroX, height - yCircle/2f);
                        // circleColumn segment
                        g2d.draw(new QuadCurve2D.Float(
                                startPoint.x, startPoint.y, // start of
                                xCircle + diameter/4f, midPoint.y, // ctrl
                                midPoint.x, midPoint.y // end
                        ));
                        // outside segment
                        g2d.draw(new QuadCurve2D.Float(
                                midPoint.x, midPoint.y, // start
                                otherColumn*columnWidth() + PAD + diameter - diameter/4f, midPoint.y, // ctrl
                                endPoint.x, endPoint.y // end
                        ));
                    } else {
                        // draw in Q2:
                        Point2D.Float midPoint = new Point2D.Float(circleColumnWidth, height - yCircle/2f);
                        // circleColumn segment
                        g2d.draw(new QuadCurve2D.Float(
                                startPoint.x, startPoint.y, // start of
                                xCircle + diameter - diameter/4f, midPoint.y, // ctrl
                                midPoint.x, midPoint.y // end
                        ));
                        // outside segment
                        g2d.draw(new QuadCurve2D.Float(
                                midPoint.x, midPoint.y, // start
                                otherColumn*columnWidth() + PAD + diameter/4f, midPoint.y, // ctrl
                                endPoint.x, endPoint.y // end
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
            Shape circle = new Ellipse2D.Float(xCircle, yCircle, diameter, diameter);
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
