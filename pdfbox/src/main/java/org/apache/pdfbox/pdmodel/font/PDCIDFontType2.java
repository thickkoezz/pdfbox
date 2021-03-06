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

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.cff.Type2CharString;
import org.apache.fontbox.cmap.CMap;
import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.GlyphData;
import org.apache.fontbox.ttf.OTFParser;
import org.apache.fontbox.ttf.OpenTypeFont;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.Matrix;

/**
 * Type 2 CIDFont (TrueType).
 *
 * @author Ben Litchfield
 */
public class PDCIDFontType2 extends PDCIDFont {
  private static final Log LOG = LogFactory.getLog(PDCIDFontType2.class);

  private final TrueTypeFont ttf;
  private final int[] cid2gid;
  private final boolean isEmbedded;
  private final boolean isDamaged;
  private final CmapLookup cmap; // may be null
  private Matrix fontMatrix;
  private BoundingBox fontBBox;

  /**
   * Constructor.
   *
   * @param fontDictionary The font dictionary according to the PDF specification.
   * @param parent         The parent font.
   * @throws IOException
   */
  public PDCIDFontType2(final COSDictionary fontDictionary, final PDType0Font parent) throws IOException {
    this(fontDictionary, parent, null);
  }

  /**
   * Constructor.
   *
   * @param fontDictionary The font dictionary according to the PDF specification.
   * @param parent         The parent font.
   * @param trueTypeFont   The true type font used to create the parent font
   * @throws IOException
   */
  public PDCIDFontType2(final COSDictionary fontDictionary, final PDType0Font parent, final TrueTypeFont trueTypeFont)
      throws IOException {
    super(fontDictionary, parent);

    final PDFontDescriptor fd = getFontDescriptor();
    if (trueTypeFont != null) {
      ttf = trueTypeFont;
      isEmbedded = true;
      isDamaged = false;
    } else {
      boolean fontIsDamaged = false;
      TrueTypeFont ttfFont = null;

      PDStream stream = null;
      if (fd != null) {
        stream = fd.getFontFile2();
        if (stream == null) {
          stream = fd.getFontFile3();
        }
        if (stream == null) {
          // Acrobat looks in FontFile too, even though it is not in the spec, see
          // PDFBOX-2599
          stream = fd.getFontFile();
        }
      }
      if (stream != null) {
        try {
          // embedded OTF or TTF
          final OTFParser otfParser = new OTFParser(true);
          final OpenTypeFont otf = otfParser.parse(stream.createInputStream());
          ttfFont = otf;

          if (otf.isPostScript()) {
            // PDFBOX-3344 contains PostScript outlines instead of TrueType
            fontIsDamaged = true;
            PDCIDFontType2.LOG.warn("Found CFF/OTF but expected embedded TTF font " + fd.getFontName());
          }

          if (otf.hasLayoutTables()) {
            PDCIDFontType2.LOG.info("OpenType Layout tables used in font " + getBaseFont()
                + " are not implemented in PDFBox and will be ignored");
          }
        } catch (NullPointerException | IOException e) {
          // NPE due to TTF parser being buggy
          fontIsDamaged = true;
          PDCIDFontType2.LOG.warn("Could not read embedded OTF for font " + getBaseFont(), e);
        }
      }
      isEmbedded = ttfFont != null;
      isDamaged = fontIsDamaged;

      if (ttfFont == null) {
        ttfFont = findFontOrSubstitute();
      }
      ttf = ttfFont;
    }
    cmap = ttf.getUnicodeCmapLookup(false);
    cid2gid = readCIDToGIDMap();
  }

  private TrueTypeFont findFontOrSubstitute() throws IOException {
    TrueTypeFont ttfFont;

    final CIDFontMapping mapping = FontMappers.instance().getCIDFont(getBaseFont(), getFontDescriptor(),
        getCIDSystemInfo());
    if (mapping.isCIDFont()) {
      ttfFont = mapping.getFont();
    } else {
      ttfFont = (TrueTypeFont) mapping.getTrueTypeFont();
    }
    if (mapping.isFallback()) {
      PDCIDFontType2.LOG
          .warn("Using fallback font " + ttfFont.getName() + " for CID-keyed TrueType font " + getBaseFont());
    }
    return ttfFont;
  }

