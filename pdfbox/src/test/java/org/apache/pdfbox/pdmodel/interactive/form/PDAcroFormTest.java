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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.rendering.TestPDFToImage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for the PDButton class.
 *
 */
public class PDAcroFormTest {

  private PDDocument document;
  private PDAcroForm acroForm;

  private static final File OUT_DIR = new File("target/test-output");
  private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/pdmodel/interactive/form");

  @Before
  public void setUp() {
    document = new PDDocument();
    acroForm = new PDAcroForm(document);
    document.getDocumentCatalog().setAcroForm(acroForm);
  }

  @Test
  public void testFieldsEntry() {
    // the /Fields entry has been created with the AcroForm
    // as this is a required entry
    Assert.assertNotNull(acroForm.getFields());
    Assert.assertEquals(acroForm.getFields().size(), 0);

    // there shouldn't be an exception if there is no such field
    Assert.assertNull(acroForm.getField("foo"));

    // remove the required entry which is the case for some
    // PDFs (see PDFBOX-2965)
    acroForm.getCOSObject().removeItem(COSName.FIELDS);

    // ensure there is always an empty collection returned
    Assert.assertNotNull(acroForm.getFields());
    Assert.assertEquals(acroForm.getFields().size(), 0);

    // there shouldn't be an exception if there is no such field
    Assert.assertNull(acroForm.getField("foo"));
  }

  @Test
  public void testAcroFormProperties() {
    Assert.assertTrue(acroForm.getDefaultAppearance().isEmpty());
    acroForm.setDefaultAppearance("/Helv 0 Tf 0 g");
    Assert.assertEquals(acroForm.getDefaultAppearance(), "/Helv 0 Tf 0 g");
  }

  @Test
  public void testFlatten() throws IOException {
    final File file = new File(PDAcroFormTest.OUT_DIR, "AlignmentTests-flattened.pdf");
    try (PDDocument testPdf = PDDocument.load(new File(PDAcroFormTest.IN_DIR, "AlignmentTests.pdf"))) {
      testPdf.getDocumentCatalog().getAcroForm().flatten();
      Assert.assertTrue(testPdf.getDocumentCatalog().getAcroForm().getFields().isEmpty());
      testPdf.save(file);
    }
    // compare rendering
    final TestPDFToImage testPDFToImage = new TestPDFToImage(TestPDFToImage.class.getName());
    if (!testPDFToImage.doTestFile(file, PDAcroFormTest.IN_DIR.getAbsolutePath(),
        PDAcroFormTest.OUT_DIR.getAbsolutePath())) {
      // don't fail, rendering is different on different systems, result must be
      // viewed manually
      System.out.println("Rendering of " + file + " failed or is not identical to expected rendering in "
          + PDAcroFormTest.IN_DIR + " directory");
    }

  }

  /*
   * Same as above but remove the page reference from the widget annotation before
   * doing the flatten() to ensure that the widgets page reference is properly
   * looked up (PDFBOX-3301)
   */
  @Test
  public void testFlattenWidgetNoRef() throws IOException {
    final File file = new File(PDAcroFormTest.OUT_DIR, "AlignmentTests-flattened-noRef.pdf");

    try (PDDocument testPdf = PDDocument.load(new File(PDAcroFormTest.IN_DIR, "AlignmentTests.pdf"))) {
      final PDAcroForm acroFormToTest = testPdf.getDocumentCatalog().getAcroForm();
      for (final PDField field : acroFormToTest.getFieldTree()) {
        for (final PDAnnotationWidget widget : field.getWidgets()) {
          widget.getCOSObject().removeItem(COSName.P);
        }
      }
      acroFormToTest.flatten();
      Assert.assertTrue(acroFormToTest.getFields().isEmpty());
      testPdf.save(file);
    }
    // compare rendering
    final TestPDFToImage testPDFToImage = new TestPDFToImage(TestPDFToImage.class.getName());
    if (!testPDFToImage.doTestFile(file, PDAcroFormTest.IN_DIR.getAbsolutePath(),
        PDAcroFormTest.OUT_DIR.getAbsolutePath())) {
      // don't fail, rendering is different on different systems, result must be
      // viewed manually
      System.out.println("Rendering of " + file + " failed or is not identical to expected rendering in "
          + PDAcroFormTest.IN_DIR + " directory");
    }
  }

