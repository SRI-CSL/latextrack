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
package com.sri.ltc;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sri.ltc.git.TemporaryGitRepository;
import com.sri.ltc.server.LTCserverImpl;
import com.sri.ltc.server.LTCserverInterface;
import org.apache.xmlrpc.XmlRpcException;
import org.junit.*;

import java.awt.*;
import java.io.File;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test author colors and preferences.
 * @author linda
 */
public final class TestColors {

    private final static LTCserverInterface API = new LTCserverImpl();
    private final static String TEST_NAME = "Annette von Stockhausen";
    private final static String TEST_EMAIL = "Annette.v.Stockhausen@theologie.uni-erlangen.de";

    @ClassRule
    public static TemporaryGitRepository temporaryGitRepository1 = new TemporaryGitRepository();
    @ClassRule
    public static TemporaryGitRepository temporaryGitRepository2 = new TemporaryGitRepository();

    @Before
    public void newline() {
        System.out.println();
    }

    @AfterClass
    public static void cleanup() throws XmlRpcException {
        API.reset_color(TEST_NAME, TEST_EMAIL);
    }

    @Test(expected = XmlRpcException.class)
    public void emptyName() throws XmlRpcException {
        API.get_color("", "");
    }

    @Test
    public void longKeys() throws XmlRpcException {
        API.get_color(TEST_NAME, TEST_EMAIL);
    }

    @Test
    public void consistentColor() throws XmlRpcException, InterruptedException {
        String name = "Linda Briesemeister";
        String email = "linda.briesemeister@sri.com";
        Color color = Color.decode(API.get_color(name, email));
        Thread.sleep(100L);
        assertEquals("color is consistent", color, Color.decode(API.get_color(name, email)));
    }

