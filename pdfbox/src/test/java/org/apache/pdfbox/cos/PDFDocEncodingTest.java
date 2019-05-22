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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for PDFDocEncoding.
 *
 */
public class PDFDocEncodingTest {

  static List<String> deviations = new ArrayList<>();

  static {
    // all deviations (based on the table in ISO 32000-1:2008)
    // block 1
    PDFDocEncodingTest.deviations.add(String.valueOf('\u02D8')); // BREVE
    PDFDocEncodingTest.deviations.add(String.valueOf('\u02C7')); // CARON
    PDFDocEncodingTest.deviations.add(String.valueOf('\u02C6')); // MODIFIER LETTER CIRCUMFLEX ACCENT
    PDFDocEncodingTest.deviations.add(String.valueOf('\u02D9')); // DOT ABOVE
    PDFDocEncodingTest.deviations.add(String.valueOf('\u02DD')); // DOUBLE ACUTE ACCENT
    PDFDocEncodingTest.deviations.add(String.valueOf('\u02DB')); // OGONEK
    PDFDocEncodingTest.deviations.add(String.valueOf('\u02DA')); // RING ABOVE
    PDFDocEncodingTest.deviations.add(String.valueOf('\u02DC')); // SMALL TILDE
    // block 2
    PDFDocEncodingTest.deviations.add(String.valueOf('\u2022')); // BULLET
    PDFDocEncodingTest.deviations.add(String.valueOf('\u2020')); // DAGGER
    PDFDocEncodingTest.deviations.add(String.valueOf('\u2021')); // DOUBLE DAGGER
    PDFDocEncodingTest.deviations.add(String.valueOf('\u2026')); // HORIZONTAL ELLIPSIS
    PDFDocEncodingTest.deviations.add(String.valueOf('\u2014')); // EM DASH
    PDFDocEncodingTest.deviations.add(String.valueOf('\u2013')); // EN DASH
    PDFDocEncodingTest.deviations.add(String.valueOf('\u0192')); // LATIN SMALL LETTER SCRIPT F
    PDFDocEncodingTest.deviations.add(String.valueOf('\u2044')); // FRACTION SLASH (solidus)
    PDFDocEncodingTest.deviations.add(String.valueOf('\u2039')); // SINGLE LEFT-POINTING ANGLE QUOTATION MARK
    PDFDocEncodingTest.deviations.add(String.valueOf('\u203A')); // SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
    PDFDocEncodingTest.deviations.add(String.valueOf('\u2212')); // MINUS SIGN
    PDFDocEncodingTest.deviations.add(String.valueOf('\u2030')); // PER MILLE SIGN
    PDFDocEncodingTest.deviations.add(String.valueOf('\u201E')); // DOUBLE LOW-9 QUOTATION MARK (quotedblbase)
    PDFDocEncodingTest.deviations.add(String.valueOf('\u201C')); // LEFT DOUBLE QUOTATION MARK (quotedblleft)
    PDFDocEncodingTest.deviations.add(String.valueOf('\u201D')); // RIGHT DOUBLE QUOTATION MARK (quotedblright)
    PDFDocEncodingTest.deviations.add(String.valueOf('\u2018')); // LEFT SINGLE QUOTATION MARK (quoteleft)
    PDFDocEncodingTest.deviations.add(String.valueOf('\u2019')); // RIGHT SINGLE QUOTATION MARK (quoteright)
    PDFDocEncodingTest.deviations.add(String.valueOf('\u201A')); // SINGLE LOW-9 QUOTATION MARK (quotesinglbase)
    PDFDocEncodingTest.deviations.add(String.valueOf('\u2122')); // TRADE MARK SIGN
    PDFDocEncodingTest.deviations.add(String.valueOf('\uFB01')); // LATIN SMALL LIGATURE FI
    PDFDocEncodingTest.deviations.add(String.valueOf('\uFB02')); // LATIN SMALL LIGATURE FL
    PDFDocEncodingTest.deviations.add(String.valueOf('\u0141')); // LATIN CAPITAL LETTER L WITH STROKE
    PDFDocEncodingTest.deviations.add(String.valueOf('\u0152')); // LATIN CAPITAL LIGATURE OE
    PDFDocEncodingTest.deviations.add(String.valueOf('\u0160')); // LATIN CAPITAL LETTER S WITH CARON
    PDFDocEncodingTest.deviations.add(String.valueOf('\u0178')); // LATIN CAPITAL LETTER Y WITH DIAERESIS
    PDFDocEncodingTest.deviations.add(String.valueOf('\u017D')); // LATIN CAPITAL LETTER Z WITH CARON
    PDFDocEncodingTest.deviations.add(String.valueOf('\u0131')); // LATIN SMALL LETTER DOTLESS I
    PDFDocEncodingTest.deviations.add(String.valueOf('\u0142')); // LATIN SMALL LETTER L WITH STROKE
    PDFDocEncodingTest.deviations.add(String.valueOf('\u0153')); // LATIN SMALL LIGATURE OE
    PDFDocEncodingTest.deviations.add(String.valueOf('\u0161')); // LATIN SMALL LETTER S WITH CARON
    PDFDocEncodingTest.deviations.add(String.valueOf('\u017E')); // LATIN SMALL LETTER Z WITH CARON
    PDFDocEncodingTest.deviations.add(String.valueOf('\u20AC')); // EURO SIGN
    // end of deviations
  }

  @Test
  public void testDeviations() {
    for (final String deviation : PDFDocEncodingTest.deviations) {
      final COSString cosString = new COSString(deviation);
      Assert.assertEquals(cosString.getString(), deviation);
    }
  }

  /**
   * PDFBOX-3864: Test that chars smaller than 256 which are NOT part of
   * PDFDocEncoding are handled correctly.
   *
   * @throws IOException
   */
  @Test
  public void testPDFBox3864() throws IOException {
    for (int i = 0; i < 256; i++) {
      final String hex = String.format("FEFF%04X", i);
      final COSString cs1 = COSString.parseHex(hex);
      final COSString cs2 = new COSString(cs1.getString());
      Assert.assertEquals(cs1, cs2);
    }
  }
}
