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

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for the PDSignatureField class.
 *
 */
public class PDSignatureFieldTest {
  private PDDocument document;
  private PDAcroForm acroForm;

  @Before
  public void setUp() {
    document = new PDDocument();
    acroForm = new PDAcroForm(document);
  }

  @Test
  public void createDefaultSignatureField() {
    final PDSignatureField sigField = new PDSignatureField(acroForm);
    sigField.setPartialName("SignatureField");

    Assert.assertEquals(sigField.getFieldType(), sigField.getCOSObject().getNameAsString(COSName.FT));
    Assert.assertEquals(sigField.getFieldType(), "Sig");

    Assert.assertEquals(COSName.ANNOT, sigField.getCOSObject().getItem(COSName.TYPE));
    Assert.assertEquals(PDAnnotationWidget.SUB_TYPE, sigField.getCOSObject().getNameAsString(COSName.SUBTYPE));

    // Add the field to the acroform
    final List<PDField> fields = new ArrayList<>();
    fields.add(sigField);
    acroForm.setFields(fields);

    Assert.assertNotNull(acroForm.getField("SignatureField"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void setValueForAbstractedSignatureField() {
    final PDSignatureField sigField = new PDSignatureField(acroForm);
    sigField.setPartialName("SignatureField");

    sigField.setValue("Can't set value using String");
  }
}
