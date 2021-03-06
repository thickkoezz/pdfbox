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
package org.apache.pdfbox.pdmodel.graphics.blend;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * AWT composite for blend modes.
 *
 * @author Kühn &amp; Weyh Software GmbH
 */
public final class BlendComposite implements Composite {
  /**
   * Log instance.
   */
  private static final Log LOG = LogFactory.getLog(BlendComposite.class);

  /**
   * Creates a blend composite
   *
   * @param blendMode     Desired blend mode
   * @param constantAlpha Constant alpha, must be in the inclusive range
   *                      [0.0...1.0] or it will be clipped.
   * @return a blend composite.
   */
  public static Composite getInstance(final BlendMode blendMode, float constantAlpha) {
    if (constantAlpha < 0) {
      BlendComposite.LOG.warn("using 0 instead of incorrect Alpha " + constantAlpha);
      constantAlpha = 0;
    } else if (constantAlpha > 1) {
      BlendComposite.LOG.warn("using 1 instead of incorrect Alpha " + constantAlpha);
      constantAlpha = 1;
    }
    if (blendMode == BlendMode.NORMAL)
      return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, constantAlpha);
    else
      return new BlendComposite(blendMode, constantAlpha);
  }

  private final BlendMode blendMode;
  private final float constantAlpha;

  private BlendComposite(final BlendMode blendMode, final float constantAlpha) {
    super();
    this.blendMode = blendMode;
    this.constantAlpha = constantAlpha;
  }

  @Override
  public CompositeContext createContext(final ColorModel srcColorModel, final ColorModel dstColorModel,
      final RenderingHints hints) {
    return new BlendCompositeContext(srcColorModel, dstColorModel, hints);
  }

  class BlendCompositeContext implements CompositeContext {
    private final ColorModel srcColorModel;
    private final ColorModel dstColorModel;

    BlendCompositeContext(final ColorModel srcColorModel, final ColorModel dstColorModel, final RenderingHints hints) {
      this.srcColorModel = srcColorModel;
      this.dstColorModel = dstColorModel;
    }

    @Override
    public void dispose() {
      // nothing needed
    }

    @Override
    public void compose(final Raster src, final Raster dstIn, final WritableRaster dstOut) {
      final int x0 = src.getMinX();
      final int y0 = src.getMinY();
      final int width = Math.min(Math.min(src.getWidth(), dstIn.getWidth()), dstOut.getWidth());
      final int height = Math.min(Math.min(src.getHeight(), dstIn.getHeight()), dstOut.getHeight());
      final int x1 = x0 + width;
      final int y1 = y0 + height;
      final int dstInXShift = dstIn.getMinX() - x0;
      final int dstInYShift = dstIn.getMinY() - y0;
      final int dstOutXShift = dstOut.getMinX() - x0;
      final int dstOutYShift = dstOut.getMinY() - y0;

      final ColorSpace srcColorSpace = srcColorModel.getColorSpace();
      final int numSrcColorComponents = srcColorModel.getNumColorComponents();
      final int numSrcComponents = src.getNumBands();
      final boolean srcHasAlpha = numSrcComponents > numSrcColorComponents;
      final ColorSpace dstColorSpace = dstColorModel.getColorSpace();
      final int numDstColorComponents = dstColorModel.getNumColorComponents();
      final int numDstComponents = dstIn.getNumBands();
      final boolean dstHasAlpha = numDstComponents > numDstColorComponents;

      final int srcColorSpaceType = srcColorSpace.getType();
      final int dstColorSpaceType = dstColorSpace.getType();
      final boolean subtractive = dstColorSpaceType != ColorSpace.TYPE_RGB && dstColorSpaceType != ColorSpace.TYPE_GRAY;

      final boolean blendModeIsSeparable = blendMode instanceof SeparableBlendMode;
      final SeparableBlendMode separableBlendMode = blendModeIsSeparable ? (SeparableBlendMode) blendMode : null;
      final NonSeparableBlendMode nonSeparableBlendMode = !blendModeIsSeparable ? (NonSeparableBlendMode) blendMode
          : null;

      final boolean needsColorConversion = !srcColorSpace.equals(dstColorSpace);

      Object srcPixel = null;
      Object dstPixel = null;
      float[] srcComponents = new float[numSrcComponents];
      // PDFBOX-3501 let getNormalizedComponents allocate to avoid
      // ArrayIndexOutOfBoundsException for bitonal target
      float[] dstComponents = null;

      final float[] srcColor = new float[numSrcColorComponents];
      float[] srcConverted;
      float[] dstConverted;
      final float[] rgbResult = blendModeIsSeparable ? null : new float[dstHasAlpha ? 4 : 3];

      for (int y = y0; y < y1; y++) {
        for (int x = x0; x < x1; x++) {
          srcPixel = src.getDataElements(x, y, srcPixel);
          dstPixel = dstIn.getDataElements(dstInXShift + x, dstInYShift + y, dstPixel);

          srcComponents = srcColorModel.getNormalizedComponents(srcPixel, srcComponents, 0);
          dstComponents = dstColorModel.getNormalizedComponents(dstPixel, dstComponents, 0);

          float srcAlpha = srcHasAlpha ? srcComponents[numSrcColorComponents] : 1.0f;
          final float dstAlpha = dstHasAlpha ? dstComponents[numDstColorComponents] : 1.0f;

          srcAlpha = srcAlpha * constantAlpha;

          final float resultAlpha = dstAlpha + srcAlpha - srcAlpha * dstAlpha;
          final float srcAlphaRatio = resultAlpha > 0 ? srcAlpha / resultAlpha : 0;

          if (separableBlendMode != null) {
            // convert color
            System.arraycopy(srcComponents, 0, srcColor, 0, numSrcColorComponents);
            if (needsColorConversion) {
              // TODO - very very slow - Hash results???
              final float[] cieXYZ = srcColorSpace.toCIEXYZ(srcColor);
              srcConverted = dstColorSpace.fromCIEXYZ(cieXYZ);
            } else {
              srcConverted = srcColor;
            }

            for (int k = 0; k < numDstColorComponents; k++) {
              float srcValue = srcConverted[k];
              float dstValue = dstComponents[k];

              if (subtractive) {
                srcValue = 1 - srcValue;
                dstValue = 1 - dstValue;
              }

              float value = separableBlendMode.blendChannel(srcValue, dstValue);
              value = srcValue + dstAlpha * (value - srcValue);
              value = dstValue + srcAlphaRatio * (value - dstValue);

              if (subtractive) {
                value = 1 - value;
              }

              dstComponents[k] = value;
            }
          } else {
            // Nonseparable blend modes are computed in RGB color space.
            // TODO - CMYK color spaces need special treatment.

            if (srcColorSpaceType == ColorSpace.TYPE_RGB) {
              srcConverted = srcComponents;
            } else {
              srcConverted = srcColorSpace.toRGB(srcComponents);
            }

            if (dstColorSpaceType == ColorSpace.TYPE_RGB) {
              dstConverted = dstComponents;
            } else {
              dstConverted = dstColorSpace.toRGB(dstComponents);
            }

            nonSeparableBlendMode.blend(srcConverted, dstConverted, rgbResult);

            for (int k = 0; k < 3; k++) {
              final float srcValue = srcConverted[k];
              final float dstValue = dstConverted[k];
              float value = rgbResult[k];
              value = Math.max(Math.min(value, 1.0f), 0.0f);
              value = srcValue + dstAlpha * (value - srcValue);
              value = dstValue + srcAlphaRatio * (value - dstValue);
              rgbResult[k] = value;
            }

            if (dstColorSpaceType == ColorSpace.TYPE_RGB) {
              System.arraycopy(rgbResult, 0, dstComponents, 0, dstComponents.length);
            } else {
              final float[] temp = dstColorSpace.fromRGB(rgbResult);
              System.arraycopy(temp, 0, dstComponents, 0, Math.min(dstComponents.length, temp.length));
            }
          }

          if (dstHasAlpha) {
            dstComponents[numDstColorComponents] = resultAlpha;
          }

          dstPixel = dstColorModel.getDataElements(dstComponents, 0, dstPixel);
          dstOut.setDataElements(dstOutXShift + x, dstOutYShift + y, dstPixel);
        }
      }
    }
  }
}
