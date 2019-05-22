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

import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

/**
 * This class tests the extraction of document-level metadata.
 * 
 * @author Neil McErlean
 * @since 1.3.0
 */
public class TestPDDocumentInformation extends TestCase {

  public void testMetadataExtraction() throws Exception {
    // This document has been selected for this test as it contains custom metadata.
    try (PDDocument doc = PDDocument.load(new File("src/test/resources/input/hello3.pdf"))) {
      final PDDocumentInformation info = doc.getDocumentInformation();

      TestCase.assertEquals("Wrong author", "Brian Carrier", info.getAuthor());
      TestCase.assertNotNull("Wrong creationDate", info.getCreationDate());
      TestCase.assertEquals("Wrong creator", "Acrobat PDFMaker 8.1 for Word", info.getCreator());
      TestCase.assertNull("Wrong keywords", info.getKeywords());
      TestCase.assertNotNull("Wrong modificationDate", info.getModificationDate());
      TestCase.assertEquals("Wrong producer", "Acrobat Distiller 8.1.0 (Windows)", info.getProducer());
      TestCase.assertNull("Wrong subject", info.getSubject());
      TestCase.assertNull("Wrong trapped", info.getTrapped());

      final List<String> expectedMetadataKeys = Arrays.asList("CreationDate", "Author", "Creator", "Producer",
          "ModDate", "Company", "SourceModified", "Title");
      TestCase.assertEquals("Wrong metadata key count", expectedMetadataKeys.size(), info.getMetadataKeys().size());
      for (final String key : expectedMetadataKeys) {
        TestCase.assertTrue("Missing metadata key:" + key, info.getMetadataKeys().contains(key));
      }

      // Custom metadata fields.
      TestCase.assertEquals("Wrong company", "Basis Technology Corp.", info.getCustomMetadataValue("Company"));
      TestCase.assertEquals("Wrong sourceModified", "D:20080819181502", info.getCustomMetadataValue("SourceModified"));
    }
  }

  /**
   * PDFBOX-3068: test that indirect /Title element of /Info entry can be found.
   * 
   * @throws Exception
   */
  public void testPDFBox3068() throws Exception {
    try (PDDocument doc = PDDocument.load(TestPDDocumentInformation.class.getResourceAsStream("PDFBOX-3068.pdf"))) {
      final PDDocumentInformation documentInformation = doc.getDocumentInformation();
      TestCase.assertEquals("Title", documentInformation.getTitle());
    }
  }

}
