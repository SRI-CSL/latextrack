package com.sri.ltc;

import com.sri.ltc.server.LTCserverImpl;
import com.sri.ltc.server.LTCserverInterface;
import org.apache.xmlrpc.XmlRpcException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Test author colors and preferences.
 * @author linda
 */
public final class TestColors {

    private final static LTCserverInterface API = new LTCserverImpl();
    private final static String TEST_NAME = "Annette von Stockhausen";
    private final static String TEST_EMAIL = "Annette.v.Stockhausen@theologie.uni-erlangen.de";

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
        API.reset_color(TEST_NAME, TEST_EMAIL);
        assertFalse("random new color is different", firstColor.equals(Color.decode(API.get_color(TEST_NAME, TEST_EMAIL))));
    }

    // TODO: colors clashing with existing ones?
}