  @Test
  public void testFlattenSpecificFieldsOnly() throws IOException {
    final File file = new File(PDAcroFormTest.OUT_DIR, "AlignmentTests-flattened-specificFields.pdf");

    final List<PDField> fieldsToFlatten = new ArrayList<>();

    try (PDDocument testPdf = PDDocument.load(new File(PDAcroFormTest.IN_DIR, "AlignmentTests.pdf"))) {
      final PDAcroForm acroFormToFlatten = testPdf.getDocumentCatalog().getAcroForm();
      final int numFieldsBeforeFlatten = acroFormToFlatten.getFields().size();
      final int numWidgetsBeforeFlatten = countWidgets(testPdf);

      fieldsToFlatten.add(acroFormToFlatten.getField("AlignLeft-Border_Small-Filled"));
      fieldsToFlatten.add(acroFormToFlatten.getField("AlignLeft-Border_Medium-Filled"));
      fieldsToFlatten.add(acroFormToFlatten.getField("AlignLeft-Border_Wide-Filled"));
      fieldsToFlatten.add(acroFormToFlatten.getField("AlignLeft-Border_Wide_Clipped-Filled"));

      acroFormToFlatten.flatten(fieldsToFlatten, true);
      final int numFieldsAfterFlatten = acroFormToFlatten.getFields().size();
      final int numWidgetsAfterFlatten = countWidgets(testPdf);

      Assert.assertEquals(numFieldsBeforeFlatten, numFieldsAfterFlatten + fieldsToFlatten.size());
      Assert.assertEquals(numWidgetsBeforeFlatten, numWidgetsAfterFlatten + fieldsToFlatten.size());

      testPdf.save(file);
    }
  }

  /*
   * Test that we do not modify an AcroForm with missing resource information when
   * loading the document only. (PDFBOX-3752)
   */
  @Test
  public void testDontAddMissingInformationOnDocumentLoad() {
    try {
      final byte[] pdfBytes = createAcroFormWithMissingResourceInformation();

      try (PDDocument pdfDocument = PDDocument.load(pdfBytes)) {
        // do a low level access to the AcroForm to avoid the generation of missing
        // entries
        final PDDocumentCatalog documentCatalog = pdfDocument.getDocumentCatalog();
        final COSDictionary catalogDictionary = documentCatalog.getCOSObject();
        final COSDictionary acroFormDictionary = (COSDictionary) catalogDictionary
            .getDictionaryObject(COSName.ACRO_FORM);

        // ensure that the missing information has not been generated
        Assert.assertNull(acroFormDictionary.getDictionaryObject(COSName.DA));
        Assert.assertNull(acroFormDictionary.getDictionaryObject(COSName.RESOURCES));

        pdfDocument.close();
      }
    } catch (final IOException e) {
      System.err.println("Couldn't create test document, test skipped");
      return;
    }
  }

  /*
   * Test that we add missing ressouce information to an AcroForm when accessing
   * the AcroForm on the PD level (PDFBOX-3752)
   */
  @Test
  public void testAddMissingInformationOnAcroFormAccess() {
    try {
      final byte[] pdfBytes = createAcroFormWithMissingResourceInformation();

      try (PDDocument pdfDocument = PDDocument.load(pdfBytes)) {
        final PDDocumentCatalog documentCatalog = pdfDocument.getDocumentCatalog();

        // this call shall trigger the generation of missing information
        final PDAcroForm theAcroForm = documentCatalog.getAcroForm();

        // ensure that the missing information has been generated
        // DA entry
        Assert.assertEquals("/Helv 0 Tf 0 g ", theAcroForm.getDefaultAppearance());
        Assert.assertNotNull(theAcroForm.getDefaultResources());

        // DR entry
        final PDResources acroFormResources = theAcroForm.getDefaultResources();
        Assert.assertNotNull(acroFormResources.getFont(COSName.getPDFName("Helv")));
        Assert.assertEquals("Helvetica", acroFormResources.getFont(COSName.getPDFName("Helv")).getName());
        Assert.assertNotNull(acroFormResources.getFont(COSName.getPDFName("ZaDb")));
        Assert.assertEquals("ZapfDingbats", acroFormResources.getFont(COSName.getPDFName("ZaDb")).getName());
      }
    } catch (final IOException e) {
      System.err.println("Couldn't create test document, test skipped");
      return;
    }
  }

