/*
 * Copyright 2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.graphics.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.rendering.PDFRenderer;

import junit.framework.TestCase;

/**
 * Helper class to do some validations for PDImageXObject.
 *
 * @author Tilman Hausherr
 */
public class ValidateXImage {
  public static void validate(final PDImageXObject ximage, final int bpc, final int width, final int height,
      final String format, final String colorSpaceName) throws IOException {
    // check the dictionary
    TestCase.assertNotNull(ximage);
    final COSStream cosStream = ximage.getCOSObject();
    TestCase.assertNotNull(cosStream);
    TestCase.assertEquals(COSName.XOBJECT, cosStream.getItem(COSName.TYPE));
    TestCase.assertEquals(COSName.IMAGE, cosStream.getItem(COSName.SUBTYPE));
    TestCase.assertTrue(ximage.getCOSObject().getLength() > 0);
    TestCase.assertEquals(bpc, ximage.getBitsPerComponent());
    TestCase.assertEquals(width, ximage.getWidth());
    TestCase.assertEquals(height, ximage.getHeight());
    TestCase.assertEquals(format, ximage.getSuffix());
    TestCase.assertEquals(colorSpaceName, ximage.getColorSpace().getName());

    // check the image
    TestCase.assertNotNull(ximage.getImage());
    TestCase.assertEquals(ximage.getWidth(), ximage.getImage().getWidth());
    TestCase.assertEquals(ximage.getHeight(), ximage.getImage().getHeight());

    boolean canEncode = true;
    boolean writeOk;
    // jdk11+ no longer encodes ARGB jpg
    // https://bugs.openjdk.java.net/browse/JDK-8211748
    if ("jpg".equals(format) && ximage.getImage().getType() == BufferedImage.TYPE_INT_ARGB) {
      final ImageWriter writer = ImageIO.getImageWritersBySuffix(format).next();
      final ImageWriterSpi originatingProvider = writer.getOriginatingProvider();
      canEncode = originatingProvider.canEncodeImage(ximage.getImage());
    }
    if (canEncode) {
      writeOk = ImageIO.write(ximage.getImage(), format, new NullOutputStream());
      TestCase.assertTrue(writeOk);
    }
    writeOk = ImageIO.write(ximage.getOpaqueImage(), format, new NullOutputStream());
    TestCase.assertTrue(writeOk);
  }

  private static class NullOutputStream extends OutputStream {
    @Override
    public void write(final int b) throws IOException {
    }
  }

  static int colorCount(final BufferedImage bim) {
    final Set<Integer> colors = new HashSet<>();
    final int w = bim.getWidth();
    final int h = bim.getHeight();
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        colors.add(bim.getRGB(x, y));
      }
    }
    return colors.size();
  }

  // write image twice (overlapped) in document, close document and re-read PDF
  static void doWritePDF(PDDocument document, final PDImageXObject ximage, final File testResultsDir,
      final String filename) throws IOException {
    final File pdfFile = new File(testResultsDir, filename);

    // This part isn't really needed because this test doesn't break
    // if the mask has the wrong colorspace (PDFBOX-2057), but it is still useful
    // if something goes wrong in the future and we want to have a PDF to open.

    final PDPage page = new PDPage();
    document.addPage(page);
    try (PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, false)) {
      contentStream.drawImage(ximage, 150, 300);
      contentStream.drawImage(ximage, 200, 350);
    }

    // check that the resource map is up-to-date
    TestCase.assertEquals(1, ValidateXImage.count(document.getPage(0).getResources().getXObjectNames()));

    document.save(pdfFile);
    document.close();

    document = PDDocument.load(pdfFile);
    TestCase.assertEquals(1, ValidateXImage.count(document.getPage(0).getResources().getXObjectNames()));
    new PDFRenderer(document).renderImage(0);
    document.close();
  }

  private static int count(final Iterable<COSName> iterable) {
    int count = 0;
    for (final COSName name : iterable) {
      count++;
    }
    return count;
  }

  /**
   * Check whether the images are identical.
   *
   * @param expectedImage
   * @param actualImage
   */
  public static void checkIdent(final BufferedImage expectedImage, final BufferedImage actualImage) {
    String errMsg = "";

    final int w = expectedImage.getWidth();
    final int h = expectedImage.getHeight();
    TestCase.assertEquals(w, actualImage.getWidth());
    TestCase.assertEquals(h, actualImage.getHeight());
    for (int y = 0; y < h; ++y) {
      for (int x = 0; x < w; ++x) {
        if (expectedImage.getRGB(x, y) != actualImage.getRGB(x, y)) {
          errMsg = String.format("(%d,%d) expected: <%08X> but was: <%08X>; ", x, y, expectedImage.getRGB(x, y),
              actualImage.getRGB(x, y));
        }
        TestCase.assertEquals(errMsg, expectedImage.getRGB(x, y), actualImage.getRGB(x, y));
      }
    }
  }
}
