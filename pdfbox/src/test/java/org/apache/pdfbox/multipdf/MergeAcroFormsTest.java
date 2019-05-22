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
package org.apache.pdfbox.multipdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.multipdf.PDFMergerUtility.AcroFormMergeMode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test merging different PDFs with AcroForms.
 *
 *
 */
public class MergeAcroFormsTest {
  private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/multipdf");
  private static final File OUT_DIR = new File("target/test-output/merge/");
  private static final File TARGET_PDF_DIR = new File("target/pdfs");

  @Before
  public void setUp() {
    MergeAcroFormsTest.OUT_DIR.mkdirs();
  }

  /*
   * Test LegacyMode merge
   */
  @Test
  public void testLegacyModeMerge() throws IOException {
    final PDFMergerUtility merger = new PDFMergerUtility();
    final File toBeMerged = new File(MergeAcroFormsTest.IN_DIR, "AcroFormForMerge.pdf");
    final File pdfOutput = new File(MergeAcroFormsTest.OUT_DIR, "PDFBoxLegacyMerge-SameMerged.pdf");
    merger.setDestinationFileName(pdfOutput.getAbsolutePath());
    merger.addSource(toBeMerged);
    merger.addSource(toBeMerged);
    merger.mergeDocuments(null);
    merger.setAcroFormMergeMode(AcroFormMergeMode.PDFBOX_LEGACY_MODE);

    try (
        PDDocument compliantDocument = PDDocument
            .load(new File(MergeAcroFormsTest.IN_DIR, "PDFBoxLegacyMerge-SameMerged.pdf"));
        PDDocument toBeCompared = PDDocument
            .load(new File(MergeAcroFormsTest.OUT_DIR, "PDFBoxLegacyMerge-SameMerged.pdf"))) {
      final PDAcroForm compliantAcroForm = compliantDocument.getDocumentCatalog().getAcroForm();
      final PDAcroForm toBeComparedAcroForm = toBeCompared.getDocumentCatalog().getAcroForm();

      Assert.assertEquals("There shall be the same number of root fields", compliantAcroForm.getFields().size(),
          toBeComparedAcroForm.getFields().size());

      for (final PDField compliantField : compliantAcroForm.getFieldTree()) {
        Assert.assertNotNull("There shall be a field with the same FQN",
            toBeComparedAcroForm.getField(compliantField.getFullyQualifiedName()));
        final PDField toBeComparedField = toBeComparedAcroForm.getField(compliantField.getFullyQualifiedName());
        compareFieldProperties(compliantField, toBeComparedField);
      }

      for (final PDField toBeComparedField : toBeComparedAcroForm.getFieldTree()) {
        Assert.assertNotNull("There shall be a field with the same FQN",
            compliantAcroForm.getField(toBeComparedField.getFullyQualifiedName()));
        final PDField compliantField = compliantAcroForm.getField(toBeComparedField.getFullyQualifiedName());
        compareFieldProperties(toBeComparedField, compliantField);
      }
    }
  }

  private void compareFieldProperties(final PDField sourceField, final PDField toBeComapredField) {
    // List of keys for comparison
    // Don't include too complex properties such as AP as this will fail the test
    // because
    // of a stack overflow when
    final String[] keys = { "FT", "T", "TU", "TM", "Ff", "V", "DV", "Opts", "TI", "I", "Rect", "DA", };

    final COSDictionary sourceFieldCos = sourceField.getCOSObject();
    final COSDictionary toBeComparedCos = toBeComapredField.getCOSObject();

    for (final String key : keys) {
      final COSBase sourceBase = sourceFieldCos.getDictionaryObject(key);
      final COSBase toBeComparedBase = toBeComparedCos.getDictionaryObject(key);

      if (sourceBase != null) {
        Assert.assertEquals("The content of the field properties shall be the same", sourceBase.toString(),
            toBeComparedBase.toString());
      } else {
        Assert.assertNull("If the source property is null the compared property shall be null too", toBeComparedBase);
      }
    }
  }

