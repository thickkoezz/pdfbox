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
import java.util.Arrays;

import org.apache.pdfbox.pdfwriter.COSWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This will test all of the filters in the PDFBox system.
 *
 * Ben Litchfield
 */
public class TestCOSString extends TestCOSBase {
  private static final String ESC_CHAR_STRING = "( test#some) escaped< \\chars>!~1239857 ";
  private static final String ESC_CHAR_STRING_PDF_FORMAT = "\\( test#some\\) escaped< \\\\chars>!~1239857 ";

  /**
   * This will get the suite of test that this class holds.
   *
   * @return All of the tests that this class holds.
   */
  public static Test suite() {
    return new TestSuite(TestCOSString.class);
  }

  @Override
  public void setUp() {
    testCOSBase = new COSString("test cos string");
  }

  /**
   * infamous main method.
   *
   * @param args The command line arguments.
   */
  public static void main(final String[] args) {
    final String[] arg = { TestCOSString.class.getName() };
    junit.textui.TestRunner.main(arg);
  }

  /**
   * Test setForceHexForm() and setForceLiteralForm() - tests these two methods do
   * enforce the different String output forms within PDF.
   */
  public void testSetForceHexLiteralForm() {
    final String inputString = "Test with a text and a few numbers 1, 2 and 3";
    final String pdfHex = "<" + createHex(inputString) + ">";
    final COSString cosStr = new COSString(inputString);
    cosStr.setForceHexForm(true);
    writePDFTests(pdfHex, cosStr);

    final COSString escStr = new COSString(TestCOSString.ESC_CHAR_STRING);
    writePDFTests("(" + TestCOSString.ESC_CHAR_STRING_PDF_FORMAT + ")", escStr);
    escStr.setForceHexForm(true);
    // Escape characters not escaped in hex version
    writePDFTests("<" + createHex(TestCOSString.ESC_CHAR_STRING) + ">", escStr);
  }

  /**
   * Helper method for testing writePDF().
   * 
   * @param expected the String expected when writePDF() is invoked
   * @param testSubj the test subject
   */
  private void writePDFTests(final String expected, final COSString testSubj) {
    final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    try {
      COSWriter.writeString(testSubj, outStream);
    } catch (final IOException e) {
      TestCase.fail("IOException: " + e.getMessage());
    }
    TestCase.assertEquals(expected, outStream.toString());
  }

  /**
   * Test parseHex() - tests that the proper String is created from a hex string
   * input.
   */
  public void testFromHex() {
    final String expected = "Quick and simple test";
    final String hexForm = createHex(expected);
    try {
      final COSString test1 = COSString.parseHex(hexForm);
      writePDFTests("(" + expected + ")", test1);
      final COSString test2 = COSString.parseHex(createHex(TestCOSString.ESC_CHAR_STRING));
      writePDFTests("(" + TestCOSString.ESC_CHAR_STRING_PDF_FORMAT + ")", test2);
    } catch (final IOException e) {
      TestCase.fail("IOException thrown: " + e.getMessage());
    }
    try {
      COSString.parseHex(hexForm + "xx");
      TestCase.fail("Should have thrown an IOException here");
    } catch (final IOException e) {
      // PASS
    }
  }

  private String createHex(final String str) {
    final StringBuilder sb = new StringBuilder();
    for (final char c : str.toCharArray()) {
      sb.append(Integer.toString(c, 16));
    }
    return sb.toString().toUpperCase();
  }

  /**
   * Tests getHex() - ensure the hex String returned is properly formatted.
   */
  public void testGetHex() {
    final String expected = "Test subject for testing getHex";
    final COSString test1 = new COSString(expected);
    final String hexForm = createHex(expected);
    TestCase.assertEquals(hexForm, test1.toHexString());
    final COSString escCS = new COSString(TestCOSString.ESC_CHAR_STRING);
    // Not sure whether the escaped characters should be escaped or not, presumably
    // since
    // writePDF() gives you the proper formatted text, getHex() should ONLY convert
    // to hex.
    TestCase.assertEquals(createHex(TestCOSString.ESC_CHAR_STRING), escCS.toHexString());
  }

