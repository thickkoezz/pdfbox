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

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.TestPDFToImage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MultilineFieldsTest {
  private static final File OUT_DIR = new File("target/test-output");
  private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/pdmodel/interactive/form");
  private static final String NAME_OF_PDF = "MultilineFields.pdf";
  private static final String TEST_VALUE = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, "
      + "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam";

  private PDDocument document;
  private PDAcroForm acroForm;

  @Before
  public void setUp() throws IOException {
    document = PDDocument.load(new File(MultilineFieldsTest.IN_DIR, MultilineFieldsTest.NAME_OF_PDF));
    acroForm = document.getDocumentCatalog().getAcroForm();
    MultilineFieldsTest.OUT_DIR.mkdirs();
  }

  @Test
  public void fillFields() throws IOException {
    PDTextField field = (PDTextField) acroForm.getField("AlignLeft");
    field.setValue(MultilineFieldsTest.TEST_VALUE);

    field = (PDTextField) acroForm.getField("AlignMiddle");
    field.setValue(MultilineFieldsTest.TEST_VALUE);

    field = (PDTextField) acroForm.getField("AlignRight");
    field.setValue(MultilineFieldsTest.TEST_VALUE);

    field = (PDTextField) acroForm.getField("AlignLeft-Border_Small");
    field.setValue(MultilineFieldsTest.TEST_VALUE);

    field = (PDTextField) acroForm.getField("AlignMiddle-Border_Small");
    field.setValue(MultilineFieldsTest.TEST_VALUE);

    field = (PDTextField) acroForm.getField("AlignRight-Border_Small");
    field.setValue(MultilineFieldsTest.TEST_VALUE);

    field = (PDTextField) acroForm.getField("AlignLeft-Border_Medium");
    field.setValue(MultilineFieldsTest.TEST_VALUE);

    field = (PDTextField) acroForm.getField("AlignMiddle-Border_Medium");
    field.setValue(MultilineFieldsTest.TEST_VALUE);

    field = (PDTextField) acroForm.getField("AlignRight-Border_Medium");
    field.setValue(MultilineFieldsTest.TEST_VALUE);

    field = (PDTextField) acroForm.getField("AlignLeft-Border_Wide");
    field.setValue(MultilineFieldsTest.TEST_VALUE);

    field = (PDTextField) acroForm.getField("AlignMiddle-Border_Wide");
    field.setValue(MultilineFieldsTest.TEST_VALUE);

    field = (PDTextField) acroForm.getField("AlignRight-Border_Wide");
    field.setValue(MultilineFieldsTest.TEST_VALUE);

    // compare rendering
    final File file = new File(MultilineFieldsTest.OUT_DIR, MultilineFieldsTest.NAME_OF_PDF);
    document.save(file);
    final TestPDFToImage testPDFToImage = new TestPDFToImage(TestPDFToImage.class.getName());
    if (!testPDFToImage.doTestFile(file, MultilineFieldsTest.IN_DIR.getAbsolutePath(),
        MultilineFieldsTest.OUT_DIR.getAbsolutePath())) {
      // don't fail, rendering is different on different systems, result must be
      // viewed manually
      System.err.println("Rendering of " + file + " failed or is not identical to expected rendering in "
          + MultilineFieldsTest.IN_DIR + " directory");
    }
  }

  @After
  public void tearDown() throws IOException {
    document.close();
  }

}
