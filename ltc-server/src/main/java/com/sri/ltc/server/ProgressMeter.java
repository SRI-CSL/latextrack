/*
 * #%L
 * LaTeX Track Changes (LTC) allows collaborators on a version-controlled LaTeX writing project to view and query changes in the .tex documents.
 * %%
 * Copyright (C) 2009 - 2014 SRI International
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
package com.sri.ltc.server;

import com.sri.ltc.ProgressReceiver;

/**
 * @author linda
 */
public final class ProgressMeter extends Thread implements ProgressReceiver {
    @Override
    public void updateProgress(int percent) {
        StringBuilder bar = new StringBuilder("[");

        for (int i = 0; i < 50; i++) {
            if (i < (percent/2)){
                bar.append("=");
            } else if (i == (percent/2)) {
                bar.append(">");
            } else {
                bar.append(" ");
            }
        }

        bar.append("]   " + percent + "%     ");
        System.out.print("\r" + bar.toString());
        if (percent >= 100)
            System.out.println();
        else
            System.out.flush();
    }
}