  /**
   * Test testGetString() - ensure getString() are returned in the correct format.
   */
  public void testGetString() {
    try {
      String testStr = "Test subject for getString()";
      final COSString test1 = new COSString(testStr);
      TestCase.assertEquals(testStr, test1.getString());

      final COSString hexStr = COSString.parseHex(createHex(testStr));
      TestCase.assertEquals(testStr, hexStr.getString());

      final COSString escapedString = new COSString(TestCOSString.ESC_CHAR_STRING);
      TestCase.assertEquals(TestCOSString.ESC_CHAR_STRING, escapedString.getString());

      testStr = "Line1\nLine2\nLine3\n";
      final COSString lineFeedString = new COSString(testStr);
      TestCase.assertEquals(testStr, lineFeedString.getString());
    } catch (final IOException e) {
      TestCase.fail("IOException thrown: " + e.getMessage());
    }
  }

  /**
   * Test getBytes() - again not much to test, just ensure the proper byte array
   * is returned.
   */
  public void testGetBytes() {
    final COSString str = new COSString(TestCOSString.ESC_CHAR_STRING);
    testByteArrays(TestCOSString.ESC_CHAR_STRING.getBytes(), str.getBytes());
  }

  /**
   * Tests writePDF() - tests that the string is in PDF format.
   */
  public void testWritePDF() {
    // This has been tested quite thorougly above but do a couple tests anyway
    final COSString testSubj = new COSString(TestCOSString.ESC_CHAR_STRING);
    writePDFTests("(" + TestCOSString.ESC_CHAR_STRING_PDF_FORMAT + ")", testSubj);
    final String textString = "This is just an arbitrary piece of text for testing";
    final COSString testSubj2 = new COSString(textString);
    writePDFTests("(" + textString + ")", testSubj2);
  }

  /**
   * This will test all of the filters in the system.
   *
   * @throws IOException If there is an exception while encoding.
   */
  public void testUnicode() throws IOException {
    final String theString = "\u4e16";
    final COSString string = new COSString(theString);
    TestCase.assertTrue(string.getString().equals(theString));

    final String textAscii = "This is some regular text. It should all be expressable in ASCII";
    /** En français où les choses sont accentués. En español, así */
    final String text8Bit = "En fran\u00e7ais o\u00f9 les choses sont accentu\u00e9s. En espa\u00f1ol, as\u00ed";
    /** をクリックしてく */
    final String textHighBits = "\u3092\u30af\u30ea\u30c3\u30af\u3057\u3066\u304f";

    // Testing the getString method
    final COSString stringAscii = new COSString(textAscii);
    TestCase.assertEquals(stringAscii.getString(), textAscii);

    final COSString string8Bit = new COSString(text8Bit);
    TestCase.assertEquals(string8Bit.getString(), text8Bit);

    final COSString stringHighBits = new COSString(textHighBits);
    TestCase.assertEquals(stringHighBits.getString(), textHighBits);

    // Testing the getBytes method
    // The first two strings should be stored as ISO-8859-1 because they only
    // contain chars in the range 0..255
    TestCase.assertEquals(textAscii, new String(stringAscii.getBytes(), "ISO-8859-1"));
    // likewise for the 8bit characters.
    TestCase.assertEquals(text8Bit, new String(string8Bit.getBytes(), "ISO-8859-1"));

    // The japanese text contains high bits so must be stored as big endian UTF-16
    TestCase.assertEquals(textHighBits, new String(stringHighBits.getBytes(), "UnicodeBig"));

    // Test the writePDF method to ensure that the Strings are correct when written
    // into PDF.
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    COSWriter.writeString(stringAscii, out);
    TestCase.assertEquals("(" + textAscii + ")", new String(out.toByteArray(), "ASCII"));

    out.reset();
    COSWriter.writeString(string8Bit, out);
    StringBuffer hex = new StringBuffer();
    for (final char c : text8Bit.toCharArray()) {
      hex.append(Integer.toHexString(c).toUpperCase());
    }
    TestCase.assertEquals("<" + hex.toString() + ">", new String(out.toByteArray(), "ASCII"));

    out.reset();
    COSWriter.writeString(stringHighBits, out);
    hex = new StringBuffer();
    hex.append("FEFF"); // Byte Order Mark
    for (final char c : textHighBits.toCharArray()) {
      hex.append(Integer.toHexString(c).toUpperCase());
    }
    TestCase.assertEquals("<" + hex.toString() + ">", new String(out.toByteArray(), "ASCII"));
  }

