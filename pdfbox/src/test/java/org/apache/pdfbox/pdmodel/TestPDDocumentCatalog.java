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
package org.apache.pdfbox.pdmodel;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test PDDocument Catalog functionality.
 *
 */
public class TestPDDocumentCatalog {

  /**
   * Test getPageLabels().
   * 
   * Test case for
   * <a href="https://issues.apache.org/jira/browse/PDFBOX-90" >PDFBOX-90</a> -
   * Support explicit retrieval of page labels.
   * 
   * @throws IOException in case the document can not be parsed.
   */
  @Test
  public void retrievePageLabels() throws IOException {
    PDDocument doc = null;
    try {
      doc = PDDocument.load(TestPDDocumentCatalog.class.getResourceAsStream("test_pagelabels.pdf"));
      final PDDocumentCatalog cat = doc.getDocumentCatalog();
      final String[] labels = cat.getPageLabels().getLabelsByPageIndices();
      Assert.assertEquals(12, labels.length);
      Assert.assertEquals("A1", labels[0]);
      Assert.assertEquals("A2", labels[1]);
      Assert.assertEquals("A3", labels[2]);
      Assert.assertEquals("i", labels[3]);
      Assert.assertEquals("ii", labels[4]);
      Assert.assertEquals("iii", labels[5]);
      Assert.assertEquals("iv", labels[6]);
      Assert.assertEquals("v", labels[7]);
      Assert.assertEquals("vi", labels[8]);
      Assert.assertEquals("vii", labels[9]);
      Assert.assertEquals("Appendix I", labels[10]);
      Assert.assertEquals("Appendix II", labels[11]);
    } finally {
      if (doc != null) {
        doc.close();
      }
    }
  }

  /**
   * Test page labels for malformed PDF.
   * 
   * Test case for
   * <a href="https://issues.apache.org/jira/browse/PDFBOX-900" >PDFBOX-900</a> -
   * Handle malformed PDFs
   * 
   * @throws IOException in case the document can not be parsed.
   */
  @Test
  public void retrievePageLabelsOnMalformedPdf() throws IOException {
    PDDocument doc = null;
    try {
      doc = PDDocument.load(TestPDDocumentCatalog.class.getResourceAsStream("badpagelabels.pdf"));
      final PDDocumentCatalog cat = doc.getDocumentCatalog();
      // getLabelsByPageIndices() should not throw an exception
      cat.getPageLabels().getLabelsByPageIndices();
    } finally {
      if (doc != null) {
        doc.close();
      }
    }
  }

  /**
   * Test getNumberOfPages().
   * 
   * Test case for
   * <a href="https://issues.apache.org/jira/browse/PDFBOX-911" >PDFBOX-911</a> -
   * Method PDDocument.getNumberOfPages() returns wrong number of pages
   * 
   * @throws IOException in case the document can not be parsed.
   */
  @Test
  public void retrieveNumberOfPages() throws IOException {
    PDDocument doc = null;
    try {
      doc = PDDocument.load(TestPDDocumentCatalog.class.getResourceAsStream("test.unc.pdf"));
      Assert.assertEquals(4, doc.getNumberOfPages());
    } finally {
      if (doc != null) {
        doc.close();
      }
    }
  }

  /**
   * Test OutputIntents functionality.
   * 
   * Test case for <a
   * https://issues.apache.org/jira/browse/PDFBOX-2687">PDFBOX-2687</a>
   * ClassCastException when trying to get OutputIntents or add to it.
   * 
   * @throws IOException in case the document can not be parsed.
   */
  @Test
  public void handleOutputIntents() throws IOException {
    PDDocument doc = null;
    InputStream colorProfile = null;
    try {

      doc = PDDocument.load(TestPDDocumentCatalog.class.getResourceAsStream("test.unc.pdf"));
      final PDDocumentCatalog catalog = doc.getDocumentCatalog();

      // retrieve OutputIntents
      List<PDOutputIntent> outputIntents = catalog.getOutputIntents();
      Assert.assertTrue(outputIntents.isEmpty());

      // add an OutputIntent
      colorProfile = TestPDDocumentCatalog.class.getResourceAsStream("sRGB.icc");
      // create output intent
      final PDOutputIntent oi = new PDOutputIntent(doc, colorProfile);
      oi.setInfo("sRGB IEC61966-2.1");
      oi.setOutputCondition("sRGB IEC61966-2.1");
      oi.setOutputConditionIdentifier("sRGB IEC61966-2.1");
      oi.setRegistryName("http://www.color.org");
      doc.getDocumentCatalog().addOutputIntent(oi);

      // retrieve OutputIntents
      outputIntents = catalog.getOutputIntents();
      Assert.assertEquals(1, outputIntents.size());

      // set OutputIntents
      catalog.setOutputIntents(outputIntents);
      outputIntents = catalog.getOutputIntents();
      Assert.assertEquals(1, outputIntents.size());

    } finally {
      if (doc != null) {
        doc.close();
      }

      if (colorProfile != null) {
        colorProfile.close();
      }
    }
  }

  @Test
  public void handleBooleanInOpenAction() throws IOException {
    // PDFBOX-3772 -- allow for COSBoolean
    final PDDocument doc = new PDDocument();
    doc.getDocumentCatalog().getCOSObject().setBoolean(COSName.OPEN_ACTION, false);
    Assert.assertNull(doc.getDocumentCatalog().getOpenAction());
  }
}
