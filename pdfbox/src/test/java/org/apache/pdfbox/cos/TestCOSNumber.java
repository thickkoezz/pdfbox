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

import junit.framework.TestCase;

/**
 * Test class for {@link COSNumber}
 */
public abstract class TestCOSNumber extends TestCOSBase {
  /**
   * Test floatValue() - test that the correct float value is returned.
   */
  public abstract void testFloatValue();

  /**
   * Test doubleValue() - test that the correct double value is returned.
   */
  public abstract void testDoubleValue();

  /**
   * Test intValue() - test that the correct int value is returned.
   */
  public abstract void testIntValue();

  /**
   * Test longValue() - test that the correct long value is returned.
   */
  public abstract void testLongValue();

  /**
   * Tests get() - tests a static constructor for COSNumber classes.
   */
  public void testGet() {
    try {
      // Ensure the basic static numbers are recognized
      TestCase.assertEquals(COSInteger.ZERO, COSNumber.get("0"));
      TestCase.assertEquals(COSInteger.ONE, COSNumber.get("1"));
      TestCase.assertEquals(COSInteger.TWO, COSNumber.get("2"));
      TestCase.assertEquals(COSInteger.THREE, COSNumber.get("3"));
      // Test some arbitrary ints
      TestCase.assertEquals(COSInteger.get(100), COSNumber.get("100"));
      TestCase.assertEquals(COSInteger.get(256), COSNumber.get("256"));
      TestCase.assertEquals(COSInteger.get(-1000), COSNumber.get("-1000"));
      TestCase.assertEquals(COSInteger.get(2000), COSNumber.get("+2000"));
      // Some arbitrary floats
      TestCase.assertEquals(new COSFloat(1.1f), COSNumber.get("1.1"));
      TestCase.assertEquals(new COSFloat(100f), COSNumber.get("100.0"));
      TestCase.assertEquals(new COSFloat(-100.001f), COSNumber.get("-100.001"));
      // according to the specs the exponential shall not be used
      // but obviously there some
      TestCase.assertNotNull(COSNumber.get("-2e-006"));
      TestCase.assertNotNull(COSNumber.get("-8e+05"));
      try {
        TestCase.assertEquals("Null Value...", COSNumber.get(null));
        TestCase.fail("Failed to throw a NullPointerException");
      } catch (final NullPointerException e) {
        // PASS
      }

    } catch (final IOException e) {
      TestCase.fail("Failed to convert a number " + e.getMessage());
    }
  }
}