  @Override
  public void testAccept() throws IOException {
    final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    final ICOSVisitor visitor = new COSWriter(outStream);
    final COSString testSubj = new COSString(TestCOSString.ESC_CHAR_STRING);
    testSubj.accept(visitor);
    TestCase.assertEquals("(" + TestCOSString.ESC_CHAR_STRING_PDF_FORMAT + ")", outStream.toString());
    outStream.reset();
    testSubj.setForceHexForm(true);
    testSubj.accept(visitor);
    TestCase.assertEquals("<" + createHex(TestCOSString.ESC_CHAR_STRING) + ">", outStream.toString());
  }

  /**
   * Tests equals(Object) - ensure that the Object.equals() contract is obeyed.
   */
  public void testEquals() {
    // Check all these several times for consistency
    for (int i = 0; i < 10; i++) {
      // Reflexive
      final COSString x1 = new COSString("Test");
      TestCase.assertTrue(x1.equals(x1));

      // Symmetry i.e. if x == y then y == x
      final COSString y1 = new COSString("Test");
      TestCase.assertTrue(x1.equals(y1));
      TestCase.assertTrue(y1.equals(x1));
      final COSString x2 = new COSString("Test");
      x2.setForceHexForm(true);
      // also if x != y then y != x
      TestCase.assertFalse(x1.equals(x2));
      TestCase.assertFalse(x2.equals(x1));

      // Transitive if x == y && y == z then x == z
      final COSString z1 = new COSString("Test");
      TestCase.assertTrue(x1.equals(y1));
      TestCase.assertTrue(y1.equals(z1));
      TestCase.assertTrue(x1.equals(z1));
      // Test the negative as well if x1 == y1 && y1 != x2 then x1 != x2
      TestCase.assertTrue(x1.equals(y1));
      TestCase.assertFalse(y1.equals(x2));
      TestCase.assertFalse(x1.equals(x2));

      // Non-nullity
      TestCase.assertFalse(x1 == null);
      TestCase.assertFalse(y1 == null);
      TestCase.assertFalse(z1 == null);
      TestCase.assertFalse(x2 == null);
    }
  }

  /**
   * Test hashCode() - tests that the Object.hashCode() contract is obeyed.
   */
  public void testHashCode() {
    final COSString str1 = new COSString("Test1");
    final COSString str2 = new COSString("Test2");
    TestCase.assertFalse(str1.hashCode() == str2.hashCode());
    final COSString str3 = new COSString("Test1");
    TestCase.assertTrue(str1.hashCode() == str3.hashCode());
    str3.setForceHexForm(true);
    TestCase.assertFalse(str1.hashCode() == str3.hashCode());
  }

  /**
   * Test testCompareFromHexString() - tests that Strings created from hex compare
   * correctly (PDFBOX-2401)
   */
  public void testCompareFromHexString() throws IOException {
    final COSString test1 = COSString.parseHex("000000FF000000");
    final COSString test2 = COSString.parseHex("000000FF00FFFF");
    TestCase.assertEquals(test1, test1);
    TestCase.assertEquals(test2, test2);
    TestCase.assertFalse(test1.toHexString().equals(test2.toHexString()));
    TestCase.assertFalse(Arrays.equals(test1.getBytes(), test2.getBytes()));
    TestCase.assertFalse(test1.equals(test2));
    TestCase.assertFalse(test2.equals(test1));
    TestCase.assertFalse(test1.getString().equals(test2.getString()));
  }

  /**
   * PDFBOX-3881: Test that if String has only the BOM, that it be an empty
   * string.
   * 
   * @throws IOException
   */
  public void testEmptyStringWithBOM() throws IOException {
    TestCase.assertTrue(COSString.parseHex("FEFF").getString().isEmpty());
    TestCase.assertTrue(COSString.parseHex("FFFE").getString().isEmpty());
  }
}
