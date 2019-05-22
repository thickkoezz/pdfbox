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
package org.apache.pdfbox.pdmodel.graphics.color;

import java.util.Arrays;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.common.PDRange;

import junit.framework.TestCase;

/**
 *
 * @author Tilman Hausherr
 */
public class PDLabTest extends TestCase {

  /**
   * This test checks that getting default values do not alter the object, and
   * checks getters and setters.
   */
  public void testLAB() {
    final PDLab pdLab = new PDLab();
    final COSArray cosArray = (COSArray) pdLab.getCOSObject();
    final COSDictionary dict = (COSDictionary) cosArray.getObject(1);

    // test with default values
    TestCase.assertEquals("Lab", pdLab.getName());
    TestCase.assertEquals(3, pdLab.getNumberOfComponents());
    TestCase.assertNotNull(pdLab.getInitialColor());
    TestCase.assertTrue(Arrays.equals(new float[] { 0, 0, 0 }, pdLab.getInitialColor().getComponents()));
    TestCase.assertEquals(0f, pdLab.getBlackPoint().getX());
    TestCase.assertEquals(0f, pdLab.getBlackPoint().getY());
    TestCase.assertEquals(0f, pdLab.getBlackPoint().getZ());
    TestCase.assertEquals(1f, pdLab.getWhitepoint().getX());
    TestCase.assertEquals(1f, pdLab.getWhitepoint().getY());
    TestCase.assertEquals(1f, pdLab.getWhitepoint().getZ());
    TestCase.assertEquals(-100f, pdLab.getARange().getMin());
    TestCase.assertEquals(100f, pdLab.getARange().getMax());
    TestCase.assertEquals(-100f, pdLab.getBRange().getMin());
    TestCase.assertEquals(100f, pdLab.getBRange().getMax());
    TestCase.assertEquals("read operations should not change the size of /Lab objects", 0, dict.size());
    dict.toString(); // rev 1571125 did a stack overflow here

    // test setting specific values
    PDRange pdRange = new PDRange();
    pdRange.setMin(-1);
    pdRange.setMax(2);
    pdLab.setARange(pdRange);
    pdRange = new PDRange();
    pdRange.setMin(3);
    pdRange.setMax(4);
    pdLab.setBRange(pdRange);
    TestCase.assertEquals(-1f, pdLab.getARange().getMin());
    TestCase.assertEquals(2f, pdLab.getARange().getMax());
    TestCase.assertEquals(3f, pdLab.getBRange().getMin());
    TestCase.assertEquals(4f, pdLab.getBRange().getMax());
    PDTristimulus pdTristimulus = new PDTristimulus();
    pdTristimulus.setX(5);
    pdTristimulus.setY(6);
    pdTristimulus.setZ(7);
    pdLab.setWhitePoint(pdTristimulus);
    pdTristimulus = new PDTristimulus();
    pdTristimulus.setX(8);
    pdTristimulus.setY(9);
    pdTristimulus.setZ(10);
    pdLab.setBlackPoint(pdTristimulus);
    TestCase.assertEquals(5f, pdLab.getWhitepoint().getX());
    TestCase.assertEquals(6f, pdLab.getWhitepoint().getY());
    TestCase.assertEquals(7f, pdLab.getWhitepoint().getZ());
    TestCase.assertEquals(8f, pdLab.getBlackPoint().getX());
    TestCase.assertEquals(9f, pdLab.getBlackPoint().getY());
    TestCase.assertEquals(10f, pdLab.getBlackPoint().getZ());
    TestCase.assertTrue(Arrays.equals(new float[] { 0, 0, 3 }, pdLab.getInitialColor().getComponents()));
  }

}
