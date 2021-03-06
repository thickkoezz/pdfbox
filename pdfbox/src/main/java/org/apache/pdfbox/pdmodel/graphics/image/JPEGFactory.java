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
package org.apache.pdfbox.pdmodel.graphics.image;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.filter.MissingImageReaderException;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.w3c.dom.Element;

/**
 * Factory for creating a PDImageXObject containing a JPEG compressed image.
 *
 * @author John Hewson
 */
public final class JPEGFactory {
  private JPEGFactory() {
  }

  /**
   * Creates a new JPEG Image XObject from an input stream containing JPEG data.
   *
   * The input stream data will be preserved and embedded in the PDF file without
   * modification.
   *
   * @param document the document where the image will be created
   * @param stream   a stream of JPEG data
   * @return a new Image XObject
   *
   * @throws IOException if the input stream cannot be read
   */
  public static PDImageXObject createFromStream(final PDDocument document, final InputStream stream)
      throws IOException {
    return JPEGFactory.createFromByteArray(document, IOUtils.toByteArray(stream));
  }

  /**
   * Creates a new JPEG Image XObject from a byte array containing JPEG data.
   *
   * @param document  the document where the image will be created
   * @param byteArray bytes of JPEG image
   * @return a new Image XObject
   *
   * @throws IOException if the input stream cannot be read
   */
  public static PDImageXObject createFromByteArray(final PDDocument document, final byte[] byteArray)
      throws IOException {
    // copy stream
    final ByteArrayInputStream byteStream = new ByteArrayInputStream(byteArray);

    // read image
    final Raster raster = JPEGFactory.readJPEGRaster(byteStream);
    byteStream.reset();

    PDColorSpace colorSpace;
    switch (raster.getNumDataElements()) {
    case 1:
      colorSpace = PDDeviceGray.INSTANCE;
      break;
    case 3:
      colorSpace = PDDeviceRGB.INSTANCE;
      break;
    case 4:
      colorSpace = PDDeviceCMYK.INSTANCE;
      break;
    default:
      throw new UnsupportedOperationException("number of data elements not supported: " + raster.getNumDataElements());
    }

    // create PDImageXObject from stream
    final PDImageXObject pdImage = new PDImageXObject(document, byteStream, COSName.DCT_DECODE, raster.getWidth(),
        raster.getHeight(), 8, colorSpace);

    if (colorSpace instanceof PDDeviceCMYK) {
      final COSArray decode = new COSArray();
      decode.add(COSInteger.ONE);
      decode.add(COSInteger.ZERO);
      decode.add(COSInteger.ONE);
      decode.add(COSInteger.ZERO);
      decode.add(COSInteger.ONE);
      decode.add(COSInteger.ZERO);
      decode.add(COSInteger.ONE);
      decode.add(COSInteger.ZERO);
      pdImage.setDecode(decode);
    }

    return pdImage;
  }

  private static Raster readJPEGRaster(final InputStream stream) throws IOException {
    // find suitable image reader
    final Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("JPEG");
    ImageReader reader = null;
    while (readers.hasNext()) {
      reader = readers.next();
      if (reader.canReadRaster()) {
        break;
      }
    }

    if (reader == null)
      throw new MissingImageReaderException("Cannot read JPEG image: a suitable JAI I/O image filter is not installed");

    try (ImageInputStream iis = ImageIO.createImageInputStream(stream)) {
      reader.setInput(iis);
      ImageIO.setUseCache(false);
      return reader.readRaster(0, null);
    } finally {
      reader.dispose();
    }
  }

  /**
   * Creates a new JPEG PDImageXObject from a BufferedImage.
   * <p>
   * Do not read a JPEG image from a stream/file and call this method; you'll get
   * more speed and quality by calling
   * {@link #createFromStream(org.apache.pdfbox.pdmodel.PDDocument, java.io.InputStream)
   * createFromStream()} instead.
   *
   * @param document the document where the image will be created
   * @param image    the BufferedImage to embed
   * @return a new Image XObject
   * @throws IOException if the JPEG data cannot be written
   */
  public static PDImageXObject createFromImage(final PDDocument document, final BufferedImage image)
      throws IOException {
    return JPEGFactory.createFromImage(document, image, 0.75f);
  }

