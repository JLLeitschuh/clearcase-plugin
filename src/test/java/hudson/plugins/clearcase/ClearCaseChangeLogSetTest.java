/**
 * The MIT License
 *
 * Copyright (c) 2007-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Erik Ramfelt,
 *                          Henrik Lynggaard, Peter Liljenberg, Andrew Bayer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.clearcase;

import hudson.plugins.clearcase.ClearCaseChangeLogEntry.FileElement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

public class ClearCaseChangeLogSetTest {

    @Test
    public void testGetParent() throws IOException, SAXException {
        ClearCaseChangeLogSet logSet = ClearCaseChangeLogSet.parse(null, ClearCaseChangeLogSetTest.class.getResourceAsStream("changelog.xml"));
        List<ClearCaseChangeLogEntry> logs = logSet.getLogs();

        for (ClearCaseChangeLogEntry entry : logs) {
            Assert.assertNotNull("Parent is null", entry.getParent());
        }
    }

    @Test
    public void testMultipleFilesInLogEntry() throws IOException, SAXException {
        ClearCaseChangeLogEntry entry = new ClearCaseChangeLogEntry();
        entry.setUser("Anka");
        entry.setComment("comment");
        entry.setDate(Calendar.getInstance().getTime());
        entry.addElement(new FileElement("file1", "version1", "action1", "mkelem"));
        entry.addElement(new FileElement("file2", "version2", "action2", "mkelem"));

        List<ClearCaseChangeLogEntry> history = new ArrayList<ClearCaseChangeLogEntry>();
        history.add(entry);

        File tempLogFile = File.createTempFile("clearcase", "xml");
        tempLogFile.deleteOnExit();
        FileOutputStream fileOutputStream = new FileOutputStream(tempLogFile);

        ClearCaseChangeLogSet.saveToChangeLog(fileOutputStream, history);
        fileOutputStream.close();

        FileInputStream fileInputStream = new FileInputStream(tempLogFile);
        ClearCaseChangeLogSet logSet = ClearCaseChangeLogSet.parse(null, fileInputStream);
        fileInputStream.close();
        List<ClearCaseChangeLogEntry> logs = logSet.getLogs();

        Assert.assertEquals("The number of change log entries is incorrect", 1, logs.size());
        Assert.assertEquals("The number of files in the first log entry is incorrect", 2, logs.get(0).getElements().size());
        Assert.assertEquals("The first file name is incorrect", "file1", logs.get(0).getElements().get(0).getFile());
        Assert.assertEquals("The first version is incorrect", "version1", logs.get(0).getElements().get(0).getVersion());
        Assert.assertEquals("The first action is incorrect", "action1", logs.get(0).getElements().get(0).getAction());
        Assert.assertEquals("The first operation is incorrect", "mkelem", logs.get(0).getElements().get(0).getOperation());
        Assert.assertEquals("The second file name is incorrect", "file2", logs.get(0).getElements().get(1).getFile());
        Assert.assertEquals("The second version is incorrect", "version2", logs.get(0).getElements().get(1).getVersion());
        Assert.assertEquals("The second action is incorrect", "action2", logs.get(0).getElements().get(1).getAction());
        Assert.assertEquals("The second operation is incorrect", "mkelem", logs.get(0).getElements().get(1).getOperation());
    }

    @Test
    public void testParse() throws IOException, SAXException {
        ClearCaseChangeLogSet logSet = ClearCaseChangeLogSet.parse(null, ClearCaseChangeLogSetTest.class.getResourceAsStream("changelog.xml"));
        List<ClearCaseChangeLogEntry> logs = logSet.getLogs();
        Assert.assertEquals("Number of logs is incorrect", 3, logs.size());
        Assert.assertEquals("The user is incorrect", "qhensam", logs.get(0).getUser());
        Assert.assertEquals("The date is incorrect", "Tue Aug 28 15:27:00 CEST 2007", logs.get(0).getDateStr());
    }

    @Test
    public void testParseMultipleEntries() throws IOException, SAXException {
        ClearCaseChangeLogSet logSet = ClearCaseChangeLogSet.parse(null, ClearCaseChangeLogSetTest.class.getResourceAsStream("changelog-multi.xml"));
        List<ClearCaseChangeLogEntry> logs = logSet.getLogs();
        Assert.assertEquals("Number of logs is incorrect", 1, logs.size());
        Assert.assertEquals("The user is incorrect", "qhensam", logs.get(0).getUser());
        Assert.assertEquals("Number of file elements is incorrect", 3, logs.get(0).getElements().size());
        Assert.assertEquals("Name of file element is incorrect", "Source\\OperatorControls\\UserControlResourceView.cs", logs.get(0).getElements().get(0)
                .getFile());
        Assert.assertEquals("Version of file element is incorrect", "\\main\\sit_r5_maint\\3", logs.get(0).getElements().get(0).getVersion());
        Assert.assertEquals("Version of file element is incorrect", "create version", logs.get(0).getElements().get(0).getAction());
        Assert.assertEquals("Name of file element is incorrect", "Source\\Operator\\FormStationOverview.cs", logs.get(0).getElements().get(1).getFile());
        Assert.assertEquals("Version of file element is incorrect", "\\main\\sit_r5_maint\\4", logs.get(0).getElements().get(1).getVersion());
        Assert.assertEquals("Version of file element is incorrect", "create version", logs.get(0).getElements().get(1).getAction());
        Assert.assertEquals("Name of file element is incorrect", "Source\\OperatorControls\\ResourcesOperatorControlsTexts.sv.resx", logs.get(0).getElements()
                .get(2).getFile());
        Assert.assertEquals("Version of file element is incorrect", "\\main\\sit_r5_maint\\1", logs.get(0).getElements().get(2).getVersion());
        Assert.assertEquals("Version of file element is incorrect", "create version", logs.get(0).getElements().get(2).getAction());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testUnicodeXml() throws IOException, SAXException {
        ClearCaseChangeLogEntry entry = new ClearCaseChangeLogEntry();
        entry.setUser("B\u00FClow");
        entry.setAction("action");
        entry.setComment("comment");
        entry.setDate(Calendar.getInstance().getTime());
        entry.setVersion("version");

        List<ClearCaseChangeLogEntry> history = new ArrayList<ClearCaseChangeLogEntry>();
        history.add(entry);

        File tempLogFile = File.createTempFile("clearcase", "xml");
        tempLogFile.deleteOnExit();
        FileOutputStream fileOutputStream = new FileOutputStream(tempLogFile);

        ClearCaseChangeLogSet.saveToChangeLog(fileOutputStream, history);
        fileOutputStream.close();

        FileInputStream fileInputStream = new FileInputStream(tempLogFile);
        ClearCaseChangeLogSet logSet = ClearCaseChangeLogSet.parse(null, fileInputStream);
        fileInputStream.close();
        List<ClearCaseChangeLogEntry> logs = logSet.getLogs();

        Assert.assertEquals("The comment wasnt correct", "B\u00FClow", logs.get(0).getUser());
    }
}
