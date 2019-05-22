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

package org.apache.pdfbox.pdmodel.font;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.fontbox.afm.AFMParser;
import org.apache.fontbox.afm.FontMetrics;

/**
 * The "Standard 14" PDF fonts, also known as the "base 14" fonts. There are 14
 * font files, but Acrobat uses additional names for compatibility, e.g. Arial.
 *
 * @author John Hewson
 */
final class Standard14Fonts {
  private Standard14Fonts() {
  }

  private static final Set<String> STANDARD_14_NAMES = new HashSet<>(34);
  private static final Map<String, String> STANDARD_14_MAPPING = new HashMap<>(34);
  private static final Map<String, FontMetrics> STANDARD14_AFM_MAP = new HashMap<>(34);
  static {
    try {
      Standard14Fonts.addAFM("Courier-Bold");
      Standard14Fonts.addAFM("Courier-BoldOblique");
      Standard14Fonts.addAFM("Courier");
      Standard14Fonts.addAFM("Courier-Oblique");
      Standard14Fonts.addAFM("Helvetica");
      Standard14Fonts.addAFM("Helvetica-Bold");
      Standard14Fonts.addAFM("Helvetica-BoldOblique");
      Standard14Fonts.addAFM("Helvetica-Oblique");
      Standard14Fonts.addAFM("Symbol");
      Standard14Fonts.addAFM("Times-Bold");
      Standard14Fonts.addAFM("Times-BoldItalic");
      Standard14Fonts.addAFM("Times-Italic");
      Standard14Fonts.addAFM("Times-Roman");
      Standard14Fonts.addAFM("ZapfDingbats");

      // alternative names from Adobe Supplement to the ISO 32000
      Standard14Fonts.addAFM("CourierCourierNew", "Courier");
      Standard14Fonts.addAFM("CourierNew", "Courier");
      Standard14Fonts.addAFM("CourierNew,Italic", "Courier-Oblique");
      Standard14Fonts.addAFM("CourierNew,Bold", "Courier-Bold");
      Standard14Fonts.addAFM("CourierNew,BoldItalic", "Courier-BoldOblique");
      Standard14Fonts.addAFM("Arial", "Helvetica");
      Standard14Fonts.addAFM("Arial,Italic", "Helvetica-Oblique");
      Standard14Fonts.addAFM("Arial,Bold", "Helvetica-Bold");
      Standard14Fonts.addAFM("Arial,BoldItalic", "Helvetica-BoldOblique");
      Standard14Fonts.addAFM("TimesNewRoman", "Times-Roman");
      Standard14Fonts.addAFM("TimesNewRoman,Italic", "Times-Italic");
      Standard14Fonts.addAFM("TimesNewRoman,Bold", "Times-Bold");
      Standard14Fonts.addAFM("TimesNewRoman,BoldItalic", "Times-BoldItalic");

      // Acrobat treats these fonts as "standard 14" too (at least Acrobat preflight
      // says so)
      Standard14Fonts.addAFM("Symbol,Italic", "Symbol");
      Standard14Fonts.addAFM("Symbol,Bold", "Symbol");
      Standard14Fonts.addAFM("Symbol,BoldItalic", "Symbol");
      Standard14Fonts.addAFM("Times", "Times-Roman");
      Standard14Fonts.addAFM("Times,Italic", "Times-Italic");
      Standard14Fonts.addAFM("Times,Bold", "Times-Bold");
      Standard14Fonts.addAFM("Times,BoldItalic", "Times-BoldItalic");

      // PDFBOX-3457: PDF.js file bug864847.pdf
      Standard14Fonts.addAFM("ArialMT", "Helvetica");
      Standard14Fonts.addAFM("Arial-ItalicMT", "Helvetica-Oblique");
      Standard14Fonts.addAFM("Arial-BoldMT", "Helvetica-Bold");
      Standard14Fonts.addAFM("Arial-BoldItalicMT", "Helvetica-BoldOblique");
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void addAFM(final String fontName) throws IOException {
    Standard14Fonts.addAFM(fontName, fontName);
  }

  private static void addAFM(final String fontName, final String afmName) throws IOException {
    Standard14Fonts.STANDARD_14_NAMES.add(fontName);
    Standard14Fonts.STANDARD_14_MAPPING.put(fontName, afmName);

    if (Standard14Fonts.STANDARD14_AFM_MAP.containsKey(afmName)) {
      Standard14Fonts.STANDARD14_AFM_MAP.put(fontName, Standard14Fonts.STANDARD14_AFM_MAP.get(afmName));
    }

    final String resourceName = "/org/apache/pdfbox/resources/afm/" + afmName + ".afm";
    try (InputStream afmStream = PDType1Font.class.getResourceAsStream(resourceName)) {
      if (afmStream == null)
        throw new IOException(resourceName + " not found");
      final AFMParser parser = new AFMParser(afmStream);
      final FontMetrics metric = parser.parse(true);
      Standard14Fonts.STANDARD14_AFM_MAP.put(fontName, metric);
    }
  }

  /**
   * Returns the AFM for the given font.
   *
   * @param baseName base name of font
   */
  public static FontMetrics getAFM(final String baseName) {
    return Standard14Fonts.STANDARD14_AFM_MAP.get(baseName);
  }

  /**
   * Returns true if the given font name a Standard 14 font.
   *
   * @param baseName base name of font
   */
  public static boolean containsName(final String baseName) {
    return Standard14Fonts.STANDARD_14_NAMES.contains(baseName);
  }

  /**
   * Returns the set of Standard 14 font names, including additional names.
   */
  public static Set<String> getNames() {
    return Collections.unmodifiableSet(Standard14Fonts.STANDARD_14_NAMES);
  }

  /**
   * Returns the name of the actual font which the given font name maps to.
   *
   * @param baseName base name of font
   */
  public static String getMappedFontName(final String baseName) {
    return Standard14Fonts.STANDARD_14_MAPPING.get(baseName);
  }
}
