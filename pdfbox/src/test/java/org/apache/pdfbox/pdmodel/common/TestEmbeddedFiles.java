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
package org.apache.pdfbox.pdmodel.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.junit.Test;

import junit.framework.TestCase;

public class TestEmbeddedFiles extends TestCase {
  @Test
  public void testNullEmbeddedFile() throws IOException {
    PDEmbeddedFile embeddedFile = null;
    boolean ok = false;
    try {
      final PDDocument doc = PDDocument
          .load(TestEmbeddedFiles.class.getResourceAsStream("null_PDComplexFileSpecification.pdf"));

      final PDDocumentCatalog catalog = doc.getDocumentCatalog();
      final PDDocumentNameDictionary names = catalog.getNames();
      TestCase.assertEquals("expected two files", 2, names.getEmbeddedFiles().getNames().size());
      final PDEmbeddedFilesNameTreeNode embeddedFiles = names.getEmbeddedFiles();

      PDComplexFileSpecification spec = embeddedFiles.getNames().get("non-existent-file.docx");

      if (spec != null) {
        embeddedFile = spec.getEmbeddedFile();
        ok = true;
      }
      // now test for actual attachment
      spec = embeddedFiles.getNames().get("My first attachment");
      TestCase.assertNotNull("one attachment actually exists", spec);
      TestCase.assertEquals("existing file length", 17660, spec.getEmbeddedFile().getLength());
      spec = embeddedFiles.getNames().get("non-existent-file.docx");
    } catch (final NullPointerException e) {
      TestCase.assertNotNull("null pointer exception", null);
    }
    TestCase.assertTrue("Was able to get file without exception", ok);
    TestCase.assertNull("EmbeddedFile was correctly null", embeddedFile);
  }

  @Test
  public void testOSSpecificAttachments() throws IOException {
    PDEmbeddedFile nonOSFile = null;
    PDEmbeddedFile macFile = null;
    PDEmbeddedFile dosFile = null;
    PDEmbeddedFile unixFile = null;

    final PDDocument doc = PDDocument
        .load(TestEmbeddedFiles.class.getResourceAsStream("testPDF_multiFormatEmbFiles.pdf"));

    final PDDocumentCatalog catalog = doc.getDocumentCatalog();
    final PDDocumentNameDictionary names = catalog.getNames();
    final PDEmbeddedFilesNameTreeNode treeNode = names.getEmbeddedFiles();
    final List<PDNameTreeNode<PDComplexFileSpecification>> kids = treeNode.getKids();
    for (final PDNameTreeNode<PDComplexFileSpecification> kid : kids) {
      final Map<String, PDComplexFileSpecification> tmpNames = kid.getNames();
      final COSObjectable obj = tmpNames.get("My first attachment");

      final PDComplexFileSpecification spec = (PDComplexFileSpecification) obj;
      nonOSFile = spec.getEmbeddedFile();
      macFile = spec.getEmbeddedFileMac();
      dosFile = spec.getEmbeddedFileDos();
      unixFile = spec.getEmbeddedFileUnix();
    }

    TestCase.assertTrue("non os specific",
        byteArrayContainsLC("non os specific", nonOSFile.toByteArray(), "ISO-8859-1"));

    TestCase.assertTrue("mac", byteArrayContainsLC("mac embedded", macFile.toByteArray(), "ISO-8859-1"));

    TestCase.assertTrue("dos", byteArrayContainsLC("dos embedded", dosFile.toByteArray(), "ISO-8859-1"));

    TestCase.assertTrue("unix", byteArrayContainsLC("unix embedded", unixFile.toByteArray(), "ISO-8859-1"));

  }

  private boolean byteArrayContainsLC(final String target, final byte[] bytes, final String encoding)
      throws UnsupportedEncodingException {
    final String s = new String(bytes, encoding);
    return s.toLowerCase().contains(target);
  }
}
