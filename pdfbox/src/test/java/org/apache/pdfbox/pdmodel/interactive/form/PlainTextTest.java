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
package org.apache.pdfbox.pdmodel.interactive.form;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test if a sequence of linebreak/paragraph characters produces the same number
 * of paragraphs as Adobe Acrobat produces when setting the value via
 * JavaScript.
 *
 */
public class PlainTextTest {
  @Test
  public void characterCR() {
    final PlainText text = new PlainText("CR\rCR");
    Assert.assertEquals(2, text.getParagraphs().size());
  }

  @Test
  public void characterLF() {
    final PlainText text = new PlainText("LF\nLF");
    Assert.assertEquals(2, text.getParagraphs().size());
  }

  @Test
  public void characterCRLF() {
    final PlainText text = new PlainText("CRLF\r\nCRLF");
    Assert.assertEquals(2, text.getParagraphs().size());
  }

  @Test
  public void characterLFCR() {
    final PlainText text = new PlainText("LFCR\n\rLFCR");
    Assert.assertEquals(3, text.getParagraphs().size());
  }

  @Test
  public void characterUnicodeLinebreak() {
    final PlainText text = new PlainText("linebreak\u2028linebreak");
    Assert.assertEquals(2, text.getParagraphs().size());
  }

  @Test
  public void characterUnicodeParagraphbreak() {
    final PlainText text = new PlainText("paragraphbreak\u2029paragraphbreak");
    Assert.assertEquals(2, text.getParagraphs().size());
  }

}
