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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.encoding.DictionaryEncoding;
import org.apache.pdfbox.pdmodel.font.encoding.MacRomanEncoding;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;
import org.apache.pdfbox.text.PDFTextStripper;

import junit.framework.TestCase;

/**
 * Tests font encoding.
 *
 */
public class TestFontEncoding extends TestCase {
  /**
   * Test the add method of a font encoding.
   */
  public void testAdd() throws Exception {
    // see PDFDBOX-3332
    int codeForSpace = WinAnsiEncoding.INSTANCE.getNameToCodeMap().get("space");
    TestCase.assertEquals(32, codeForSpace);

    codeForSpace = MacRomanEncoding.INSTANCE.getNameToCodeMap().get("space");
    TestCase.assertEquals(32, codeForSpace);
  }

  public void testOverwrite() throws Exception {
    // see PDFDBOX-3332
    final COSDictionary dictEncodingDict = new COSDictionary();
    dictEncodingDict.setItem(COSName.TYPE, COSName.ENCODING);
    dictEncodingDict.setItem(COSName.BASE_ENCODING, COSName.WIN_ANSI_ENCODING);
    final COSArray differences = new COSArray();
    differences.add(COSInteger.get(32));
    differences.add(COSName.getPDFName("a"));
    dictEncodingDict.setItem(COSName.DIFFERENCES, differences);
    final DictionaryEncoding dictEncoding = new DictionaryEncoding(dictEncodingDict, false, null);
    TestCase.assertNull(dictEncoding.getNameToCodeMap().get("space"));
    TestCase.assertEquals(32, dictEncoding.getNameToCodeMap().get("a").intValue());
  }

  /**
   * PDFBOX-3826: Some unicodes are reached by several names in glyphlist.txt,
   * e.g. tilde and ilde.
   *
   * @throws IOException
   */
  public void testPDFBox3884() throws IOException {
    PDDocument doc = new PDDocument();
    final PDPage page = new PDPage();
    doc.addPage(page);
    final PDPageContentStream cs = new PDPageContentStream(doc, page);
    cs.setFont(PDType1Font.HELVETICA, 20);
    cs.beginText();
    cs.newLineAtOffset(100, 700);
    // first tilde is "asciitilde" (from the keyboard), 2nd tilde is "tilde"
    // using ˜ would bring IllegalArgumentException prior to bugfix
    cs.showText("~˜");
    cs.endText();
    cs.close();
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    doc.save(baos);
    doc.close();

    // verify
    doc = PDDocument.load(baos.toByteArray());
    final PDFTextStripper stripper = new PDFTextStripper();
    final String text = stripper.getText(doc);
    TestCase.assertEquals("~˜", text.trim());
    doc.close();
  }
}
