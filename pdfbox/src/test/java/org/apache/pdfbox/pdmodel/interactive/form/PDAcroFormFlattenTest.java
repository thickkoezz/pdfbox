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
package org.apache.pdfbox.pdmodel.interactive.form;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.TestPDFToImage;
import org.junit.Assert;
import org.junit.Before;

/**
 * Test flatten different forms and compare with rendering.
 *
 * The tests are currently disabled to not run within the CI environment as the
 * test results need manual inspection. Enable as needed.
 *
 */
public class PDAcroFormFlattenTest {

  private static final File IN_DIR = new File("target/test-output/flatten/in");
  private static final File OUT_DIR = new File("target/test-output/flatten/out");

  @Before
  public void setUp() {
    PDAcroFormFlattenTest.IN_DIR.mkdirs();
    PDAcroFormFlattenTest.OUT_DIR.mkdirs();
  }

  /*
   * PDFBOX-142 Filled template.
   */
  // @Test
  public void testFlattenPDFBOX142() throws IOException {
    final String sourceUrl = "https://issues.apache.org/jira/secure/attachment/12742551/Testformular1.pdf";
    final String targetFileName = "Testformular1.pdf";

    PDAcroFormFlattenTest.flattenAndCompare(sourceUrl, targetFileName);
  }

  /*
   * PDFBOX-563 Filled template.
   */
  // @Test
  public void testFlattenPDFBOX563() throws IOException {
    final String sourceUrl = "https://issues.apache.org/jira/secure/attachment/12425859/TestFax_56972.pdf";
    final String targetFileName = "TestFax_56972.pdf";

    PDAcroFormFlattenTest.flattenAndCompare(sourceUrl, targetFileName);
  }

  /*
   * PDFBOX-2469 Empty template.
   */
  // @Test
  public void testFlattenPDFBOX2469Empty() throws IOException {
    final String sourceUrl = "https://issues.apache.org/jira/secure/attachment/12682897/FormI-9-English.pdf";
    final String targetFileName = "FormI-9-English.pdf";

    PDAcroFormFlattenTest.flattenAndCompare(sourceUrl, targetFileName);
  }

  /*
   * PDFBOX-2469 Filled template.
   */
  // @Test
  public void testFlattenPDFBOX2469Filled() throws IOException {
    final String sourceUrl = "https://issues.apache.org/jira/secure/attachment/12678455/testPDF_acroForm.pdf";
    final String targetFileName = "testPDF_acroForm.pdf";

    PDAcroFormFlattenTest.flattenAndCompare(sourceUrl, targetFileName);
  }

  /*
   * PDFBOX-2586 Empty template.
   */
  // @Test
  public void testFlattenPDFBOX2586() throws IOException {
    final String sourceUrl = "https://issues.apache.org/jira/secure/attachment/12689788/test.pdf";
    final String targetFileName = "test-2586.pdf";

    PDAcroFormFlattenTest.flattenAndCompare(sourceUrl, targetFileName);
  }

  /*
   * PDFBOX-3083 Filled template rotated.
   */
  // @Test
  public void testFlattenPDFBOX3083() throws IOException {
    final String sourceUrl = "https://issues.apache.org/jira/secure/attachment/12770263/mypdf.pdf";
    final String targetFileName = "mypdf.pdf";

    PDAcroFormFlattenTest.flattenAndCompare(sourceUrl, targetFileName);
  }

  /*
   * PDFBOX-3262 Hidden fields
   */
  // @Test
  public void testFlattenPDFBOX3262() throws IOException {
    final String sourceUrl = "https://issues.apache.org/jira/secure/attachment/12792007/hidden_fields.pdf";
    final String targetFileName = "hidden_fields.pdf";

    Assert.assertTrue(PDAcroFormFlattenTest.flattenAndCompare(sourceUrl, targetFileName));
  }

  /*
   * PDFBOX-3396 Signed Document 1.
   */
  // @Test
  public void testFlattenPDFBOX3396_1() throws IOException {
    final String sourceUrl = "https://issues.apache.org/jira/secure/attachment/12816014/Signed-Document-1.pdf";
    final String targetFileName = "Signed-Document-1.pdf";

    PDAcroFormFlattenTest.flattenAndCompare(sourceUrl, targetFileName);
  }

  /*
   * PDFBOX-3396 Signed Document 2.
   */
  // @Test
  public void testFlattenPDFBOX3396_2() throws IOException {
    final String sourceUrl = "https://issues.apache.org/jira/secure/attachment/12816016/Signed-Document-2.pdf";
    final String targetFileName = "Signed-Document-2.pdf";

    PDAcroFormFlattenTest.flattenAndCompare(sourceUrl, targetFileName);
  }

  /*
   * PDFBOX-3396 Signed Document 3.
   */
  // @Test
  public void testFlattenPDFBOX3396_3() throws IOException {
    final String sourceUrl = "https://issues.apache.org/jira/secure/attachment/12821307/Signed-Document-3.pdf";
    final String targetFileName = "Signed-Document-3.pdf";

    PDAcroFormFlattenTest.flattenAndCompare(sourceUrl, targetFileName);
  }

  /*
   * PDFBOX-3396 Signed Document 4.
   */
  // @Test
  public void testFlattenPDFBOX3396_4() throws IOException {
    final String sourceUrl = "https://issues.apache.org/jira/secure/attachment/12821308/Signed-Document-4.pdf";
    final String targetFileName = "Signed-Document-4.pdf";

    PDAcroFormFlattenTest.flattenAndCompare(sourceUrl, targetFileName);
  }

