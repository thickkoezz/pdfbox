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

package org.apache.pdfbox.cos;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * The "PDFDocEncoding" encoding. Note that this is *not* a Type 1 font
 * encoding, it is used only within PDF "text strings".
 */
final class PDFDocEncoding {
  private static final char REPLACEMENT_CHARACTER = '\uFFFD';

  private static final int[] CODE_TO_UNI;
  private static final Map<Character, Integer> UNI_TO_CODE;

  static {
    CODE_TO_UNI = new int[256];
    UNI_TO_CODE = new HashMap<>(256);

    // initialize with basically ISO-8859-1
    for (int i = 0; i < 256; i++) {
      // skip entries not in Unicode column
      if (i > 0x17 && i < 0x20) {
        continue;
      }
      if (i > 0x7E && i < 0xA1) {
        continue;
      }
      if (i == 0xAD) {
        continue;
      }

      PDFDocEncoding.set(i, (char) i);
    }

    // then do all deviations (based on the table in ISO 32000-1:2008)
    // block 1
    PDFDocEncoding.set(0x18, '\u02D8'); // BREVE
    PDFDocEncoding.set(0x19, '\u02C7'); // CARON
    PDFDocEncoding.set(0x1A, '\u02C6'); // MODIFIER LETTER CIRCUMFLEX ACCENT
    PDFDocEncoding.set(0x1B, '\u02D9'); // DOT ABOVE
    PDFDocEncoding.set(0x1C, '\u02DD'); // DOUBLE ACUTE ACCENT
    PDFDocEncoding.set(0x1D, '\u02DB'); // OGONEK
    PDFDocEncoding.set(0x1E, '\u02DA'); // RING ABOVE
    PDFDocEncoding.set(0x1F, '\u02DC'); // SMALL TILDE
    // block 2
    PDFDocEncoding.set(0x7F, PDFDocEncoding.REPLACEMENT_CHARACTER); // undefined
    PDFDocEncoding.set(0x80, '\u2022'); // BULLET
    PDFDocEncoding.set(0x81, '\u2020'); // DAGGER
    PDFDocEncoding.set(0x82, '\u2021'); // DOUBLE DAGGER
    PDFDocEncoding.set(0x83, '\u2026'); // HORIZONTAL ELLIPSIS
    PDFDocEncoding.set(0x84, '\u2014'); // EM DASH
    PDFDocEncoding.set(0x85, '\u2013'); // EN DASH
    PDFDocEncoding.set(0x86, '\u0192'); // LATIN SMALL LETTER SCRIPT F
    PDFDocEncoding.set(0x87, '\u2044'); // FRACTION SLASH (solidus)
    PDFDocEncoding.set(0x88, '\u2039'); // SINGLE LEFT-POINTING ANGLE QUOTATION MARK
    PDFDocEncoding.set(0x89, '\u203A'); // SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
    PDFDocEncoding.set(0x8A, '\u2212'); // MINUS SIGN
    PDFDocEncoding.set(0x8B, '\u2030'); // PER MILLE SIGN
    PDFDocEncoding.set(0x8C, '\u201E'); // DOUBLE LOW-9 QUOTATION MARK (quotedblbase)
    PDFDocEncoding.set(0x8D, '\u201C'); // LEFT DOUBLE QUOTATION MARK (quotedblleft)
    PDFDocEncoding.set(0x8E, '\u201D'); // RIGHT DOUBLE QUOTATION MARK (quotedblright)
    PDFDocEncoding.set(0x8F, '\u2018'); // LEFT SINGLE QUOTATION MARK (quoteleft)
    PDFDocEncoding.set(0x90, '\u2019'); // RIGHT SINGLE QUOTATION MARK (quoteright)
    PDFDocEncoding.set(0x91, '\u201A'); // SINGLE LOW-9 QUOTATION MARK (quotesinglbase)
    PDFDocEncoding.set(0x92, '\u2122'); // TRADE MARK SIGN
    PDFDocEncoding.set(0x93, '\uFB01'); // LATIN SMALL LIGATURE FI
    PDFDocEncoding.set(0x94, '\uFB02'); // LATIN SMALL LIGATURE FL
    PDFDocEncoding.set(0x95, '\u0141'); // LATIN CAPITAL LETTER L WITH STROKE
    PDFDocEncoding.set(0x96, '\u0152'); // LATIN CAPITAL LIGATURE OE
    PDFDocEncoding.set(0x97, '\u0160'); // LATIN CAPITAL LETTER S WITH CARON
    PDFDocEncoding.set(0x98, '\u0178'); // LATIN CAPITAL LETTER Y WITH DIAERESIS
    PDFDocEncoding.set(0x99, '\u017D'); // LATIN CAPITAL LETTER Z WITH CARON
    PDFDocEncoding.set(0x9A, '\u0131'); // LATIN SMALL LETTER DOTLESS I
    PDFDocEncoding.set(0x9B, '\u0142'); // LATIN SMALL LETTER L WITH STROKE
    PDFDocEncoding.set(0x9C, '\u0153'); // LATIN SMALL LIGATURE OE
    PDFDocEncoding.set(0x9D, '\u0161'); // LATIN SMALL LETTER S WITH CARON
    PDFDocEncoding.set(0x9E, '\u017E'); // LATIN SMALL LETTER Z WITH CARON
    PDFDocEncoding.set(0x9F, PDFDocEncoding.REPLACEMENT_CHARACTER); // undefined
    PDFDocEncoding.set(0xA0, '\u20AC'); // EURO SIGN
    // end of deviations
  }

  private PDFDocEncoding() {
  }

  private static void set(final int code, final char unicode) {
    PDFDocEncoding.CODE_TO_UNI[code] = unicode;
    PDFDocEncoding.UNI_TO_CODE.put(unicode, code);
  }

  /**
   * Returns the string representation of the given PDFDocEncoded bytes.
   */
  public static String toString(final byte[] bytes) {
    final StringBuilder sb = new StringBuilder();
    for (final byte b : bytes) {
      if ((b & 0xff) >= PDFDocEncoding.CODE_TO_UNI.length) {
        sb.append('?');
      } else {
        sb.append((char) PDFDocEncoding.CODE_TO_UNI[b & 0xff]);
      }
    }
    return sb.toString();
  }

  /**
   * Returns the given string encoded with PDFDocEncoding.
   */
  public static byte[] getBytes(final String text) {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    for (final char c : text.toCharArray()) {
      final Integer code = PDFDocEncoding.UNI_TO_CODE.get(c);
      if (code == null) {
        out.write(0);
      } else {
        out.write(code);
      }
    }
    return out.toByteArray();
  }

  /**
   * Returns true if the given character is available in PDFDocEncoding.
   *
   * @param character UTF-16 character
   */
  public static boolean containsChar(final char character) {
    return PDFDocEncoding.UNI_TO_CODE.containsKey(character);
  }
}