  @Override
  public Matrix getFontMatrix() {
    if (fontMatrix == null) {
      // 1000 upem, this is not strictly true
      fontMatrix = new Matrix(0.001f, 0, 0, 0.001f, 0, 0);
    }
    return fontMatrix;
  }

  @Override
  public BoundingBox getBoundingBox() throws IOException {
    if (fontBBox == null) {
      fontBBox = generateBoundingBox();
    }
    return fontBBox;
  }

  private BoundingBox generateBoundingBox() throws IOException {
    if (getFontDescriptor() != null) {
      final PDRectangle bbox = getFontDescriptor().getFontBoundingBox();
      if (bbox != null && (Float.compare(bbox.getLowerLeftX(), 0) != 0 || Float.compare(bbox.getLowerLeftY(), 0) != 0
          || Float.compare(bbox.getUpperRightX(), 0) != 0 || Float.compare(bbox.getUpperRightY(), 0) != 0))
        return new BoundingBox(bbox.getLowerLeftX(), bbox.getLowerLeftY(), bbox.getUpperRightX(),
            bbox.getUpperRightY());
    }
    return ttf.getFontBBox();
  }

  @Override
  public int codeToCID(final int code) {
    final CMap cMap = parent.getCMap();

    // Acrobat allows bad PDFs to use Unicode CMaps here instead of CID CMaps, see
    // PDFBOX-1283
    if (!cMap.hasCIDMappings() && cMap.hasUnicodeMappings())
      return cMap.toUnicode(code).codePointAt(0); // actually: code -> CID

    return cMap.toCID(code);
  }

  /**
   * Returns the GID for the given character code.
   *
   * @param code character code
   * @return GID
   * @throws IOException
   */
  @Override
  public int codeToGID(final int code) throws IOException {
    if (!isEmbedded) {
      // The conforming reader shall select glyphs by translating characters from the
      // encoding specified by the predefined CMap to one of the encodings in the
      // TrueType
      // font's 'cmap' table. The means by which this is accomplished are
      // implementation-
      // dependent.
      // omit the CID2GID mapping if the embedded font is replaced by an external font
      if (cid2gid != null && !isDamaged) {
        // Acrobat allows non-embedded GIDs - todo: can we find a test PDF for this?
        PDCIDFontType2.LOG.warn("Using non-embedded GIDs in font " + getName());
        final int cid = codeToCID(code);
        return cid2gid[cid];
      } else {
        // fallback to the ToUnicode CMap, test with PDFBOX-1422 and PDFBOX-2560
        final String unicode = parent.toUnicode(code);
        if (unicode == null) {
          PDCIDFontType2.LOG.warn("Failed to find a character mapping for " + code + " in " + getName());
          // Acrobat is willing to use the CID as a GID, even when the font isn't embedded
          // see PDFBOX-2599
          return codeToCID(code);
        } else if (unicode.length() > 1) {
          PDCIDFontType2.LOG.warn("Trying to map multi-byte character using 'cmap', result will be poor");
        }

        // a non-embedded font always has a cmap (otherwise FontMapper won't load it)
        return cmap.getGlyphId(unicode.codePointAt(0));
      }
    } else {
      // If the TrueType font program is embedded, the Type 2 CIDFont dictionary shall
      // contain
      // a CIDToGIDMap entry that maps CIDs to the glyph indices for the appropriate
      // glyph
      // descriptions in that font program.

      final int cid = codeToCID(code);
      if (cid2gid != null) {
        // use CIDToGIDMap
        if (cid < cid2gid.length)
          return cid2gid[cid];
        else
          return 0;
      } else {
        // "Identity" is the default CIDToGIDMap
        if (cid < ttf.getNumberOfGlyphs())
          return cid;
        else
          // out of range CIDs map to GID 0
          return 0;
      }
    }
  }