  /**
   * Creates a new JPEG PDImageXObject from a BufferedImage and a given quality.
   * <p>
   * Do not read a JPEG image from a stream/file and call this method; you'll get
   * more speed and quality by calling
   * {@link #createFromStream(org.apache.pdfbox.pdmodel.PDDocument, java.io.InputStream)
   * createFromStream()} instead.
   *
   * The image will be created with a dpi value of 72 to be stored in metadata.
   *
   * @param document the document where the image will be created
   * @param image    the BufferedImage to embed
   * @param quality  The desired JPEG compression quality; between 0 (best
   *                 compression) and 1 (best image quality). See
   *                 {@link ImageWriteParam#setCompressionQuality(float)} for more
   *                 details.
   * @return a new Image XObject
   * @throws IOException if the JPEG data cannot be written
   */
  public static PDImageXObject createFromImage(final PDDocument document, final BufferedImage image,
      final float quality) throws IOException {
    return JPEGFactory.createFromImage(document, image, quality, 72);
  }

  /**
   * Creates a new JPEG Image XObject from a BufferedImage, a given quality and
   * dpi metadata.
   * <p>
   * Do not read a JPEG image from a stream/file and call this method; you'll get
   * more speed and quality by calling
   * {@link #createFromStream(org.apache.pdfbox.pdmodel.PDDocument, java.io.InputStream)
   * createFromStream()} instead.
   *
   * @param document the document where the image will be created
   * @param image    the BufferedImage to embed
   * @param quality  The desired JPEG compression quality; between 0 (best
   *                 compression) and 1 (best image quality). See
   *                 {@link ImageWriteParam#setCompressionQuality(float)} for more
   *                 details.
   * @param dpi      the desired dpi (resolution) value of the JPEG to be stored
   *                 in metadata. This value has no influence on image content or
   *                 size.
   * @return a new Image XObject
   * @throws IOException if the JPEG data cannot be written
   */
  public static PDImageXObject createFromImage(final PDDocument document, final BufferedImage image,
      final float quality, final int dpi) throws IOException {
    return JPEGFactory.createJPEG(document, image, quality, dpi);
  }

  // returns the alpha channel of an image
  private static BufferedImage getAlphaImage(final BufferedImage image) throws IOException {
    if (!image.getColorModel().hasAlpha())
      return null;
    if (image.getTransparency() == Transparency.BITMASK)
      throw new UnsupportedOperationException(
          "BITMASK Transparency JPEG compression is not" + " useful, use LosslessImageFactory instead");
    final WritableRaster alphaRaster = image.getAlphaRaster();
    if (alphaRaster == null)
      // happens sometimes (PDFBOX-2654) despite colormodel claiming to have alpha
      return null;
    final BufferedImage alphaImage = new BufferedImage(image.getWidth(), image.getHeight(),
        BufferedImage.TYPE_BYTE_GRAY);
    alphaImage.setData(alphaRaster);
    return alphaImage;
  }

  // Creates an Image XObject from a BufferedImage using JAI Image I/O
  private static PDImageXObject createJPEG(final PDDocument document, final BufferedImage image, final float quality,
      final int dpi) throws IOException {
    // extract alpha channel (if any)
    final BufferedImage awtColorImage = JPEGFactory.getColorImage(image);
    final BufferedImage awtAlphaImage = JPEGFactory.getAlphaImage(image);

    // create XObject
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JPEGFactory.encodeImageToJPEGStream(awtColorImage, quality, dpi, baos);
    final ByteArrayInputStream byteStream = new ByteArrayInputStream(baos.toByteArray());

    final PDImageXObject pdImage = new PDImageXObject(document, byteStream, COSName.DCT_DECODE,
        awtColorImage.getWidth(), awtColorImage.getHeight(), awtColorImage.getColorModel().getComponentSize(0),
        JPEGFactory.getColorSpaceFromAWT(awtColorImage));

    // alpha -> soft mask
    if (awtAlphaImage != null) {
      final PDImage xAlpha = JPEGFactory.createFromImage(document, awtAlphaImage, quality);
      pdImage.getCOSObject().setItem(COSName.SMASK, xAlpha);
    }

    return pdImage;
  }

