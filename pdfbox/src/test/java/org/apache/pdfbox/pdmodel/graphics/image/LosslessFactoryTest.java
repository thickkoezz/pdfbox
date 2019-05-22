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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.rendering.PDFRenderer;

import junit.framework.TestCase;

/**
 * Unit tests for LosslessFactory
 *
 * @author Tilman Hausherr
 */
public class LosslessFactoryTest extends TestCase {
  private final File testResultsDir = new File("target/test-output/graphics");

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    testResultsDir.mkdirs();
  }

  /**
   * Tests RGB LosslessFactoryTest#createFromImage(PDDocument document,
   * BufferedImage image)
   *
   * @throws java.io.IOException
   */
  public void testCreateLosslessFromImageRGB() throws IOException {
    PDDocument document = new PDDocument();
    final BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("png.png"));

    final PDImageXObject ximage1 = LosslessFactory.createFromImage(document, image);
    ValidateXImage.validate(ximage1, 8, image.getWidth(), image.getHeight(), "png", PDDeviceRGB.INSTANCE.getName());
    ValidateXImage.checkIdent(image, ximage1.getImage());

    // Create a grayscale image
    final BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(),
        BufferedImage.TYPE_BYTE_GRAY);
    Graphics g = grayImage.getGraphics();
    g.drawImage(image, 0, 0, null);
    g.dispose();
    final PDImageXObject ximage2 = LosslessFactory.createFromImage(document, grayImage);
    ValidateXImage.validate(ximage2, 8, grayImage.getWidth(), grayImage.getHeight(), "png",
        PDDeviceGray.INSTANCE.getName());
    ValidateXImage.checkIdent(grayImage, ximage2.getImage());

    // Create a bitonal image
    final BufferedImage bitonalImage = new BufferedImage(image.getWidth(), image.getHeight(),
        BufferedImage.TYPE_BYTE_BINARY);

    // avoid multiple of 8 to test padding
    TestCase.assertFalse(bitonalImage.getWidth() % 8 == 0);

    g = bitonalImage.getGraphics();
    g.drawImage(image, 0, 0, null);
    g.dispose();
    final PDImageXObject ximage3 = LosslessFactory.createFromImage(document, bitonalImage);
    ValidateXImage.validate(ximage3, 1, bitonalImage.getWidth(), bitonalImage.getHeight(), "png",
        PDDeviceGray.INSTANCE.getName());
    ValidateXImage.checkIdent(bitonalImage, ximage3.getImage());

    // This part isn't really needed because this test doesn't break
    // if the mask has the wrong colorspace (PDFBOX-2057), but it is still useful
    // if something goes wrong in the future and we want to have a PDF to open.
    final PDPage page = new PDPage();
    document.addPage(page);
    final PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, false);
    contentStream.drawImage(ximage1, 200, 300, ximage1.getWidth() / 2, ximage1.getHeight() / 2);
    contentStream.drawImage(ximage2, 200, 450, ximage2.getWidth() / 2, ximage2.getHeight() / 2);
    contentStream.drawImage(ximage3, 200, 600, ximage3.getWidth() / 2, ximage3.getHeight() / 2);
    contentStream.close();

    final File pdfFile = new File(testResultsDir, "misc.pdf");
    document.save(pdfFile);
    document.close();

    document = PDDocument.load(pdfFile, (String) null);
    new PDFRenderer(document).renderImage(0);
    document.close();
  }

  /**
   * Tests INT_ARGB LosslessFactoryTest#createFromImage(PDDocument document,
   * BufferedImage image)
   *
   * @throws java.io.IOException
   */
  public void testCreateLosslessFromImageINT_ARGB() throws IOException {
    final PDDocument document = new PDDocument();
    final BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("png.png"));

    // create an ARGB image
    final int w = image.getWidth();
    final int h = image.getHeight();
    final BufferedImage argbImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    final Graphics ag = argbImage.getGraphics();
    ag.drawImage(image, 0, 0, null);
    ag.dispose();

    for (int x = 0; x < argbImage.getWidth(); ++x) {
      for (int y = 0; y < argbImage.getHeight(); ++y) {
        argbImage.setRGB(x, y, argbImage.getRGB(x, y) & 0xFFFFFF | y / 10 * 10 << 24);
      }
    }

    final PDImageXObject ximage = LosslessFactory.createFromImage(document, argbImage);
    ValidateXImage.validate(ximage, 8, argbImage.getWidth(), argbImage.getHeight(), "png",
        PDDeviceRGB.INSTANCE.getName());
    ValidateXImage.checkIdent(argbImage, ximage.getImage());
    checkIdentRGB(argbImage, ximage.getOpaqueImage());

    TestCase.assertNotNull(ximage.getSoftMask());
    ValidateXImage.validate(ximage.getSoftMask(), 8, argbImage.getWidth(), argbImage.getHeight(), "png",
        PDDeviceGray.INSTANCE.getName());
    TestCase.assertTrue(ValidateXImage.colorCount(ximage.getSoftMask().getImage()) > image.getHeight() / 10);

    ValidateXImage.doWritePDF(document, ximage, testResultsDir, "intargb.pdf");
  }

  /**
   * Tests INT_ARGB LosslessFactoryTest#createFromImage(PDDocument document,
   * BufferedImage image) with BITMASK transparency
   *
   * @throws java.io.IOException
   */
  public void testCreateLosslessFromImageBITMASK_INT_ARGB() throws IOException {
    doBitmaskTransparencyTest(BufferedImage.TYPE_INT_ARGB, "bitmaskintargb.pdf");
  }

  /**
   * Tests 4BYTE_ABGR LosslessFactoryTest#createFromImage(PDDocument document,
   * BufferedImage image) with BITMASK transparency
   *
   * @throws java.io.IOException
   */
  public void testCreateLosslessFromImageBITMASK4BYTE_ABGR() throws IOException {
    doBitmaskTransparencyTest(BufferedImage.TYPE_4BYTE_ABGR, "bitmask4babgr.pdf");
  }

  /**
   * Tests 4BYTE_ABGR LosslessFactoryTest#createFromImage(PDDocument document,
   * BufferedImage image)
   *
   * @throws java.io.IOException
   */
  public void testCreateLosslessFromImage4BYTE_ABGR() throws IOException {
    final PDDocument document = new PDDocument();
    final BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("png.png"));

    // create an ARGB image
    int w = image.getWidth();
    int h = image.getHeight();
    BufferedImage argbImage = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
    final Graphics ag = argbImage.getGraphics();
    ag.drawImage(image, 0, 0, null);
    ag.dispose();

    for (int x = 0; x < argbImage.getWidth(); ++x) {
      for (int y = 0; y < argbImage.getHeight(); ++y) {
        argbImage.setRGB(x, y, argbImage.getRGB(x, y) & 0xFFFFFF | y / 10 * 10 << 24);
      }
    }

    // extra for PDFBOX-3181: check for exception due to different sizes of
    // alphaRaster.getSampleModel().getWidth()
    // and
    // alphaRaster.getWidth()
    // happens with image returned by BufferedImage.getSubimage()
    argbImage = argbImage.getSubimage(1, 1, argbImage.getWidth() - 2, argbImage.getHeight() - 2);
    w -= 2;
    h -= 2;

    final PDImageXObject ximage = LosslessFactory.createFromImage(document, argbImage);

    ValidateXImage.validate(ximage, 8, w, h, "png", PDDeviceRGB.INSTANCE.getName());
    ValidateXImage.checkIdent(argbImage, ximage.getImage());
    checkIdentRGB(argbImage, ximage.getOpaqueImage());

    TestCase.assertNotNull(ximage.getSoftMask());
    ValidateXImage.validate(ximage.getSoftMask(), 8, w, h, "png", PDDeviceGray.INSTANCE.getName());
    TestCase.assertTrue(ValidateXImage.colorCount(ximage.getSoftMask().getImage()) > image.getHeight() / 10);

    ValidateXImage.doWritePDF(document, ximage, testResultsDir, "4babgr.pdf");
  }

  /**
   * Tests LosslessFactoryTest#createFromImage(PDDocument document, BufferedImage
   * image) with transparent GIF
   *
   * @throws java.io.IOException
   */
  public void testCreateLosslessFromTransparentGIF() throws IOException {
    final PDDocument document = new PDDocument();
    final BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("gif.gif"));

    TestCase.assertEquals(Transparency.BITMASK, image.getColorModel().getTransparency());

    final PDImageXObject ximage = LosslessFactory.createFromImage(document, image);

    final int w = image.getWidth();
    final int h = image.getHeight();
    ValidateXImage.validate(ximage, 8, w, h, "png", PDDeviceRGB.INSTANCE.getName());
    ValidateXImage.checkIdent(image, ximage.getImage());
    checkIdentRGB(image, ximage.getOpaqueImage());

    TestCase.assertNotNull(ximage.getSoftMask());
    ValidateXImage.validate(ximage.getSoftMask(), 1, w, h, "png", PDDeviceGray.INSTANCE.getName());
    TestCase.assertEquals(2, ValidateXImage.colorCount(ximage.getSoftMask().getImage()));

    ValidateXImage.doWritePDF(document, ximage, testResultsDir, "gif.pdf");
  }

  /**
   * Test file that had a predictor encoding bug in PDFBOX-4184.
   *
   * @throws java.io.IOException
   */
  public void testCreateLosslessFromGovdocs032163() throws IOException {
    final PDDocument document = new PDDocument();
    final BufferedImage image = ImageIO.read(new File("target/imgs", "PDFBOX-4184-032163.jpg"));
    final PDImageXObject ximage = LosslessFactory.createFromImage(document, image);
    ValidateXImage.validate(ximage, 8, image.getWidth(), image.getHeight(), "png", PDDeviceRGB.INSTANCE.getName());
    ValidateXImage.checkIdent(image, ximage.getImage());

    ValidateXImage.doWritePDF(document, ximage, testResultsDir, "PDFBOX-4184-032163.pdf");
  }

  /**
   * Check whether the RGB part of images are identical.
   *
   * @param expectedImage
   * @param actualImage
   */
  private void checkIdentRGB(final BufferedImage expectedImage, final BufferedImage actualImage) {
    String errMsg = "";

    final int w = expectedImage.getWidth();
    final int h = expectedImage.getHeight();
    TestCase.assertEquals(w, actualImage.getWidth());
    TestCase.assertEquals(h, actualImage.getHeight());
    for (int y = 0; y < h; ++y) {
      for (int x = 0; x < w; ++x) {
        if ((expectedImage.getRGB(x, y) & 0xFFFFFF) != (actualImage.getRGB(x, y) & 0xFFFFFF)) {
          errMsg = String.format("(%d,%d) %06X != %06X", x, y, expectedImage.getRGB(x, y) & 0xFFFFFF,
              actualImage.getRGB(x, y) & 0xFFFFFF);
        }
        TestCase.assertEquals(errMsg, expectedImage.getRGB(x, y) & 0xFFFFFF, actualImage.getRGB(x, y) & 0xFFFFFF);
      }
    }
  }

  private void doBitmaskTransparencyTest(final int imageType, final String pdfFilename) throws IOException {
    PDDocument document = new PDDocument();

    final int width = 257;
    final int height = 256;

    // create an ARGB image
    BufferedImage argbImage = new BufferedImage(width, height, imageType);

    // from there, create an image with Transparency.BITMASK
    Graphics2D g = argbImage.createGraphics();
    final GraphicsConfiguration gc = g.getDeviceConfiguration();
    argbImage = gc.createCompatibleImage(width, height, Transparency.BITMASK);
    g.dispose();
    // create a red rectangle
    g = argbImage.createGraphics();
    g.setColor(Color.red);
    g.fillRect(0, 0, width, height);
    g.dispose();

    final Random random = new Random();
    random.setSeed(12345);
    // create a transparency cross: only pixels in the
    // interval max/2 - max/8 ... max/2 + max/8 will be visible
    final int startX = width / 2 - width / 8;
    final int endX = width / 2 + width / 8;
    final int startY = height / 2 - height / 8;
    final int endY = height / 2 + height / 8;
    for (int x = 0; x < width; ++x) {
      for (int y = 0; y < height; ++y) {
        // create pseudorandom alpha values, but those within the cross
        // must be >= 128 and those outside must be < 128
        int alpha;
        if (x >= startX && x <= endX || y >= startY && y <= endY) {
          alpha = 128 + (int) (random.nextFloat() * 127);
          TestCase.assertTrue(alpha >= 128);
          argbImage.setRGB(x, y, argbImage.getRGB(x, y) & 0xFFFFFF | alpha << 24);
          TestCase.assertEquals(255, argbImage.getRGB(x, y) >>> 24);
        } else {
          alpha = (int) (random.nextFloat() * 127);
          TestCase.assertTrue(alpha < 128);
          argbImage.setRGB(x, y, argbImage.getRGB(x, y) & 0xFFFFFF | alpha << 24);
          TestCase.assertEquals(0, argbImage.getRGB(x, y) >>> 24);
        }
      }
    }

    final PDImageXObject ximage = LosslessFactory.createFromImage(document, argbImage);
    ValidateXImage.validate(ximage, 8, width, height, "png", PDDeviceRGB.INSTANCE.getName());
    ValidateXImage.checkIdent(argbImage, ximage.getImage());
    checkIdentRGB(argbImage, ximage.getOpaqueImage());

    TestCase.assertNotNull(ximage.getSoftMask());
    ValidateXImage.validate(ximage.getSoftMask(), 1, width, height, "png", PDDeviceGray.INSTANCE.getName());
    TestCase.assertEquals(2, ValidateXImage.colorCount(ximage.getSoftMask().getImage()));

    // check whether the mask is a b/w cross
    final BufferedImage maskImage = ximage.getSoftMask().getImage();

    // avoid multiple of 8 to test padding
    TestCase.assertFalse(maskImage.getWidth() % 8 == 0);

    TestCase.assertEquals(Transparency.OPAQUE, maskImage.getTransparency());
    for (int x = 0; x < width; ++x) {
      for (int y = 0; y < height; ++y) {
        if (x >= startX && x <= endX || y >= startY && y <= endY) {
          TestCase.assertEquals(0xFFFFFF, maskImage.getRGB(x, y) & 0xFFFFFF);
        } else {
          TestCase.assertEquals(0, maskImage.getRGB(x, y) & 0xFFFFFF);
        }
      }
    }

    // This part isn't really needed because this test doesn't break
    // if the mask has the wrong colorspace (PDFBOX-2057), but it is still useful
    // if something goes wrong in the future and we want to have a PDF to open.
    // Create a rectangle
    final BufferedImage rectImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    g = rectImage.createGraphics();
    g.setColor(Color.blue);
    g.fillRect(0, 0, width, height);
    g.dispose();
    final PDImageXObject ximage2 = LosslessFactory.createFromImage(document, rectImage);

    final PDPage page = new PDPage();
    document.addPage(page);
    final PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, false);
    contentStream.drawImage(ximage2, 150, 300, ximage2.getWidth(), ximage2.getHeight());
    contentStream.drawImage(ximage, 150, 300, ximage.getWidth(), ximage.getHeight());
    contentStream.close();
    final File pdfFile = new File(testResultsDir, pdfFilename);
    document.save(pdfFile);
    document.close();
    document = PDDocument.load(pdfFile, (String) null);
    new PDFRenderer(document).renderImage(0);
    document.close();
  }

  /**
   * Test lossless encoding of CMYK images
   */
  public void testCreateLosslessFromImageCMYK() throws IOException {
    final PDDocument document = new PDDocument();
    final BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("png.png"));

    final ColorSpace targetCS = new ICC_ColorSpace(ICC_Profile
        .getInstance(this.getClass().getResourceAsStream("/org/apache/pdfbox/resources/icc/ISOcoated_v2_300_bas.icc")));
    final ColorConvertOp op = new ColorConvertOp(image.getColorModel().getColorSpace(), targetCS, null);
    final BufferedImage imageCMYK = op.filter(image, null);

    final PDImageXObject ximage = LosslessFactory.createFromImage(document, imageCMYK);
    ValidateXImage.validate(ximage, 8, imageCMYK.getWidth(), imageCMYK.getHeight(), "png", "ICCBased");

    ValidateXImage.doWritePDF(document, ximage, testResultsDir, "cmyk.pdf");

    // still slight difference of 1 color level
    // checkIdent(imageCMYK, ximage.getImage());
  }

  public void testCreateLosslessFrom16Bit() throws IOException {
    final PDDocument document = new PDDocument();
    final BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("png.png"));

    final ColorSpace targetCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
    final int dataBufferType = DataBuffer.TYPE_USHORT;
    final ColorModel colorModel = new ComponentColorModel(targetCS, false, false, Transparency.OPAQUE, dataBufferType);
    final WritableRaster targetRaster = Raster.createInterleavedRaster(dataBufferType, image.getWidth(),
        image.getHeight(), targetCS.getNumComponents(), new Point(0, 0));
    final BufferedImage img16Bit = new BufferedImage(colorModel, targetRaster, false, new Hashtable());
    final ColorConvertOp op = new ColorConvertOp(image.getColorModel().getColorSpace(), targetCS, null);
    op.filter(image, img16Bit);

    final PDImageXObject ximage = LosslessFactory.createFromImage(document, img16Bit);
    ValidateXImage.validate(ximage, 16, img16Bit.getWidth(), img16Bit.getHeight(), "png",
        PDDeviceRGB.INSTANCE.getName());
    ValidateXImage.checkIdent(image, ximage.getImage());
    ValidateXImage.doWritePDF(document, ximage, testResultsDir, "misc-16bit.pdf");
  }

  public void testCreateLosslessFromImageINT_BGR() throws IOException {
    final PDDocument document = new PDDocument();
    final BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("png.png"));

    final BufferedImage imgBgr = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_BGR);
    final Graphics2D graphics = imgBgr.createGraphics();
    graphics.drawImage(image, 0, 0, null);

    final PDImageXObject ximage = LosslessFactory.createFromImage(document, imgBgr);
    ValidateXImage.validate(ximage, 8, imgBgr.getWidth(), imgBgr.getHeight(), "png", PDDeviceRGB.INSTANCE.getName());
    ValidateXImage.checkIdent(image, ximage.getImage());
  }

  public void testCreateLosslessFromImageINT_RGB() throws IOException {
    final PDDocument document = new PDDocument();
    final BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("png.png"));

    final BufferedImage imgRgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
    final Graphics2D graphics = imgRgb.createGraphics();
    graphics.drawImage(image, 0, 0, null);

    final PDImageXObject ximage = LosslessFactory.createFromImage(document, imgRgb);
    ValidateXImage.validate(ximage, 8, imgRgb.getWidth(), imgRgb.getHeight(), "png", PDDeviceRGB.INSTANCE.getName());
    ValidateXImage.checkIdent(image, ximage.getImage());
  }

  public void testCreateLosslessFromImageBYTE_3BGR() throws IOException {
    final PDDocument document = new PDDocument();
    final BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("png.png"));

    final BufferedImage imgRgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    final Graphics2D graphics = imgRgb.createGraphics();
    graphics.drawImage(image, 0, 0, null);

    final PDImageXObject ximage = LosslessFactory.createFromImage(document, imgRgb);
    ValidateXImage.validate(ximage, 8, imgRgb.getWidth(), imgRgb.getHeight(), "png", PDDeviceRGB.INSTANCE.getName());
    ValidateXImage.checkIdent(image, ximage.getImage());
  }

  public void testCreateLosslessFrom16BitPNG() throws IOException {
    final PDDocument document = new PDDocument();
    final BufferedImage image = ImageIO.read(new File("target/imgs", "PDFBOX-4184-16bit.png"));

    TestCase.assertEquals(64, image.getColorModel().getPixelSize());
    TestCase.assertEquals(Transparency.TRANSLUCENT, image.getColorModel().getTransparency());
    TestCase.assertEquals(4, image.getRaster().getNumDataElements());
    TestCase.assertEquals(java.awt.image.DataBuffer.TYPE_USHORT, image.getRaster().getDataBuffer().getDataType());

    final PDImageXObject ximage = LosslessFactory.createFromImage(document, image);

    final int w = image.getWidth();
    final int h = image.getHeight();
    ValidateXImage.validate(ximage, 16, w, h, "png", PDDeviceRGB.INSTANCE.getName());
    ValidateXImage.checkIdent(image, ximage.getImage());
    checkIdentRGB(image, ximage.getOpaqueImage());

    TestCase.assertNotNull(ximage.getSoftMask());
    ValidateXImage.validate(ximage.getSoftMask(), 16, w, h, "png", PDDeviceGray.INSTANCE.getName());
    TestCase.assertEquals(35, ValidateXImage.colorCount(ximage.getSoftMask().getImage()));

    ValidateXImage.doWritePDF(document, ximage, testResultsDir, "png16bit.pdf");
  }
}