  /*
   * PDFBOX-1031 Ensure that after merging the PDFs there is an Annots entry per
   * page.
   */
  @Test
  public void testAnnotsEntry() throws IOException {

    // Merge the PDFs form PDFBOX-1031
    final PDFMergerUtility merger = new PDFMergerUtility();

    final File f1 = new File(MergeAcroFormsTest.TARGET_PDF_DIR, "PDFBOX-1031-1.pdf");
    final File f2 = new File(MergeAcroFormsTest.TARGET_PDF_DIR, "PDFBOX-1031-2.pdf");
    final File pdfOutput = new File(MergeAcroFormsTest.OUT_DIR, "PDFBOX-1031.pdf");

    try (InputStream is1 = new FileInputStream(f1); InputStream is2 = new FileInputStream(f2)) {

      merger.setDestinationFileName(pdfOutput.getAbsolutePath());
      merger.addSource(is1);
      merger.addSource(is2);
      merger.mergeDocuments(null);
    }

    // Test merge result
    try (PDDocument mergedPDF = PDDocument.load(pdfOutput)) {
      Assert.assertEquals("There shall be 2 pages", 2, mergedPDF.getNumberOfPages());

      Assert.assertNotNull("There shall be an /Annots entry for the first page",
          mergedPDF.getPage(0).getCOSObject().getDictionaryObject(COSName.ANNOTS));
      Assert.assertEquals("There shall be 1 annotation for the first page", 1,
          mergedPDF.getPage(0).getAnnotations().size());

      Assert.assertNotNull("There shall be an /Annots entry for the second page",
          mergedPDF.getPage(1).getCOSObject().getDictionaryObject(COSName.ANNOTS));
      Assert.assertEquals("There shall be 1 annotation for the second page", 1,
          mergedPDF.getPage(0).getAnnotations().size());
    }
  }

  /*
   * PDFBOX-1100 Ensure that after merging the PDFs there is an AP and V entry.
   */
  @Test
  public void testAPEntry() throws IOException {

    final File file1 = new File(MergeAcroFormsTest.TARGET_PDF_DIR, "PDFBOX-1100-1.pdf");
    final File file2 = new File(MergeAcroFormsTest.TARGET_PDF_DIR, "PDFBOX-1100-2.pdf");
    // Merge the PDFs form PDFBOX-1100
    final PDFMergerUtility merger = new PDFMergerUtility();

    final File pdfOutput = new File(MergeAcroFormsTest.OUT_DIR, "PDFBOX-1100.pdf");

    try (InputStream is1 = new FileInputStream(file1); InputStream is2 = new FileInputStream(file2)) {
      merger.setDestinationFileName(pdfOutput.getAbsolutePath());
      merger.addSource(is1);
      merger.addSource(is2);
      merger.mergeDocuments(null);
    }

    // Test merge result
    try (PDDocument mergedPDF = PDDocument.load(pdfOutput)) {
      Assert.assertEquals("There shall be 2 pages", 2, mergedPDF.getNumberOfPages());

      final PDAcroForm acroForm = mergedPDF.getDocumentCatalog().getAcroForm();

      PDField formField = acroForm.getField("Testfeld");
      Assert.assertNotNull("There shall be an /AP entry for the field",
          formField.getCOSObject().getDictionaryObject(COSName.AP));
      Assert.assertNotNull("There shall be a /V entry for the field",
          formField.getCOSObject().getDictionaryObject(COSName.V));

      formField = acroForm.getField("Testfeld2");
      Assert.assertNotNull("There shall be an /AP entry for the field",
          formField.getCOSObject().getDictionaryObject(COSName.AP));
      Assert.assertNotNull("There shall be a /V entry for the field",
          formField.getCOSObject().getDictionaryObject(COSName.V));
    }
  }

}
