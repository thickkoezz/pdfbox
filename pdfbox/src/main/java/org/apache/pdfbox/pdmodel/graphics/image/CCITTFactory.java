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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.filter.FilterFactory;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;

/**
 * Factory for creating a PDImageXObject containing a CCITT Fax compressed TIFF
 * image.
 *
 * @author Ben Litchfield
 * @author Paul King
 */
public final class CCITTFactory {
  private CCITTFactory() {
  }

  /**
   * Creates a new CCITT group 4 (T6) compressed image XObject from a b/w
   * BufferedImage. This compression technique usually results in smaller images
   * than those produced by
   * {@link LosslessFactory#createFromImage(PDDocument, BufferedImage) }.
   *
   * @param document the document to create the image as part of.
   * @param image    the image.
   * @return a new image XObject.
   * @throws IOException              if there is an error creating the image.
   * @throws IllegalArgumentException if the BufferedImage is not a b/w image.
   */
  public static PDImageXObject createFromImage(final PDDocument document, final BufferedImage image)
      throws IOException {
    if (image.getType() != BufferedImage.TYPE_BYTE_BINARY && image.getColorModel().getPixelSize() != 1)
      throw new IllegalArgumentException("Only 1-bit b/w images supported");

    final int height = image.getHeight();
    final int width = image.getWidth();

    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(bos)) {
      for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
          // flip bit to avoid having to set /BlackIs1
          mcios.writeBits(~(image.getRGB(x, y) & 1), 1);
        }
        if (mcios.getBitOffset() != 0) {
          mcios.writeBits(0, 8 - mcios.getBitOffset());
        }
      }
      mcios.flush();
    }

    return CCITTFactory.prepareImageXObject(document, bos.toByteArray(), width, height, PDDeviceGray.INSTANCE);
  }

  /**
   * Creates a new CCITT Fax compressed image XObject from a specific image of a
   * TIFF file stored in a byte array. Only single-strip CCITT T4 or T6 compressed
   * TIFF files are supported. If you're not sure what TIFF files you have, use
   * {@link LosslessFactory#createFromImage(PDDocument, BufferedImage) } or
   * {@link CCITTFactory#createFromImage(PDDocument, BufferedImage) } instead.
   *
   * @param document  the document to create the image as part of.
   * @param byteArray the TIFF file in a byte array which contains a suitable
   *                  CCITT compressed image
   * @return a new Image XObject
   * @throws IOException if there is an error reading the TIFF data.
   */
  public static PDImageXObject createFromByteArray(final PDDocument document, final byte[] byteArray)
      throws IOException {
    return CCITTFactory.createFromByteArray(document, byteArray, 0);
  }

  /**
   * Creates a new CCITT Fax compressed image XObject from a specific image of a
   * TIFF file stored in a byte array. Only single-strip CCITT T4 or T6 compressed
   * TIFF files are supported. If you're not sure what TIFF files you have, use
   * {@link LosslessFactory#createFromImage(PDDocument, BufferedImage) } or
   * {@link CCITTFactory#createFromImage(PDDocument, BufferedImage) } instead.
   *
   * @param document  the document to create the image as part of.
   * @param byteArray the TIFF file in a byte array which contains a suitable
   *                  CCITT compressed image
   * @param number    TIFF image number, starting from 0
   * @return a new Image XObject
   * @throws IOException if there is an error reading the TIFF data.
   */
  public static PDImageXObject createFromByteArray(final PDDocument document, final byte[] byteArray, final int number)
      throws IOException {
    try (RandomAccess raf = new RandomAccessBuffer(byteArray)) {
      return CCITTFactory.createFromRandomAccessImpl(document, raf, number);
    }
  }

  private static PDImageXObject prepareImageXObject(final PDDocument document, final byte[] byteArray, final int width,
      final int height, final PDColorSpace initColorSpace) throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    final Filter filter = FilterFactory.INSTANCE.getFilter(COSName.CCITTFAX_DECODE);
    final COSDictionary dict = new COSDictionary();
    dict.setInt(COSName.COLUMNS, width);
    dict.setInt(COSName.ROWS, height);
    filter.encode(new ByteArrayInputStream(byteArray), baos, dict, 0);

    final ByteArrayInputStream encodedByteStream = new ByteArrayInputStream(baos.toByteArray());
    final PDImageXObject image = new PDImageXObject(document, encodedByteStream, COSName.CCITTFAX_DECODE, width, height,
        1, initColorSpace);
    dict.setInt(COSName.K, -1);
    image.getCOSObject().setItem(COSName.DECODE_PARMS, dict);
    return image;
  }

  /**
   * Creates a new CCITT Fax compressed image XObject from the first image of a
   * TIFF file. Only single-strip CCITT T4 or T6 compressed TIFF files are
   * supported. If you're not sure what TIFF files you have, use
   * {@link LosslessFactory#createFromImage(org.apache.pdfbox.pdmodel.PDDocument, java.awt.image.BufferedImage)}
   * or {@link CCITTFactory#createFromImage(PDDocument, BufferedImage) } instead.
   *
   * @param document the document to create the image as part of.
   * @param file     the TIFF file which contains a suitable CCITT compressed
   *                 image
   * @return a new Image XObject
   * @throws IOException if there is an error reading the TIFF data.
   */
  public static PDImageXObject createFromFile(final PDDocument document, final File file) throws IOException {
    return CCITTFactory.createFromFile(document, file, 0);
  }

  /**
   * Creates a new CCITT Fax compressed image XObject from a specific image of a
   * TIFF file. Only single-strip CCITT T4 or T6 compressed TIFF files are
   * supported. If you're not sure what TIFF files you have, use
   * {@link LosslessFactory#createFromImage(PDDocument, BufferedImage) } or
   * {@link CCITTFactory#createFromImage(PDDocument, BufferedImage) } instead.
   *
   * @param document the document to create the image as part of.
   * @param file     the TIFF file which contains a suitable CCITT compressed
   *                 image
   * @param number   TIFF image number, starting from 0
   * @return a new Image XObject
   * @throws IOException if there is an error reading the TIFF data.
   */
  public static PDImageXObject createFromFile(final PDDocument document, final File file, final int number)
      throws IOException {
    try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
      return CCITTFactory.createFromRandomAccessImpl(document, raf, number);
    }
  }

  /**
   * Creates a new CCITT Fax compressed image XObject from a TIFF file.
   *
   * @param document the document to create the image as part of.
   * @param reader   the random access TIFF file which contains a suitable CCITT
   *                 compressed image
   * @param number   TIFF image number, starting from 0
   * @return a new Image XObject, or null if no such page
   * @throws IOException if there is an error reading the TIFF data.
   */
  private static PDImageXObject createFromRandomAccessImpl(final PDDocument document, final RandomAccess reader,
      final int number) throws IOException {
    final COSDictionary decodeParms = new COSDictionary();
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    CCITTFactory.extractFromTiff(reader, bos, decodeParms, number);
    if (bos.size() == 0)
      return null;
    final ByteArrayInputStream encodedByteStream = new ByteArrayInputStream(bos.toByteArray());
    final PDImageXObject pdImage = new PDImageXObject(document, encodedByteStream, COSName.CCITTFAX_DECODE,
        decodeParms.getInt(COSName.COLUMNS), decodeParms.getInt(COSName.ROWS), 1, PDDeviceGray.INSTANCE);

    final COSDictionary dict = pdImage.getCOSObject();
    dict.setItem(COSName.DECODE_PARMS, decodeParms);
    return pdImage;
  }

  // extracts the CCITT stream from the TIFF file
  private static void extractFromTiff(final RandomAccess reader, final OutputStream os, final COSDictionary params,
      final int number) throws IOException {
    try {
      // First check the basic tiff header
      reader.seek(0);
      final char endianess = (char) reader.read();
      if ((char) reader.read() != endianess)
        throw new IOException("Not a valid tiff file");
      // ensure that endianess is either M or I
      if (endianess != 'M' && endianess != 'I')
        throw new IOException("Not a valid tiff file");
      final int magicNumber = CCITTFactory.readshort(endianess, reader);
      if (magicNumber != 42)
        throw new IOException("Not a valid tiff file");

      // Relocate to the first set of tags
      int address = CCITTFactory.readlong(endianess, reader);
      reader.seek(address);

      // If some higher page number is required, skip this page's tags,
      // then read the next page's address
      for (int i = 0; i < number; i++) {
        final int numtags = CCITTFactory.readshort(endianess, reader);
        if (numtags > 50)
          throw new IOException("Not a valid tiff file");
        reader.seek(address + 2 + numtags * 12);
        address = CCITTFactory.readlong(endianess, reader);
        if (address == 0)
          return;
        reader.seek(address);
      }

      final int numtags = CCITTFactory.readshort(endianess, reader);

      // The number 50 is somewhat arbitary, it just stops us load up junk from
      // somewhere
      // and tramping on
      if (numtags > 50)
        throw new IOException("Not a valid tiff file");

      // Loop through the tags, some will convert to items in the params dictionary
      // Other point us to where to find the data stream.
      // The only param which might change as a result of other TIFF tags is K, so
      // we'll deal with that differently.

      // Default value to detect error
      int k = -1000;

      int dataoffset = 0;
      int datalength = 0;

      for (int i = 0; i < numtags; i++) {
        final int tag = CCITTFactory.readshort(endianess, reader);
        final int type = CCITTFactory.readshort(endianess, reader);
        final int count = CCITTFactory.readlong(endianess, reader);
        int val;
        // Note that when the type is shorter than 4 bytes, the rest can be garbage
        // and must be ignored. E.g. short (2 bytes) from "01 00 38 32" (little endian)
        // is 1, not 842530817 (seen in a real-life TIFF image).
        switch (type) {
        case 1: // byte value
          val = reader.read();
          reader.read();
          reader.read();
          reader.read();
          break;
        case 3: // short value
          val = CCITTFactory.readshort(endianess, reader);
          reader.read();
          reader.read();
          break;
        default: // long and other types
          val = CCITTFactory.readlong(endianess, reader);
          break;
        }
        switch (tag) {
        case 256: {
          params.setInt(COSName.COLUMNS, val);
          break;
        }
        case 257: {
          params.setInt(COSName.ROWS, val);
          break;
        }
        case 259: {
          if (val == 4) {
            k = -1;
          }
          if (val == 3) {
            k = 0;
          }
          break; // T6/T4 Compression
        }
        case 262: {
          if (val == 1) {
            params.setBoolean(COSName.BLACK_IS_1, true);
          }
          break;
        }
        case 266: {
          if (val != 1)
            throw new IOException("FillOrder " + val + " is not supported");
          break;
        }
        case 273: {
          if (count == 1) {
            dataoffset = val;
          }
          break;
        }
        case 274: {
          // http://www.awaresystems.be/imaging/tiff/tifftags/orientation.html
          if (val != 1)
            throw new IOException("Orientation " + val + " is not supported");
          break;
        }
        case 279: {
          if (count == 1) {
            datalength = val;
          }
          break;
        }
        case 292: {
          if ((val & 1) != 0) {
            // T4 2D - arbitary positive K value
            k = 50;
          }
          // http://www.awaresystems.be/imaging/tiff/tifftags/t4options.html
          if ((val & 4) != 0)
            throw new IOException("CCITT Group 3 'uncompressed mode' is not supported");
          if ((val & 2) != 0)
            throw new IOException("CCITT Group 3 'fill bits before EOL' is not supported");
          break;
        }
        case 324: {
          if (count == 1) {
            dataoffset = val;
          }
          break;
        }
        case 325: {
          if (count == 1) {
            datalength = val;
          }
          break;
        }
        default: {
          // do nothing
        }
        }
      }

      if (k == -1000)
        throw new IOException("First image in tiff is not CCITT T4 or T6 compressed");
      if (dataoffset == 0)
        throw new IOException("First image in tiff is not a single tile/strip");

      params.setInt(COSName.K, k);

      reader.seek(dataoffset);

      final byte[] buf = new byte[8192];
      int amountRead;
      while ((amountRead = reader.read(buf, 0, Math.min(8192, datalength))) > 0) {
        datalength -= amountRead;
        os.write(buf, 0, amountRead);
      }

    } finally {
      os.close();
    }
  }

  private static int readshort(final char endianess, final RandomAccess raf) throws IOException {
    if (endianess == 'I')
      return raf.read() | raf.read() << 8;
    return raf.read() << 8 | raf.read();
  }

  private static int readlong(final char endianess, final RandomAccess raf) throws IOException {
    if (endianess == 'I')
      return raf.read() | raf.read() << 8 | raf.read() << 16 | raf.read() << 24;
    return raf.read() << 24 | raf.read() << 16 | raf.read() << 8 | raf.read();
  }
}
