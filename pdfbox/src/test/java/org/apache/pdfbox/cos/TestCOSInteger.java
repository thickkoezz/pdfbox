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
import java.io.IOException;

import org.apache.pdfbox.pdfwriter.COSWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A test case for COSInteger
 *
 * @author Koch
 */
public class TestCOSInteger extends TestCOSNumber {
  @Override
  public void setUp() {
    try {
      testCOSBase = COSNumber.get("0");
    } catch (final IOException e) {
      TestCase.fail("Failed to create a COSNumber in setUp()");
    }
  }

  /**
   * Tests equals() - ensures that the Object.equals() contract is obeyed. These
   * are tested over a range of arbitrary values to ensure Consistency,
   * Reflexivity, Symmetry, Transitivity and non-nullity.
   */
  public void testEquals() {
    // Consistency
    for (int i = -1000; i < 3000; i += 200) {
      final COSInteger test1 = COSInteger.get(i);
      final COSInteger test2 = COSInteger.get(i);
      final COSInteger test3 = COSInteger.get(i);
      // Reflexive (x == x)
      TestCase.assertTrue(test1.equals(test1));
      // Symmetric is preserved ( x==y then y===x)
      TestCase.assertTrue(test2.equals(test1));
      TestCase.assertTrue(test1.equals(test2));
      // Transitive (if x==y && y==z then x===z)
      TestCase.assertTrue(test1.equals(test2));
      TestCase.assertTrue(test2.equals(test3));
      TestCase.assertTrue(test1.equals(test3));
      // Non-nullity
      TestCase.assertFalse(test1 == null);
      TestCase.assertFalse(test2 == null);
      TestCase.assertFalse(test3 == null);

      final COSInteger test4 = COSInteger.get(i + 1);
      TestCase.assertFalse(test4.equals(test1));
    }
  }

  /**
   * Tests hashCode() - ensures that the Object.hashCode() contract is obeyed over
   * a range of arbitrary values.
   */
  public void testHashCode() {
    for (int i = -1000; i < 3000; i += 200) {
      final COSInteger test1 = COSInteger.get(i);
      final COSInteger test2 = COSInteger.get(i);
      TestCase.assertEquals(test1.hashCode(), test2.hashCode());

      final COSInteger test3 = COSInteger.get(i + 1);
      TestCase.assertFalse(test3.hashCode() == test1.hashCode());
    }
  }

  @Override
  public void testFloatValue() {
    for (int i = -1000; i < 3000; i += 200) {
      TestCase.assertEquals((float) i, COSInteger.get(i).floatValue());
    }
  }

  @Override
  public void testDoubleValue() {
    for (int i = -1000; i < 3000; i += 200) {
      TestCase.assertEquals((double) i, COSInteger.get(i).doubleValue());
    }
  }

  @Override
  public void testIntValue() {
    for (int i = -1000; i < 3000; i += 200) {
      TestCase.assertEquals(i, COSInteger.get(i).intValue());
    }
  }

  @Override
  public void testLongValue() {
    for (int i = -1000; i < 3000; i += 200) {
      TestCase.assertEquals(i, COSInteger.get(i).longValue());
    }
  }

  @Override
  public void testAccept() {
    final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    final COSWriter visitor = new COSWriter(outStream);
    int index = 0;
    try {
      for (int i = -1000; i < 3000; i += 200) {
        index = i;
        final COSInteger cosInt = COSInteger.get(i);
        cosInt.accept(visitor);
        testByteArrays(String.valueOf(i).getBytes("ISO-8859-1"), outStream.toByteArray());
        outStream.reset();
      }
    } catch (final Exception e) {
      TestCase.fail("Failed to write " + index + " exception: " + e.getMessage());
    }
  }

  /**
   * Tests writePDF() - this method takes an {@link java.io.OutputStream} and
   * writes this object to it.
   */
  public void testWritePDF() {
    final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    int index = 0;
    try {
      for (int i = -1000; i < 3000; i += 200) {
        index = i;
        final COSInteger cosInt = COSInteger.get(i);
        cosInt.writePDF(outStream);
        testByteArrays(String.valueOf(i).getBytes("ISO-8859-1"), outStream.toByteArray());
        outStream.reset();
      }
    } catch (final Exception e) {
      TestCase.fail("Failed to write " + index + " exception: " + e.getMessage());
    }
  }

  /**
   * This will get the suite of test that this class holds.
   *
   * @return All of the tests that this class holds.
   */
  public static Test suite() {
    return new TestSuite(TestCOSInteger.class);
  }
}