  private static ImageWriter getJPEGImageWriter() throws IOException {
    ImageWriter writer = null;
    final Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("jpeg");
    while (writers.hasNext()) {
      if (writer != null) {
        writer.dispose();
      }
      writer = writers.next();
      if (writer == null) {
        continue;
      }
      // PDFBOX-3566: avoid CLibJPEGImageWriter, which is not a JPEGImageWriteParam
      if (writer.getDefaultWriteParam() instanceof JPEGImageWriteParam)
        return writer;
    }
    throw new IOException("No ImageWriter found for JPEG format");
  }

  private static void encodeImageToJPEGStream(final BufferedImage image, final float quality, final int dpi,
      final OutputStream out) throws IOException {
    // encode to JPEG
    ImageOutputStream ios = null;
    ImageWriter imageWriter = null;
    try {
      // find JAI writer
      imageWriter = JPEGFactory.getJPEGImageWriter();
      ios = ImageIO.createImageOutputStream(out);
      imageWriter.setOutput(ios);

      // add compression
      final JPEGImageWriteParam jpegParam = (JPEGImageWriteParam) imageWriter.getDefaultWriteParam();
      jpegParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      jpegParam.setCompressionQuality(quality);

      // add metadata
      final ImageTypeSpecifier imageTypeSpecifier = new ImageTypeSpecifier(image);
      final IIOMetadata data = imageWriter.getDefaultImageMetadata(imageTypeSpecifier, jpegParam);
      final Element tree = (Element) data.getAsTree("javax_imageio_jpeg_image_1.0");
      final Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);
      jfif.setAttribute("Xdensity", Integer.toString(dpi));
      jfif.setAttribute("Ydensity", Integer.toString(dpi));
      jfif.setAttribute("resUnits", "1"); // 1 = dots/inch

      // write
      imageWriter.write(data, new IIOImage(image, null, null), jpegParam);
    } finally {
      // clean up
      IOUtils.closeQuietly(out);
      if (ios != null) {
        ios.close();
      }
      if (imageWriter != null) {
        imageWriter.dispose();
      }
    }
  }

  // returns a PDColorSpace for a given BufferedImage
  private static PDColorSpace getColorSpaceFromAWT(final BufferedImage awtImage) {
    if (awtImage.getColorModel().getNumComponents() == 1)
      // 256 color (gray) JPEG
      return PDDeviceGray.INSTANCE;

    final ColorSpace awtColorSpace = awtImage.getColorModel().getColorSpace();
    if (awtColorSpace instanceof ICC_ColorSpace && !awtColorSpace.isCS_sRGB())
      throw new UnsupportedOperationException("ICC color spaces not implemented");

    switch (awtColorSpace.getType()) {
    case ColorSpace.TYPE_RGB:
      return PDDeviceRGB.INSTANCE;
    case ColorSpace.TYPE_GRAY:
      return PDDeviceGray.INSTANCE;
    case ColorSpace.TYPE_CMYK:
      return PDDeviceCMYK.INSTANCE;
    default:
      throw new UnsupportedOperationException("color space not implemented: " + awtColorSpace.getType());
    }
  }

  // returns the color channels of an image
  private static BufferedImage getColorImage(final BufferedImage image) {
    if (!image.getColorModel().hasAlpha())
      return image;

    if (image.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_RGB)
      throw new UnsupportedOperationException("only RGB color spaces are implemented");

    // create an RGB image without alpha
    // BEWARE: the previous solution in the history
    // g.setComposite(AlphaComposite.Src) and g.drawImage()
    // didn't work properly for TYPE_4BYTE_ABGR.
    // alpha values of 0 result in a black dest pixel!!!
    final BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    return new ColorConvertOp(null).filter(image, rgbImage);
  }
}