  /**
   * PDFBOX-4235: a bad /DA string should not result in an NPE.
   * 
   * @throws IOException
   */
  @Test
  public void testBadDA() throws IOException {
    try (PDDocument doc = new PDDocument()) {
      final PDPage page = new PDPage();
      doc.addPage(page);

      final PDAcroForm acroForm = new PDAcroForm(document);
      doc.getDocumentCatalog().setAcroForm(acroForm);
      acroForm.setDefaultResources(new PDResources());

      final PDTextField textBox = new PDTextField(acroForm);
      textBox.setPartialName("SampleField");

      // https://stackoverflow.com/questions/50609478/
      // "tf" is a typo, should have been "Tf" and this results that no font is chosen
      textBox.setDefaultAppearance("/Helv 0 tf 0 g");
      acroForm.getFields().add(textBox);

      final PDAnnotationWidget widget = textBox.getWidgets().get(0);
      final PDRectangle rect = new PDRectangle(50, 750, 200, 20);
      widget.setRectangle(rect);
      widget.setPage(page);

      page.getAnnotations().add(widget);

      try {
        textBox.setValue("huhu");
      } catch (final IllegalArgumentException ex) {
        return;
      }
      Assert.fail("IllegalArgumentException should have been thrown");
    }
  }

  /**
   * PDFBOX-3732, PDFBOX-4303, PDFBOX-4393: Test whether /Helv and /ZaDb get
   * added, but only if they don't exist.
   */
  @Test
  public void testAcroFormDefaultFonts() throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (PDDocument doc = new PDDocument()) {
      final PDPage page = new PDPage(PDRectangle.A4);
      doc.addPage(page);
      PDAcroForm acroForm2 = new PDAcroForm(doc);
      doc.getDocumentCatalog().setAcroForm(acroForm2);
      PDResources defaultResources = acroForm2.getDefaultResources();
      Assert.assertNull(defaultResources);
      defaultResources = new PDResources();
      acroForm2.setDefaultResources(defaultResources);
      Assert.assertNull(defaultResources.getFont(COSName.HELV));
      Assert.assertNull(defaultResources.getFont(COSName.ZA_DB));

      // getting AcroForm sets the two fonts
      acroForm2 = doc.getDocumentCatalog().getAcroForm();
      defaultResources = acroForm2.getDefaultResources();
      Assert.assertNotNull(defaultResources.getFont(COSName.HELV));
      Assert.assertNotNull(defaultResources.getFont(COSName.ZA_DB));

      // repeat with a new AcroForm (to delete AcroForm cache) and thus missing /DR
      doc.getDocumentCatalog().setAcroForm(new PDAcroForm(doc));
      acroForm2 = doc.getDocumentCatalog().getAcroForm();
      defaultResources = acroForm2.getDefaultResources();
      final PDFont helv = defaultResources.getFont(COSName.HELV);
      final PDFont zadb = defaultResources.getFont(COSName.ZA_DB);
      Assert.assertNotNull(helv);
      Assert.assertNotNull(zadb);
      doc.save(baos);
    }
    try (PDDocument doc = PDDocument.load(baos.toByteArray())) {
      final PDAcroForm acroForm2 = doc.getDocumentCatalog().getAcroForm();
      final PDResources defaultResources = acroForm2.getDefaultResources();
      final PDFont helv = defaultResources.getFont(COSName.HELV);
      final PDFont zadb = defaultResources.getFont(COSName.ZA_DB);
      Assert.assertNotNull(helv);
      Assert.assertNotNull(zadb);
      // make sure that font wasn't overwritten
      Assert.assertNotEquals(PDType1Font.HELVETICA, helv);
      Assert.assertNotEquals(PDType1Font.ZAPF_DINGBATS, zadb);
    }
  }

  @After
  public void tearDown() throws IOException {
    document.close();
  }

  private byte[] createAcroFormWithMissingResourceInformation() throws IOException {
    try (PDDocument tmpDocument = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      final PDPage page = new PDPage();
      tmpDocument.addPage(page);

      final PDAcroForm newAcroForm = new PDAcroForm(document);
      tmpDocument.getDocumentCatalog().setAcroForm(newAcroForm);

      final PDTextField textBox = new PDTextField(newAcroForm);
      textBox.setPartialName("SampleField");
      newAcroForm.getFields().add(textBox);

      final PDAnnotationWidget widget = textBox.getWidgets().get(0);
      final PDRectangle rect = new PDRectangle(50, 750, 200, 20);
      widget.setRectangle(rect);
      widget.setPage(page);

      page.getAnnotations().add(widget);

      // acroForm.setNeedAppearances(true);
      // acroForm.getField("SampleField").getCOSObject().setString(COSName.V,
      // "content");

      tmpDocument.save(baos); // this is a working PDF
      tmpDocument.close();
      return baos.toByteArray();
    }
  }

  private int countWidgets(final PDDocument documentToTest) {
    int count = 0;
    for (final PDPage page : documentToTest.getPages()) {
      try {
        for (final PDAnnotation annotation : page.getAnnotations()) {
          if (annotation instanceof PDAnnotationWidget) {
            count++;
          }
        }
      } catch (final IOException e) {
        // ignoring
      }
    }
    return count;
  }
}
