/*
 * Copyright 2017 The Apache Software Foundation.
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
package org.apache.pdfbox.rendering;

import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.util.Matrix;

/**
 * Factory class to cache TilingPaint generation.
 *
 * @author Tilman Hausherr
 */
class TilingPaintFactory {
  private static final Log LOG = LogFactory.getLog(TilingPaintFactory.class);

  private final PageDrawer drawer;
  private final Map<TilingPaintParameter, WeakReference<Paint>> weakCache = new WeakHashMap<>();

  TilingPaintFactory(final PageDrawer drawer) {
    this.drawer = drawer;
  }

  Paint create(final PDTilingPattern pattern, final PDColorSpace colorSpace, final PDColor color,
      final AffineTransform xform) throws IOException {
    Paint paint = null;
    final TilingPaintParameter tilingPaintParameter = new TilingPaintParameter(drawer.getInitialMatrix(),
        pattern.getCOSObject(), colorSpace, color, xform);
    final WeakReference<Paint> weakRef = weakCache.get(tilingPaintParameter);
    if (weakRef != null) {
      // PDFBOX-4058: additional WeakReference makes gc work better
      paint = weakRef.get();
    }
    if (paint == null) {
      paint = new TilingPaint(drawer, pattern, colorSpace, color, xform);
      weakCache.put(tilingPaintParameter, new WeakReference<>(paint));
    }
    return paint;
  }

  // class to characterize a TilingPaint object. It is important that TilingPaint
  // does not
  // keep any objects from this class, so that the weak cache works.
  private static class TilingPaintParameter {
    private final Matrix matrix;
    private final COSDictionary patternDict;
    private final PDColorSpace colorSpace;
    private final PDColor color;
    private final AffineTransform xform;

    private TilingPaintParameter(final Matrix matrix, final COSDictionary patternDict, final PDColorSpace colorSpace,
        final PDColor color, final AffineTransform xform) {
      this.matrix = matrix.clone();
      this.patternDict = patternDict;
      this.colorSpace = colorSpace;
      this.color = color;
      this.xform = xform;
    }

    // this may not catch all equals, but at least those related to one resource
    // dictionary.
    // it isn't needed to investigate further because matrix or transform would be
    // different anyway.
    @Override
    public boolean equals(final Object obj) {
      if (this == obj)
        return true;
      if (!(obj instanceof TilingPaintParameter))
        return false;
      final TilingPaintParameter other = (TilingPaintParameter) obj;
      if (matrix != other.matrix && (matrix == null || !matrix.equals(other.matrix)))
        return false;
      if (patternDict != other.patternDict && (patternDict == null || !patternDict.equals(other.patternDict)))
        return false;
      if (colorSpace != other.colorSpace && (colorSpace == null || !colorSpace.equals(other.colorSpace)))
        return false;
      if (color == null && other.color != null)
        return false;
      if (color != null && other.color == null)
        return false;
      if (color != null && color.getColorSpace() != other.color.getColorSpace())
        return false;
      try {
        if (color != other.color && color.toRGB() != other.color.toRGB())
          return false;
      } catch (final IOException ex) {
        TilingPaintFactory.LOG.debug("Couldn't convert color to RGB - treating as not equal", ex);
        return false;
      }
      return !(xform != other.xform && (xform == null || !xform.equals(other.xform)));
    }

    @Override
    public int hashCode() {
      int hash = 7;
      hash = 23 * hash + (matrix != null ? matrix.hashCode() : 0);
      hash = 23 * hash + (patternDict != null ? patternDict.hashCode() : 0);
      hash = 23 * hash + (colorSpace != null ? colorSpace.hashCode() : 0);
      hash = 23 * hash + (color != null ? color.hashCode() : 0);
      hash = 23 * hash + (xform != null ? xform.hashCode() : 0);
      return hash;
    }

    @Override
    public String toString() {
      return "TilingPaintParameter{" + "matrix=" + matrix + ", pattern=" + patternDict + ", colorSpace=" + colorSpace
          + ", color=" + color + ", xform=" + xform + '}';
    }
  }
}
