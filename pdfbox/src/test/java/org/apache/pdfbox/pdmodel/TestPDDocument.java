/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Locale;

import org.apache.pdfbox.io.IOUtils;

import junit.framework.TestCase;

/**
 * Testcase introduced with PDFBOX-1581.
 *
 */
public class TestPDDocument extends TestCase {
  private final File testResultsDir = new File("target/test-output");

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    testResultsDir.mkdirs();
  }

  /**
   * Test document save/load using a stream.
   * 
   * @throws IOException if something went wrong
   */
  public void testSaveLoadStream() throws IOException {
    // Create PDF with one blank page
    final PDDocument document = new PDDocument();
    document.addPage(new PDPage());

    // Save
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    document.save(baos);
    document.close();

    // Verify content
    final byte[] pdf = baos.toByteArray();
    TestCase.assertTrue(pdf.length > 200);
    TestCase.assertEquals("%PDF-1.4", new String(Arrays.copyOfRange(pdf, 0, 8), "UTF-8"));
    TestCase.assertEquals("%%EOF\n", new String(Arrays.copyOfRange(pdf, pdf.length - 6, pdf.length), "UTF-8"));

    // Load
    final PDDocument loadDoc = PDDocument.load(new ByteArrayInputStream(pdf));
    TestCase.assertEquals(1, loadDoc.getNumberOfPages());
    loadDoc.close();
  }

  /**
   * Test document save/load using a file.
   * 
   * @throws IOException if something went wrong
   */
  public void testSaveLoadFile() throws IOException {
    // Create PDF with one blank page
    final PDDocument document = new PDDocument();
    document.addPage(new PDPage());

    // Save
    final File targetFile = new File(testResultsDir, "pddocument-saveloadfile.pdf");
    document.save(targetFile);
    document.close();

    // Verify content
    TestCase.assertTrue(targetFile.length() > 200);
    final InputStream in = new FileInputStream(targetFile);
    final byte[] pdf = IOUtils.toByteArray(in);
    in.close();
    TestCase.assertTrue(pdf.length > 200);
    TestCase.assertEquals("%PDF-1.4", new String(Arrays.copyOfRange(pdf, 0, 8), "UTF-8"));
    TestCase.assertEquals("%%EOF\n", new String(Arrays.copyOfRange(pdf, pdf.length - 6, pdf.length), "UTF-8"));

    // Load
    final PDDocument loadDoc = PDDocument.load(targetFile);
    TestCase.assertEquals(1, loadDoc.getNumberOfPages());
    loadDoc.close();
  }

  /**
   * Test get/setVersion.
   * 
   * @throws IOException if something went wrong
   */
  public void testVersions() throws IOException {
    PDDocument document = new PDDocument();
    // test default version
    TestCase.assertEquals(1.4f, document.getVersion());
    TestCase.assertEquals(1.4f, document.getDocument().getVersion());
    TestCase.assertEquals("1.4", document.getDocumentCatalog().getVersion());
    // force downgrading version (header)
    document.getDocument().setVersion(1.3f);
    document.getDocumentCatalog().setVersion(null);
    // test new version (header)
    TestCase.assertEquals(1.3f, document.getVersion());
    TestCase.assertEquals(1.3f, document.getDocument().getVersion());
    TestCase.assertNull(document.getDocumentCatalog().getVersion());
    document.close();

    // check if version downgrade is denied
    document = new PDDocument();
    document.setVersion(1.3f);
    // all versions shall have their default value
    TestCase.assertEquals(1.4f, document.getVersion());
    TestCase.assertEquals(1.4f, document.getDocument().getVersion());
    TestCase.assertEquals("1.4", document.getDocumentCatalog().getVersion());

    // check version upgrade
    document.setVersion(1.5f);
    // overall version has to be 1.5f
    TestCase.assertEquals(1.5f, document.getVersion());
    // header version has to be unchanged
    TestCase.assertEquals(1.4f, document.getDocument().getVersion());
    // catalog version version has to be 1.5
    TestCase.assertEquals("1.5", document.getDocumentCatalog().getVersion());
    document.close();
  }

  /**
   * Test whether a bad file can be deleted after load() failed.
   *
   * @throws java.io.FileNotFoundException
   */
  public void testDeleteBadFile() throws FileNotFoundException {
    final File f = new File("test.pdf");
    final PrintWriter pw = new PrintWriter(new FileOutputStream(f));
    pw.write("<script language='JavaScript'>");
    pw.close();
    PDDocument doc = null;
    try {
      doc = PDDocument.load(f);
      TestCase.fail("parsing should fail");
    } catch (final IOException ex) {
      // expected
    } finally {
      TestCase.assertNull(doc);
    }

    final boolean deleted = f.delete();
    TestCase.assertTrue("delete bad file failed after failed load()", deleted);
  }

  /**
   * Test whether a good file can be deleted after load() and close() succeed.
   *
   * @throws java.io.FileNotFoundException
   */
  public void testDeleteGoodFile() throws IOException {
    final File f = new File("test.pdf");
    final PDDocument doc = new PDDocument();
    doc.addPage(new PDPage());
    doc.save(f);
    doc.close();

    PDDocument.load(f).close();

    final boolean deleted = f.delete();
    TestCase.assertTrue("delete good file failed after successful load() and close()", deleted);
  }

  /**
   * PDFBOX-3481: Test whether XRef generation results in unusable PDFs if Arab
   * numbering is default.
   */
  public void testSaveArabicLocale() throws IOException {
    final Locale defaultLocale = Locale.getDefault();
    final Locale arabicLocale = new Locale.Builder().setLanguageTag("ar-EG-u-nu-arab").build();
    Locale.setDefault(arabicLocale);

    final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    final PDDocument doc = new PDDocument();
    doc.addPage(new PDPage());
    doc.save(baos);
    doc.close();

    PDDocument.load(new ByteArrayInputStream(baos.toByteArray())).close();

    Locale.setDefault(defaultLocale);
  }
}
