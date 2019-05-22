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
package org.apache.pdfbox.pdmodel.graphics.optionalcontent;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.PageMode;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties.BaseState;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.Assert;

import junit.framework.TestCase;

/**
 * Tests optional content group functionality (also called layers).
 */
public class TestOptionalContentGroups extends TestCase {
  private final File testResultsDir = new File("target/test-output");

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    testResultsDir.mkdirs();
  }

  /**
   * Tests OCG generation.
   * 
   * @throws Exception if an error occurs
   */
  public void testOCGGeneration() throws Exception {
    final PDDocument doc = new PDDocument();
    try {
      // Create new page
      final PDPage page = new PDPage();
      doc.addPage(page);
      PDResources resources = page.getResources();
      if (resources == null) {
        resources = new PDResources();
        page.setResources(resources);
      }

      // Prepare OCG functionality
      final PDOptionalContentProperties ocprops = new PDOptionalContentProperties();
      doc.getDocumentCatalog().setOCProperties(ocprops);
      // ocprops.setBaseState(BaseState.ON); //ON=default

      // Create OCG for background
      final PDOptionalContentGroup background = new PDOptionalContentGroup("background");
      ocprops.addGroup(background);
      TestCase.assertTrue(ocprops.isGroupEnabled("background"));

      // Create OCG for enabled
      final PDOptionalContentGroup enabled = new PDOptionalContentGroup("enabled");
      ocprops.addGroup(enabled);
      TestCase.assertFalse(ocprops.setGroupEnabled("enabled", true));
      TestCase.assertTrue(ocprops.isGroupEnabled("enabled"));

      // Create OCG for disabled
      final PDOptionalContentGroup disabled = new PDOptionalContentGroup("disabled");
      ocprops.addGroup(disabled);
      TestCase.assertFalse(ocprops.setGroupEnabled("disabled", true));
      TestCase.assertTrue(ocprops.isGroupEnabled("disabled"));
      TestCase.assertTrue(ocprops.setGroupEnabled("disabled", false));
      TestCase.assertFalse(ocprops.isGroupEnabled("disabled"));

      // Setup page content stream and paint background/title
      final PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.OVERWRITE, false);
      PDFont font = PDType1Font.HELVETICA_BOLD;
      contentStream.beginMarkedContent(COSName.OC, background);
      contentStream.beginText();
      contentStream.setFont(font, 14);
      contentStream.newLineAtOffset(80, 700);
      contentStream.showText("PDF 1.5: Optional Content Groups");
      contentStream.endText();
      font = PDType1Font.HELVETICA;
      contentStream.beginText();
      contentStream.setFont(font, 12);
      contentStream.newLineAtOffset(80, 680);
      contentStream.showText("You should see a green textline, but no red text line.");
      contentStream.endText();
      contentStream.endMarkedContent();

      // Paint enabled layer
      contentStream.beginMarkedContent(COSName.OC, enabled);
      contentStream.setNonStrokingColor(Color.GREEN);
      contentStream.beginText();
      contentStream.setFont(font, 12);
      contentStream.newLineAtOffset(80, 600);
      contentStream.showText("This is from an enabled layer. If you see this, that's good.");
      contentStream.endText();
      contentStream.endMarkedContent();

      // Paint disabled layer
      contentStream.beginMarkedContent(COSName.OC, disabled);
      contentStream.setNonStrokingColor(Color.RED);
      contentStream.beginText();
      contentStream.setFont(font, 12);
      contentStream.newLineAtOffset(80, 500);
      contentStream.showText("This is from a disabled layer. If you see this, that's NOT good!");
      contentStream.endText();
      contentStream.endMarkedContent();

      contentStream.close();

      final File targetFile = new File(testResultsDir, "ocg-generation.pdf");
      doc.save(targetFile.getAbsolutePath());
    } finally {
      doc.close();
    }
  }

  /**
   * Tests OCG functions on a loaded PDF.
   * 
   * @throws Exception if an error occurs
   */
  public void testOCGConsumption() throws Exception {
    final File pdfFile = new File(testResultsDir, "ocg-generation.pdf");
    if (!pdfFile.exists()) {
      testOCGGeneration();
    }

    final PDDocument doc = PDDocument.load(pdfFile);
    try {
      TestCase.assertEquals(1.5f, doc.getVersion());
      final PDDocumentCatalog catalog = doc.getDocumentCatalog();

      final PDPage page = doc.getPage(0);
      final PDResources resources = page.getResources();

      final COSName mc0 = COSName.getPDFName("oc1");
      final PDOptionalContentGroup ocg = (PDOptionalContentGroup) resources.getProperties(mc0);
      TestCase.assertNotNull(ocg);
      TestCase.assertEquals("background", ocg.getName());

      TestCase.assertNull(resources.getProperties(COSName.getPDFName("inexistent")));

      final PDOptionalContentProperties ocgs = catalog.getOCProperties();
      TestCase.assertEquals(BaseState.ON, ocgs.getBaseState());
      final Set<String> names = new java.util.HashSet<>(Arrays.asList(ocgs.getGroupNames()));
      TestCase.assertEquals(3, names.size());
      TestCase.assertTrue(names.contains("background"));

      TestCase.assertTrue(ocgs.isGroupEnabled("background"));
      TestCase.assertTrue(ocgs.isGroupEnabled("enabled"));
      TestCase.assertFalse(ocgs.isGroupEnabled("disabled"));

      ocgs.setGroupEnabled("background", false);
      TestCase.assertFalse(ocgs.isGroupEnabled("background"));

      final PDOptionalContentGroup background = ocgs.getGroup("background");
      TestCase.assertEquals(ocg.getName(), background.getName());
      TestCase.assertNull(ocgs.getGroup("inexistent"));

      final Collection<PDOptionalContentGroup> coll = ocgs.getOptionalContentGroups();
      TestCase.assertEquals(3, coll.size());
      final Set<String> nameSet = new HashSet<>();
      for (final PDOptionalContentGroup ocg2 : coll) {
        nameSet.add(ocg2.getName());
      }
      TestCase.assertTrue(nameSet.contains("background"));
      TestCase.assertTrue(nameSet.contains("enabled"));
      TestCase.assertTrue(nameSet.contains("disabled"));
    } finally {
      doc.close();
    }
  }

  public void testOCGsWithSameNameCanHaveDifferentVisibility() throws Exception {
    try (PDDocument doc = new PDDocument()) {
      // Create new page
      final PDPage page = new PDPage();
      doc.addPage(page);
      PDResources resources = page.getResources();
      if (resources == null) {
        resources = new PDResources();
        page.setResources(resources);
      }

      // Prepare OCG functionality
      final PDOptionalContentProperties ocprops = new PDOptionalContentProperties();
      doc.getDocumentCatalog().setOCProperties(ocprops);
      // ocprops.setBaseState(BaseState.ON); //ON=default

      // Create visible OCG
      final PDOptionalContentGroup visible = new PDOptionalContentGroup("layer");
      ocprops.addGroup(visible);
      TestCase.assertTrue(ocprops.isGroupEnabled(visible));

      // Create invisible OCG
      final PDOptionalContentGroup invisible = new PDOptionalContentGroup("layer");
      ocprops.addGroup(invisible);
      TestCase.assertFalse(ocprops.setGroupEnabled(invisible, false));
      TestCase.assertFalse(ocprops.isGroupEnabled(invisible));

      // Check that visible layer is still visible
      TestCase.assertTrue(ocprops.isGroupEnabled(visible));

      // Setup page content stream and paint background/title
      try (PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.OVERWRITE, false)) {
        PDFont font = PDType1Font.HELVETICA_BOLD;
        contentStream.beginMarkedContent(COSName.OC, visible);
        contentStream.beginText();
        contentStream.setFont(font, 14);
        contentStream.newLineAtOffset(80, 700);
        contentStream.showText("PDF 1.5: Optional Content Groups");
        contentStream.endText();
        font = PDType1Font.HELVETICA;
        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(80, 680);
        contentStream.showText("You should see this text, but no red text line.");
        contentStream.endText();
        contentStream.endMarkedContent();

        // Paint disabled layer
        contentStream.beginMarkedContent(COSName.OC, invisible);
        contentStream.setNonStrokingColor(Color.RED);
        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(80, 500);
        contentStream.showText("This is from a disabled layer. If you see this, that's NOT good!");
        contentStream.endText();
        contentStream.endMarkedContent();
      }

      final File targetFile = new File(testResultsDir, "ocg-generation-same-name.pdf");
      doc.save(targetFile.getAbsolutePath());
    }
  }

  /**
   * PDFBOX-4496: setGroupEnabled(String, boolean) must catch all OCGs of a name
   * even when several names are identical.
   *
   * @throws IOException
   */
  public void testOCGGenerationSameNameCanHaveSameVisibilityOff() throws IOException {
    BufferedImage expectedImage;
    BufferedImage actualImage;

    try (PDDocument doc = new PDDocument()) {
      // Create new page
      final PDPage page = new PDPage();
      doc.addPage(page);
      PDResources resources = page.getResources();
      if (resources == null) {
        resources = new PDResources();
        page.setResources(resources);
      }

      // Prepare OCG functionality
      final PDOptionalContentProperties ocprops = new PDOptionalContentProperties();
      doc.getDocumentCatalog().setOCProperties(ocprops);
      // ocprops.setBaseState(BaseState.ON); //ON=default

      // Create OCG for background
      final PDOptionalContentGroup background = new PDOptionalContentGroup("background");
      ocprops.addGroup(background);
      TestCase.assertTrue(ocprops.isGroupEnabled("background"));

      // Create OCG for enabled
      final PDOptionalContentGroup enabled = new PDOptionalContentGroup("science");
      ocprops.addGroup(enabled);
      TestCase.assertFalse(ocprops.setGroupEnabled("science", true));
      TestCase.assertTrue(ocprops.isGroupEnabled("science"));

      // Create OCG for disabled1
      final PDOptionalContentGroup disabled1 = new PDOptionalContentGroup("alternative");
      ocprops.addGroup(disabled1);

      // Create OCG for disabled2 with same name as disabled1
      final PDOptionalContentGroup disabled2 = new PDOptionalContentGroup("alternative");
      ocprops.addGroup(disabled2);

      TestCase.assertFalse(ocprops.setGroupEnabled("alternative", false));
      TestCase.assertFalse(ocprops.isGroupEnabled("alternative"));

      // Setup page content stream and paint background/title
      try (PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.OVERWRITE, false)) {
        PDFont font = PDType1Font.HELVETICA_BOLD;
        contentStream.beginMarkedContent(COSName.OC, background);
        contentStream.beginText();
        contentStream.setFont(font, 14);
        contentStream.newLineAtOffset(80, 700);
        contentStream.showText("PDF 1.5: Optional Content Groups");
        contentStream.endText();
        contentStream.endMarkedContent();

        font = PDType1Font.HELVETICA;

        // Paint enabled layer
        contentStream.beginMarkedContent(COSName.OC, enabled);
        contentStream.setNonStrokingColor(Color.GREEN);
        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(80, 600);
        contentStream.showText("The earth is a sphere");
        contentStream.endText();
        contentStream.endMarkedContent();

        // Paint disabled layer1
        contentStream.beginMarkedContent(COSName.OC, disabled1);
        contentStream.setNonStrokingColor(Color.RED);
        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(80, 500);
        contentStream.showText("Alternative 1: The earth is a flat circle");
        contentStream.endText();
        contentStream.endMarkedContent();

        // Paint disabled layer2
        contentStream.beginMarkedContent(COSName.OC, disabled2);
        contentStream.setNonStrokingColor(Color.BLUE);
        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(80, 450);
        contentStream.showText("Alternative 2: The earth is a flat parallelogram");
        contentStream.endText();
        contentStream.endMarkedContent();
      }

      doc.getDocumentCatalog().setPageMode(PageMode.USE_OPTIONAL_CONTENT);

      final File targetFile = new File(testResultsDir, "ocg-generation-same-name-off.pdf");
      doc.save(targetFile.getAbsolutePath());
    }

    // create PDF without OCGs to created expected rendering
    try (PDDocument doc = new PDDocument()) {
      final PDPage page = new PDPage();
      doc.addPage(page);
      PDResources resources = page.getResources();
      if (resources == null) {
        resources = new PDResources();
        page.setResources(resources);
      }

      try (PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.OVERWRITE, false)) {
        final PDFont font = PDType1Font.HELVETICA;

        contentStream.setNonStrokingColor(Color.RED);
        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(80, 500);
        contentStream.showText("Alternative 1: The earth is a flat circle");
        contentStream.endText();

        contentStream.setNonStrokingColor(Color.BLUE);
        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(80, 450);
        contentStream.showText("Alternative 2: The earth is a flat parallelogram");
        contentStream.endText();
      }

      expectedImage = new PDFRenderer(doc).renderImage(0, 2);
      ImageIO.write(expectedImage, "png", new File(testResultsDir, "ocg-generation-same-name-off-expected.png"));
    }

    // render PDF with science disabled and alternatives with same name enabled
    try (PDDocument doc = PDDocument.load(new File(testResultsDir, "ocg-generation-same-name-off.pdf"))) {
      doc.getDocumentCatalog().getOCProperties().setGroupEnabled("background", false);
      doc.getDocumentCatalog().getOCProperties().setGroupEnabled("science", false);
      doc.getDocumentCatalog().getOCProperties().setGroupEnabled("alternative", true);
      actualImage = new PDFRenderer(doc).renderImage(0, 2);
      ImageIO.write(actualImage, "png", new File(testResultsDir, "ocg-generation-same-name-off-actual.png"));
    }

    // compare images
    final DataBufferInt expectedData = (DataBufferInt) expectedImage.getRaster().getDataBuffer();
    final DataBufferInt actualData = (DataBufferInt) actualImage.getRaster().getDataBuffer();
    Assert.assertArrayEquals(expectedData.getData(), actualData.getData());
  }
}
