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
package org.apache.pdfbox.pdmodel.interactive.annotation;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.util.Matrix;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for the PDAnnotation classes.
 *
 */
public class PDSquareAnnotationTest {

  // delta for comparing equality of float values
  private static final double DELTA = 1e-4;

  // the location of the annotation
  static PDRectangle rectangle;

  private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/pdmodel/interactive/annotation");
  private static final String NAME_OF_PDF = "PDSquareAnnotationTest.pdf";

  @Before
  public void setUp() throws IOException {
    PDSquareAnnotationTest.rectangle = new PDRectangle();
    PDSquareAnnotationTest.rectangle.setLowerLeftX(91.5958f);
    PDSquareAnnotationTest.rectangle.setLowerLeftY(741.91f);
    PDSquareAnnotationTest.rectangle.setUpperRightX(113.849f);
    PDSquareAnnotationTest.rectangle.setUpperRightY(757.078f);
  }

  @Test
  public void createDefaultSquareAnnotation() {
    final PDAnnotation annotation = new PDAnnotationSquare();
    Assert.assertEquals(COSName.ANNOT, annotation.getCOSObject().getItem(COSName.TYPE));
    Assert.assertEquals(PDAnnotationSquare.SUB_TYPE, annotation.getCOSObject().getNameAsString(COSName.SUBTYPE));
  }

  @Test
  public void createWithAppearance() throws IOException {
    // the width of the <nnotations border
    final int borderWidth = 1;

    try (PDDocument document = new PDDocument()) {
      final PDPage page = new PDPage();
      document.addPage(page);
      final List<PDAnnotation> annotations = page.getAnnotations();

      final PDAnnotationSquareCircle annotation = new PDAnnotationSquare();

      final PDBorderStyleDictionary borderThin = new PDBorderStyleDictionary();
      borderThin.setWidth(borderWidth);

      final PDColor red = new PDColor(new float[] { 1, 0, 0 }, PDDeviceRGB.INSTANCE);
      annotation.setContents("Square Annotation");
      annotation.setColor(red);
      annotation.setBorderStyle(borderThin);

      annotation.setRectangle(PDSquareAnnotationTest.rectangle);

      annotation.constructAppearances();
      annotations.add(annotation);
    }
  }

  @Test
  public void validateAppearance() throws IOException {
    // the width of the <nnotations border
    final int borderWidth = 1;

    final File file = new File(PDSquareAnnotationTest.IN_DIR, PDSquareAnnotationTest.NAME_OF_PDF);
    try (PDDocument document = PDDocument.load(file)) {
      final PDPage page = document.getPage(0);
      final List<PDAnnotation> annotations = page.getAnnotations();

      final PDAnnotationSquareCircle annotation = (PDAnnotationSquareCircle) annotations.get(0);

      // test the correct setting of the appearance stream
      Assert.assertNotNull("Appearance dictionary shall not be null", annotation.getAppearance());
      Assert.assertNotNull("Normal appearance shall not be null", annotation.getAppearance().getNormalAppearance());
      final PDAppearanceStream appearanceStream = annotation.getAppearance().getNormalAppearance()
          .getAppearanceStream();
      Assert.assertNotNull("Appearance stream shall not be null", appearanceStream);
      Assert.assertEquals(PDSquareAnnotationTest.rectangle.getLowerLeftX(), appearanceStream.getBBox().getLowerLeftX(),
          PDSquareAnnotationTest.DELTA);
      Assert.assertEquals(PDSquareAnnotationTest.rectangle.getLowerLeftY(), appearanceStream.getBBox().getLowerLeftY(),
          PDSquareAnnotationTest.DELTA);
      Assert.assertEquals(PDSquareAnnotationTest.rectangle.getWidth(), appearanceStream.getBBox().getWidth(),
          PDSquareAnnotationTest.DELTA);
      Assert.assertEquals(PDSquareAnnotationTest.rectangle.getHeight(), appearanceStream.getBBox().getHeight(),
          PDSquareAnnotationTest.DELTA);

      final Matrix matrix = appearanceStream.getMatrix();
      Assert.assertNotNull("Matrix shall not be null", matrix);

      // should have been translated to a 0 origin
      Assert.assertEquals(-PDSquareAnnotationTest.rectangle.getLowerLeftX(), matrix.getTranslateX(),
          PDSquareAnnotationTest.DELTA);
      Assert.assertEquals(-PDSquareAnnotationTest.rectangle.getLowerLeftY(), matrix.getTranslateY(),
          PDSquareAnnotationTest.DELTA);

      // test the content of the appearance stream
      final PDStream contentStream = appearanceStream.getContentStream();
      Assert.assertNotNull("Content stream shall not be null", contentStream);
      final PDFStreamParser parser = new PDFStreamParser(appearanceStream.getContents());
      parser.parse();
      final List<Object> tokens = parser.getTokens();

      // the samples content stream should contain 10 tokens
      Assert.assertEquals(10, tokens.size());

      // setting of the stroking color
      Assert.assertEquals(1, ((COSInteger) tokens.get(0)).intValue());
      Assert.assertEquals(0, ((COSInteger) tokens.get(1)).intValue());
      Assert.assertEquals(0, ((COSInteger) tokens.get(2)).intValue());
      Assert.assertEquals("RG", ((Operator) tokens.get(3)).getName());

      // setting of the rectangle for the border
      // it shall be inset by the border width
      Assert.assertEquals(PDSquareAnnotationTest.rectangle.getLowerLeftX() + borderWidth,
          ((COSFloat) tokens.get(4)).floatValue(), PDSquareAnnotationTest.DELTA);
      Assert.assertEquals(PDSquareAnnotationTest.rectangle.getLowerLeftY() + borderWidth,
          ((COSFloat) tokens.get(5)).floatValue(), PDSquareAnnotationTest.DELTA);
      Assert.assertEquals(PDSquareAnnotationTest.rectangle.getWidth() - 2 * borderWidth,
          ((COSFloat) tokens.get(6)).floatValue(), PDSquareAnnotationTest.DELTA);
      Assert.assertEquals(PDSquareAnnotationTest.rectangle.getHeight() - 2 * borderWidth,
          ((COSFloat) tokens.get(7)).floatValue(), PDSquareAnnotationTest.DELTA);
      Assert.assertEquals("re", ((Operator) tokens.get(8)).getName());
      Assert.assertEquals("S", ((Operator) tokens.get(9)).getName());
    }
  }
}