  @Override
  public float getHeight(final int code) throws IOException {
    // todo: really we want the BBox, (for text extraction:)
    return (ttf.getHorizontalHeader().getAscender() + -ttf.getHorizontalHeader().getDescender()) / ttf.getUnitsPerEm(); // todo:
                                                                                                                        // shouldn't
                                                                                                                        // this
                                                                                                                        // be
                                                                                                                        // the
                                                                                                                        // yMax/yMin?
  }

  @Override
  public float getWidthFromFont(final int code) throws IOException {
    final int gid = codeToGID(code);
    int width = ttf.getAdvanceWidth(gid);
    final int unitsPerEM = ttf.getUnitsPerEm();
    if (unitsPerEM != 1000) {
      width *= 1000f / unitsPerEM;
    }
    return width;
  }

  @Override
  public byte[] encode(final int unicode) {
    int cid = -1;
    if (isEmbedded) {
      // embedded fonts always use CIDToGIDMap, with Identity as the default
      if (parent.getCMap().getName().startsWith("Identity-")) {
        if (cmap != null) {
          cid = cmap.getGlyphId(unicode);
        }
      } else {
        // if the CMap is predefined then there will be a UCS-2 CMap
        if (parent.getCMapUCS2() != null) {
          cid = parent.getCMapUCS2().toCID(unicode);
        }
      }

      // otherwise we require an explicit ToUnicode CMap
      if (cid == -1) {
        // TODO: invert the ToUnicode CMap?
        // see also PDFBOX-4233
        cid = 0;
      }
    } else {
      // a non-embedded font always has a cmap (otherwise it we wouldn't load it)
      cid = cmap.getGlyphId(unicode);
    }

    if (cid == 0)
      throw new IllegalArgumentException(String.format("No glyph for U+%04X in font %s", unicode, getName()));

    return encodeGlyphId(cid);
  }

  @Override
  public byte[] encodeGlyphId(final int glyphId) {
    // CID is always 2-bytes (16-bit) for TrueType
    return new byte[] { (byte) (glyphId >> 8 & 0xff), (byte) (glyphId & 0xff) };
  }

  @Override
  public boolean isEmbedded() {
    return isEmbedded;
  }

  @Override
  public boolean isDamaged() {
    return isDamaged;
  }

  /**
   * Returns the embedded or substituted TrueType font. May be an OpenType font if
   * the font is not embedded.
   */
  public TrueTypeFont getTrueTypeFont() {
    return ttf;
  }

  @Override
  public GeneralPath getPath(final int code) throws IOException {
    if (ttf instanceof OpenTypeFont && ((OpenTypeFont) ttf).isPostScript()) {
      // we're not supposed to have CFF fonts inside PDCIDFontType2, but if we do,
      // then we treat their CIDs as GIDs, see PDFBOX-3344
      final int cid = codeToGID(code);
      final Type2CharString charstring = ((OpenTypeFont) ttf).getCFF().getFont().getType2CharString(cid);
      return charstring.getPath();
    } else {
      final int gid = codeToGID(code);
      final GlyphData glyph = ttf.getGlyph().getGlyph(gid);
      if (glyph != null)
        return glyph.getPath();
      return new GeneralPath();
    }
  }

  @Override
  public GeneralPath getNormalizedPath(final int code) throws IOException {
    final boolean hasScaling = ttf.getUnitsPerEm() != 1000;
    final float scale = 1000f / ttf.getUnitsPerEm();
    final int gid = codeToGID(code);

    GeneralPath path = getPath(code);

    // Acrobat only draws GID 0 for embedded CIDFonts, see PDFBOX-2372
    if (gid == 0 && !isEmbedded()) {
      path = null;
    }

    if (path == null)
      // empty glyph (e.g. space, newline)
      return new GeneralPath();
    else {
      if (hasScaling) {
        path.transform(AffineTransform.getScaleInstance(scale, scale));
      }
      return path;
    }
  }

  @Override
  public boolean hasGlyph(final int code) throws IOException {
    return codeToGID(code) != 0;
  }
}
