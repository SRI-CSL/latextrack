/*
 * #%L
 * LaTeX Track Changes (LTC) allows collaborators on a version-controlled LaTeX writing project to view and query changes in the .tex documents.
 * %%
 * Copyright (C) 2009 - 2013 SRI International
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
package com.sri.ltc.xplatform;

//import com.apple.eawt.AboutHandler;
//import com.apple.eawt.AppEvent;
import com.sri.ltc.CommonUtils;

import javax.swing.*;
import java.io.Console;
import java.net.URL;

/**
 * @author linda
 */
public final class MacOSXApp implements AppInterface {

    private final String image;

    public MacOSXApp(String image) {
        this.image = image;
    }

    @Override
    public void customize() {
        AppInterface.LOGGER.fine("Customizing Mac OS X application");

//        com.apple.eawt.Application application = com.apple.eawt.Application.getApplication();

//        if (image != null) {
//            URL imageURL = Console.class.getResource(image);
//            if (imageURL != null) {
//                ImageIcon icon = new ImageIcon(imageURL);
//                application.setDockIconImage(icon.getImage());
//            }
//        }

//        application.setAboutHandler(null);
//        application.setAboutHandler(new AboutHandler() {
//            @Override
//            public void handleAbout(AppEvent.AboutEvent aboutEvent) {
//                // display copyright/license information
//                JOptionPane.showMessageDialog(null,
//                        "LTC version "+CommonUtils.getVersion()+"\n"+
//                                "Build number "+CommonUtils.getBuildInfo()+"\n\n"+
//                                CommonUtils.getNotice(),
//                        "About LaTeX Track Changes (LTC)",
//                        JOptionPane.PLAIN_MESSAGE,
//                        CommonUtils.getLogo());
//            }
//        });
//
//        application.setQuitHandler(new com.apple.eawt.QuitHandler() {
//            @Override
//            public void handleQuitRequestWith(com.apple.eawt.AppEvent.QuitEvent quitEvent,
//                                              com.apple.eawt.QuitResponse quitResponse) {
//                // TODO: do anything here?
//                quitResponse.performQuit();
//            }
//        });
        // TODO: enable preferences and set handler?
    }

}