    @Test
    public void resetColor() throws XmlRpcException {
        Color firstColor = Color.decode(API.get_color(TEST_NAME, TEST_EMAIL));
        for (int i = 0; i < LTCserverImpl.NUM_DEFAULT_COLORS; i++) {
            API.reset_color(TEST_NAME, TEST_EMAIL);
            API.get_color(TEST_NAME, TEST_EMAIL);
        }
        API.reset_color(TEST_NAME, TEST_EMAIL);
        assertFalse("random new color is different", firstColor.equals(Color.decode(API.get_color(TEST_NAME, TEST_EMAIL))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void clashingColors() throws Exception {
        // implement with setting switch to allow similar colors to FALSE
        boolean oldSetting = API.get_bool_pref(LTCserverInterface.BoolPrefs.ALLOW_SIMILAR_COLORS.name());
        API.set_bool_pref(LTCserverInterface.BoolPrefs.ALLOW_SIMILAR_COLORS.name(), false);

        // all authors are blue
        java.util.List<String> authorNames = Lists.newArrayList("Anna", "Berta");
        for (String name : authorNames)
            API.set_color(name, "", "#0000ff");
        authorNames.addAll(Lists.newArrayList("Dora", "Emily", "Frances", "Georgia", "Haley")); // some without stored colors

        // create some history and get changes:
        File file = Utils.createGitRepository(temporaryGitRepository1,
                new String[]{"content of first file", "more content of file", "a third content of file", "content of file", "coontent of file", "again content of file", "file file file", "more, more file file file"},
                authorNames.toArray(new String[authorNames.size()]));
        int sessionID = API.init_session(file.getPath());
        // add another self in blue:
        String self = "Carla";
        API.set_color(self, "", "#0000ff"); // use blue as well
        API.set_self(sessionID, self, "");
        authorNames.add(self);
        Map map = API.get_changes(sessionID, false, new byte[0], null, 0);

        // assert that all authors from change set and self have different colors
        Map<Integer,Object[]> authors = (Map<Integer,Object[]>) map.get(LTCserverInterface.KEY_AUTHORS);
        Set<Color> colors = Sets.newHashSet();
        for (Map.Entry<Integer,Object[]> entry : authors.entrySet()) {
            String name = (String) entry.getValue()[0];
            Color color = Color.decode(API.get_color(name, ""));
            for (Color other : colors)
                assertTrue("color of author "+name+" is different from other colors", !color.equals(other));
            colors.add(color);
        }

        // unset color for all authors and setting
        for (String name : authorNames)
            API.reset_color(name, "");
        API.set_bool_pref(LTCserverInterface.BoolPrefs.ALLOW_SIMILAR_COLORS.name(), oldSetting);
    }

    @Test
    public void allowClashingColors() throws Exception {
        // implement with setting switch to allow similar colors to TRUE
        boolean oldSetting = API.get_bool_pref(LTCserverInterface.BoolPrefs.ALLOW_SIMILAR_COLORS.name());
        API.set_bool_pref(LTCserverInterface.BoolPrefs.ALLOW_SIMILAR_COLORS.name(), true);

        // all authors are blue
        String[] authorNames = new String[] {"Anna", "Berta"};
        for (String name : authorNames)
            API.set_color(name, "", "#0000ff");

        File file = Utils.createGitRepository(temporaryGitRepository2,
                new String[]{"content of first file", "more content of file", "a third content of file"},
                authorNames);
        int sessionID = API.init_session(file.getPath());
        API.get_changes(sessionID, false, new byte[0], null, 0);

        // assert that authors can have similar colors
        assertTrue("both colors are the same", CommonUtils.isSimilarTo(
                Color.decode(API.get_color(authorNames[0], "")),
                Color.decode(API.get_color(authorNames[1], ""))));

        // unset color for all authors and setting
        for (String name : authorNames)
            API.reset_color(name, "");
        API.set_bool_pref(LTCserverInterface.BoolPrefs.ALLOW_SIMILAR_COLORS.name(), oldSetting);
    }

    @Test
    public void colorSimilarity() {
        Color color1 = Color.decode("#0000ff");

        assertTrue("blue is similar to blue", CommonUtils.isSimilarTo(color1, Color.blue));
        // five colors around it:
        for (String c : new String[] {"#0000CC", "#3333FF", "#0033CC", "#3333CC"})
            assertTrue("blue is similar to "+c, CommonUtils.isSimilarTo(color1, Color.decode(c)));

        // all predefined colors:
        assertFalse("blue is not similar to red", CommonUtils.isSimilarTo(color1, Color.red));
        assertFalse("blue is not similar to green", CommonUtils.isSimilarTo(color1, Color.green));
        assertFalse("blue is not similar to green", CommonUtils.isSimilarTo(color1, Color.black));
        assertFalse("blue is not similar to green", CommonUtils.isSimilarTo(color1, Color.cyan));
        assertFalse("blue is not similar to green", CommonUtils.isSimilarTo(color1, Color.darkGray));
        assertFalse("blue is not similar to green", CommonUtils.isSimilarTo(color1, Color.gray));
        assertFalse("blue is not similar to green", CommonUtils.isSimilarTo(color1, Color.lightGray));
        assertFalse("blue is not similar to green", CommonUtils.isSimilarTo(color1, Color.magenta));
        assertFalse("blue is not similar to green", CommonUtils.isSimilarTo(color1, Color.orange));
        assertFalse("blue is not similar to green", CommonUtils.isSimilarTo(color1, Color.pink));
        assertFalse("blue is not similar to green", CommonUtils.isSimilarTo(color1, Color.white));
        assertFalse("blue is not similar to green", CommonUtils.isSimilarTo(color1, Color.yellow));

        // some random colors:
        assertFalse("blue is not similar to orange", CommonUtils.isSimilarTo(color1, Color.decode("#ff6600")));
        assertFalse("blue is not similar to grey", CommonUtils.isSimilarTo(color1, Color.decode("#ccd4d9")));
        assertFalse("blue is not similar to bright green", CommonUtils.isSimilarTo(color1, Color.decode("#66FF99")));
        assertFalse("blue is not similar to turquoise", CommonUtils.isSimilarTo(color1, Color.decode("#00FFFF")));

    }
}