  /*
   * PDFBOX-3587 Empty template.
   */
  // @Test
  public void testFlattenOpenOfficeForm() throws IOException {
    final String sourceUrl = "https://issues.apache.org/jira/secure/attachment/12839977/OpenOfficeForm.pdf";
    final String targetFileName = "OpenOfficeForm.pdf";

    PDAcroFormFlattenTest.flattenAndCompare(sourceUrl, targetFileName);
  }

  /*
   * PDFBOX-3587 Filled template.
   */
  // @Test
  public void testFlattenOpenOfficeFormFilled() throws IOException {
    final String sourceUrl = "https://issues.apache.org/jira/secure/attachment/12840280/OpenOfficeForm_filled.pdf";
    final String targetFileName = "OpenOfficeForm_filled.pdf";

    PDAcroFormFlattenTest.flattenAndCompare(sourceUrl, targetFileName);
  }

  /*
   * Flatten and compare with generated image samples.
   */
  private static boolean flattenAndCompare(final String sourceUrl, final String targetFileName) throws IOException {

    PDAcroFormFlattenTest.generateSamples(sourceUrl, targetFileName);

    final File inputFile = new File(PDAcroFormFlattenTest.IN_DIR, targetFileName);
    final File outputFile = new File(PDAcroFormFlattenTest.OUT_DIR, targetFileName);

    try (PDDocument testPdf = PDDocument.load(inputFile)) {
      testPdf.getDocumentCatalog().getAcroForm().flatten();
      testPdf.setAllSecurityToBeRemoved(true);
      Assert.assertTrue(testPdf.getDocumentCatalog().getAcroForm().getFields().isEmpty());
      testPdf.save(outputFile);
    }

    // compare rendering
    final TestPDFToImage testPDFToImage = new TestPDFToImage(TestPDFToImage.class.getName());
    if (!testPDFToImage.doTestFile(outputFile, PDAcroFormFlattenTest.IN_DIR.getAbsolutePath(),
        PDAcroFormFlattenTest.OUT_DIR.getAbsolutePath())) {
      // don't fail, rendering is different on different systems, result must be
      // viewed manually
      System.out.println("Rendering of " + outputFile + " failed or is not identical to expected rendering in "
          + PDAcroFormFlattenTest.IN_DIR + " directory");
      PDAcroFormFlattenTest.removeMatchingRenditions(inputFile);
      return false;
    } else {
      // cleanup input and output directory for matching files.
      PDAcroFormFlattenTest.removeAllRenditions(inputFile);
      inputFile.delete();
      outputFile.delete();
    }

    return true;
  }

  /*
   * Generate the sample images to which the PDF will be compared after flatten.
   */
  private static void generateSamples(final String sourceUrl, final String targetFile) throws IOException {
    PDAcroFormFlattenTest.getFromUrl(sourceUrl, targetFile);

    final File file = new File(PDAcroFormFlattenTest.IN_DIR, targetFile);

    try (PDDocument document = PDDocument.load(file, (String) null)) {
      final String outputPrefix = PDAcroFormFlattenTest.IN_DIR.getAbsolutePath() + '/' + file.getName() + "-";
      final int numPages = document.getNumberOfPages();

      final PDFRenderer renderer = new PDFRenderer(document);
      for (int i = 0; i < numPages; i++) {
        final String fileName = outputPrefix + (i + 1) + ".png";
        final BufferedImage image = renderer.renderImageWithDPI(i, 96); // Windows native DPI
        ImageIO.write(image, "PNG", new File(fileName));
      }
    }
  }

  /*
   * Get a PDF from URL and copy to file for processing.
   */
  private static void getFromUrl(final String sourceUrl, final String targetFile) throws IOException {
    final URL url = new URL(sourceUrl);

    try (InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(new File(PDAcroFormFlattenTest.IN_DIR, targetFile))) {

      final byte[] b = new byte[2048];
      int length;

      while ((length = is.read(b)) != -1) {
        os.write(b, 0, length);
      }
    }
  }

  /*
   * Remove renditions for the PDF from the input directory for which there is no
   * corresponding rendition in the output directory. Renditions in the output
   * directory which were identical to the ones in the input directory will have
   * been deleted by the TestPDFToImage utility.
   */
  private static void removeMatchingRenditions(final File inputFile) {
    final File[] testFiles = inputFile.getParentFile().listFiles(
        (FilenameFilter) (dir, name) -> (name.startsWith(inputFile.getName()) && name.toLowerCase().endsWith(".png")));

    for (final File testFile : testFiles) {
      if (!new File(PDAcroFormFlattenTest.OUT_DIR, testFile.getName()).exists()) {
        testFile.delete();
      }
    }
  }

  /*
   * Remove renditions for the PDF from the input directory. The output directory
   * will have been cleaned by the TestPDFToImage utility.
   */
  private static void removeAllRenditions(final File inputFile) {
    final File[] testFiles = inputFile.getParentFile().listFiles(
        (FilenameFilter) (dir, name) -> (name.startsWith(inputFile.getName()) && name.toLowerCase().endsWith(".png")));

    for (final File testFile : testFiles) {
      testFile.delete();
    }
  }
}
