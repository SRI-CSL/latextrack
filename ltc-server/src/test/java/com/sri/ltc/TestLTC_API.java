/**
 ************************ 80 columns *******************************************
 * TestLTC_API
 *
 * Created on 11/1/12.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc;

import com.google.common.collect.Sets;
import com.sri.ltc.git.TemporaryGitRepository;
import com.sri.ltc.server.LTCserverImpl;
import com.sri.ltc.server.LTCserverInterface;
import com.sri.ltc.versioncontrol.TrackedFile;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Test cases for API calls in LTC.
 * @author linda
 */
public class TestLTC_API {

    @ClassRule
    public static TemporaryGitRepository repository = new TemporaryGitRepository();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final static LTCserverInterface API = new LTCserverImpl();

    @Test
    public void bugReport() throws Exception {
        // create moderately interesting git repository
        // create, add and commit 2 files:
        TrackedFile file1 = repository.createTestFileInRepository("file1-", ".txt", "contents of file1", true);
        file1.commit("commtting file1");
        TrackedFile file2 = repository.createTestFileInRepository("file2-", ".txt", "contents of file2", true);
        file2.commit("committing file2");
        // modify and commit file 2:
        repository.modifyTestFileInRepository(file2, "changing contents of file2", false);
        file2.commit("changed file2");

        int sessionID = API.init_session(file2.getFile().getPath());
        String reportPath;

        // first case: existing directory for bug report
        checkReport(API.create_bug_report(sessionID, "something is wrong", folder.getRoot().getPath()),
                "something is wrong");

        // second case: non-existing directory for bug report
        checkReport(API.create_bug_report(sessionID, "even more is wrong", folder.getRoot().getPath() + "/bla"),
                "even more is wrong");
    }

    private void checkReport(String reportPath, String message) {
        // check file stuff
        File file = new File(reportPath);
        assertEquals("file name is report.zip", "report.zip", file.getName());
        assertTrue("file exists", file.exists());
        assertTrue("file is a file", file.isFile());

        ZipFile zipFile = null;
        try {
            // check that it is a zip file
            zipFile = new ZipFile(file);

            // check that it contains "report.xml"
            ZipEntry xmlEntry = zipFile.getEntry("report.xml");
            assertTrue("ZIP contains \"report.xml\"", xmlEntry != null);
            checkXML(zipFile.getInputStream(xmlEntry), message);

            // TODO: check that it contains a bundle?
        } catch (IOException e) {
            fail("file is not a ZIP: "+e.getMessage());
        } finally {
            if (zipFile != null)
                try {
                    zipFile.close();
                } catch (IOException e) { //ignored
                }
        }
    }

    private void checkXML(InputStream is, String message) {
        // root = bug-report
        // contains elements user-message (with given message), relative-file-path, filters (with 3 sub),
        // active-revisions, show-options (with at least one show-option sub)

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(is);

            // root element is "bug-report"
            Element root = document.getDocumentElement();
            assertEquals("root element", root.getTagName(), "bug-report");

            NodeList nodes, children, grandchildren;
            // contains "user-message" element with given message
            nodes = root.getElementsByTagName("user-message");
            assertTrue("only one user message", nodes.getLength() == 1);
            assertEquals("contents is the same is given", message, nodes.item(0).getFirstChild().getNodeValue());

            // contains "relative-file-path" element
            nodes = root.getElementsByTagName("relative-file-path");
            assertTrue("only one file path", nodes.getLength() == 1);

            // contains "filters" element with 3 sub-elements
            nodes = root.getElementsByTagName("filters");
            assertTrue("only one element for filters", nodes.getLength() == 1);
            children = nodes.item(0).getChildNodes();
            assertTrue("filters has at least 3 children", children.getLength() >= 3);
            Set<String> tagsToFind = Sets.newHashSet("revision","date","authors");
            for (int i = 0; i < children.getLength(); i++) {
                grandchildren = children.item(i).getChildNodes();
                if (grandchildren instanceof Element)
                    tagsToFind.remove(((Element) grandchildren).getNodeName());
            }
            assertTrue("three filters found", tagsToFind.isEmpty());

            // contains "active-revisions" element
            nodes = root.getElementsByTagName("active-revisions");
            assertTrue("only one list of active revisions", nodes.getLength() == 1);

            // contains "show-options" element with at least one "show-option" sub-element
            nodes = root.getElementsByTagName("show-options");
            assertTrue("only one list of options to show", nodes.getLength() == 1);
            children = nodes.item(0).getChildNodes();
            assertTrue("at least one show option", children.getLength() >= 1);
        } catch (ParserConfigurationException e) {
            fail("cannot configure XML parsing: "+e.getMessage());
        } catch (SAXException e) {
            fail("cannot parse input: "+e.getMessage());
        } catch (IOException e) {
            fail("IOException while parsing XML: "+e.getMessage());
        }
    }
}
