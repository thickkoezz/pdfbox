/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License")); you may not use this file except in compliance with
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

import org.apache.pdfbox.cos.COSName;
import org.junit.Test;

import junit.framework.TestCase;

/**
 *
 * @author Tilman Hausherr
 */
public class BlendModeTest {
  public BlendModeTest() {
  }

  /**
   * Check that BlendMode.* constant instances are not null. This could happen if
   * the declaration sequence is changed.
   */
  @Test
  public void testInstances() {
    TestCase.assertEquals(BlendMode.NORMAL, BlendMode.getInstance(COSName.NORMAL));
    TestCase.assertEquals(BlendMode.NORMAL, BlendMode.getInstance(COSName.COMPATIBLE));
    TestCase.assertEquals(BlendMode.MULTIPLY, BlendMode.getInstance(COSName.MULTIPLY));
    TestCase.assertEquals(BlendMode.SCREEN, BlendMode.getInstance(COSName.SCREEN));
    TestCase.assertEquals(BlendMode.OVERLAY, BlendMode.getInstance(COSName.OVERLAY));
    TestCase.assertEquals(BlendMode.DARKEN, BlendMode.getInstance(COSName.DARKEN));
    TestCase.assertEquals(BlendMode.LIGHTEN, BlendMode.getInstance(COSName.LIGHTEN));
    TestCase.assertEquals(BlendMode.COLOR_DODGE, BlendMode.getInstance(COSName.COLOR_DODGE));
    TestCase.assertEquals(BlendMode.COLOR_BURN, BlendMode.getInstance(COSName.COLOR_BURN));
    TestCase.assertEquals(BlendMode.HARD_LIGHT, BlendMode.getInstance(COSName.HARD_LIGHT));
    TestCase.assertEquals(BlendMode.SOFT_LIGHT, BlendMode.getInstance(COSName.SOFT_LIGHT));
    TestCase.assertEquals(BlendMode.DIFFERENCE, BlendMode.getInstance(COSName.DIFFERENCE));
    TestCase.assertEquals(BlendMode.EXCLUSION, BlendMode.getInstance(COSName.EXCLUSION));
    TestCase.assertEquals(BlendMode.HUE, BlendMode.getInstance(COSName.HUE));
    TestCase.assertEquals(BlendMode.SATURATION, BlendMode.getInstance(COSName.SATURATION));
    TestCase.assertEquals(BlendMode.LUMINOSITY, BlendMode.getInstance(COSName.LUMINOSITY));
    TestCase.assertEquals(BlendMode.COLOR, BlendMode.getInstance(COSName.COLOR));
  }

  /**
   * Check that COSName constants returned for BlendMode.* instances are not null.
   * This could happen if the declaration sequence is changed.
   */
  @Test
  public void testCOSNames() {
    TestCase.assertEquals(COSName.NORMAL, BlendMode.getCOSName(BlendMode.NORMAL));
    TestCase.assertEquals(COSName.NORMAL, BlendMode.getCOSName(BlendMode.COMPATIBLE));
    TestCase.assertEquals(COSName.MULTIPLY, BlendMode.getCOSName(BlendMode.MULTIPLY));
    TestCase.assertEquals(COSName.SCREEN, BlendMode.getCOSName(BlendMode.SCREEN));
    TestCase.assertEquals(COSName.OVERLAY, BlendMode.getCOSName(BlendMode.OVERLAY));
    TestCase.assertEquals(COSName.DARKEN, BlendMode.getCOSName(BlendMode.DARKEN));
    TestCase.assertEquals(COSName.LIGHTEN, BlendMode.getCOSName(BlendMode.LIGHTEN));
    TestCase.assertEquals(COSName.COLOR_DODGE, BlendMode.getCOSName(BlendMode.COLOR_DODGE));
    TestCase.assertEquals(COSName.COLOR_BURN, BlendMode.getCOSName(BlendMode.COLOR_BURN));
    TestCase.assertEquals(COSName.HARD_LIGHT, BlendMode.getCOSName(BlendMode.HARD_LIGHT));
    TestCase.assertEquals(COSName.SOFT_LIGHT, BlendMode.getCOSName(BlendMode.SOFT_LIGHT));
    TestCase.assertEquals(COSName.DIFFERENCE, BlendMode.getCOSName(BlendMode.DIFFERENCE));
    TestCase.assertEquals(COSName.EXCLUSION, BlendMode.getCOSName(BlendMode.EXCLUSION));
    TestCase.assertEquals(COSName.HUE, BlendMode.getCOSName(BlendMode.HUE));
    TestCase.assertEquals(COSName.SATURATION, BlendMode.getCOSName(BlendMode.SATURATION));
    TestCase.assertEquals(COSName.LUMINOSITY, BlendMode.getCOSName(BlendMode.LUMINOSITY));
    TestCase.assertEquals(COSName.COLOR, BlendMode.getCOSName(BlendMode.COLOR));
  }
}
