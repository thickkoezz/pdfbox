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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for the PDButton class.
 *
 */
public class PDButtonTest {

  private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/pdmodel/interactive/form");
  private static final String NAME_OF_PDF = "AcroFormsBasicFields.pdf";
  private static final File TARGET_PDF_DIR = new File("target/pdfs");

  private PDDocument document;
  private PDAcroForm acroForm;

  private PDDocument acrobatDocument;
  private PDAcroForm acrobatAcroForm;

  @Before
  public void setUp() throws IOException {
    document = new PDDocument();
    acroForm = new PDAcroForm(document);

    acrobatDocument = PDDocument.load(new File(PDButtonTest.IN_DIR, PDButtonTest.NAME_OF_PDF));
    acrobatAcroForm = acrobatDocument.getDocumentCatalog().getAcroForm();
  }

  @Test
  public void createCheckBox() {
    final PDButton buttonField = new PDCheckBox(acroForm);

    Assert.assertEquals(buttonField.getFieldType(), buttonField.getCOSObject().getNameAsString(COSName.FT));
    Assert.assertEquals(buttonField.getFieldType(), "Btn");
    Assert.assertFalse(buttonField.isPushButton());
    Assert.assertFalse(buttonField.isRadioButton());
  }

  @Test
  public void createPushButton() {
    final PDButton buttonField = new PDPushButton(acroForm);

    Assert.assertEquals(buttonField.getFieldType(), buttonField.getCOSObject().getNameAsString(COSName.FT));
    Assert.assertEquals(buttonField.getFieldType(), "Btn");
    Assert.assertTrue(buttonField.isPushButton());
    Assert.assertFalse(buttonField.isRadioButton());
  }

  @Test
  public void createRadioButton() {
    final PDButton buttonField = new PDRadioButton(acroForm);

    Assert.assertEquals(buttonField.getFieldType(), buttonField.getCOSObject().getNameAsString(COSName.FT));
    Assert.assertEquals(buttonField.getFieldType(), "Btn");
    Assert.assertTrue(buttonField.isRadioButton());
    Assert.assertFalse(buttonField.isPushButton());
  }

  @Test
  /**
   * PDFBOX-3656
   * 
   * Test a radio button with options. This was causing an
   * ArrayIndexOutOfBoundsException when trying to set to "Off", as this wasn't
   * treated to be a valid option.
   * 
   * @throws IOException
   */
  public void testRadioButtonWithOptions() throws MalformedURLException {
    final File file = new File(PDButtonTest.TARGET_PDF_DIR, "PDFBOX-3656.pdf");

    try (InputStream is = new FileInputStream(file); PDDocument pdfDocument = PDDocument.load(is)) {
      final PDRadioButton radioButton = (PDRadioButton) pdfDocument.getDocumentCatalog().getAcroForm()
          .getField("Checking/Savings");
      radioButton.setValue("Off");
      for (final PDAnnotationWidget widget : radioButton.getWidgets()) {
        Assert.assertEquals("The widget should be set to Off", COSName.Off, widget.getCOSObject().getItem(COSName.AS));
      }

    } catch (final IOException e) {
      Assert.fail("Unexpected IOException " + e.getMessage());
    }
  }

  @Test
  /**
   * PDFBOX-3682
   * 
   * Test a radio button with options. Special handling for a radio button with
   * /Opt and the On state not being named after the index.
   * 
   * @throws IOException
   */
  public void testOptionsAndNamesNotNumbers() throws MalformedURLException {
    final File file = new File(PDButtonTest.TARGET_PDF_DIR, "PDFBOX-3682.pdf");
    try (InputStream is = new FileInputStream(file); PDDocument pdfDocument = PDDocument.load(is)) {
      pdfDocument.getDocumentCatalog().getAcroForm().getField("RadioButton").setValue("c");
      final PDRadioButton radioButton = (PDRadioButton) pdfDocument.getDocumentCatalog().getAcroForm()
          .getField("RadioButton");
      radioButton.setValue("c");

      // test that the old behavior is now invalid
      Assert.assertFalse("This shall no longer be 2", "2".equals(radioButton.getValueAsString()));
      Assert.assertFalse("This shall no longer be 2",
          "2".equals(radioButton.getWidgets().get(2).getCOSObject().getNameAsString(COSName.AS)));

      // test for the correct behavior
      Assert.assertTrue("This shall be c", "c".equals(radioButton.getValueAsString()));
      Assert.assertTrue("This shall be c",
          "c".equals(radioButton.getWidgets().get(2).getCOSObject().getNameAsString(COSName.AS)));
    } catch (final IOException e) {
      Assert.fail("Unexpected IOException " + e.getMessage());
    }
  }

  @Test
  public void retrieveAcrobatCheckBoxProperties() {
    final PDCheckBox checkbox = (PDCheckBox) acrobatAcroForm.getField("Checkbox");
    Assert.assertNotNull(checkbox);
    Assert.assertEquals(checkbox.getOnValue(), "Yes");
    Assert.assertEquals(checkbox.getOnValues().size(), 1);
    Assert.assertTrue(checkbox.getOnValues().contains("Yes"));
  }

  @Test
  public void testAcrobatCheckBoxProperties() throws IOException {
    PDCheckBox checkbox = (PDCheckBox) acrobatAcroForm.getField("Checkbox");
    Assert.assertEquals(checkbox.getValue(), "Off");
    Assert.assertEquals(checkbox.isChecked(), false);

    checkbox.check();
    Assert.assertEquals(checkbox.getValue(), checkbox.getOnValue());
    Assert.assertEquals(checkbox.isChecked(), true);

    checkbox.setValue("Yes");
    Assert.assertEquals(checkbox.getValue(), checkbox.getOnValue());
    Assert.assertEquals(checkbox.isChecked(), true);
    Assert.assertEquals(checkbox.getCOSObject().getDictionaryObject(COSName.AS), COSName.YES);

    checkbox.setValue("Off");
    Assert.assertEquals(checkbox.getValue(), COSName.Off.getName());
    Assert.assertEquals(checkbox.isChecked(), false);
    Assert.assertEquals(checkbox.getCOSObject().getDictionaryObject(COSName.AS), COSName.Off);

    checkbox = (PDCheckBox) acrobatAcroForm.getField("Checkbox-DefaultValue");
    Assert.assertEquals(checkbox.getDefaultValue(), checkbox.getOnValue());

    checkbox.setDefaultValue("Off");
    Assert.assertEquals(checkbox.getDefaultValue(), COSName.Off.getName());
  }

  @Test
  public void setValueForAbstractedAcrobatCheckBox() throws IOException {
    final PDField checkbox = acrobatAcroForm.getField("Checkbox");

    checkbox.setValue("Yes");
    Assert.assertEquals(checkbox.getValueAsString(), ((PDCheckBox) checkbox).getOnValue());
    Assert.assertEquals(((PDCheckBox) checkbox).isChecked(), true);
    Assert.assertEquals(checkbox.getCOSObject().getDictionaryObject(COSName.AS), COSName.YES);

    checkbox.setValue("Off");
    Assert.assertEquals(checkbox.getValueAsString(), COSName.Off.getName());
    Assert.assertEquals(((PDCheckBox) checkbox).isChecked(), false);
    Assert.assertEquals(checkbox.getCOSObject().getDictionaryObject(COSName.AS), COSName.Off);
  }

  @Test
  public void testAcrobatCheckBoxGroupProperties() throws IOException {
    final PDCheckBox checkbox = (PDCheckBox) acrobatAcroForm.getField("CheckboxGroup");
    Assert.assertEquals(checkbox.getValue(), "Off");
    Assert.assertEquals(checkbox.isChecked(), false);

    checkbox.check();
    Assert.assertEquals(checkbox.getValue(), checkbox.getOnValue());
    Assert.assertEquals(checkbox.isChecked(), true);

    Assert.assertEquals(checkbox.getOnValues().size(), 3);
    Assert.assertTrue(checkbox.getOnValues().contains("Option1"));
    Assert.assertTrue(checkbox.getOnValues().contains("Option2"));
    Assert.assertTrue(checkbox.getOnValues().contains("Option3"));

    // test a value which sets one of the individual checkboxes within the group
    checkbox.setValue("Option1");
    Assert.assertEquals("Option1", checkbox.getValue());
    Assert.assertEquals("Option1", checkbox.getValueAsString());

    // ensure that for the widgets representing the individual checkboxes
    // the AS entry has been set
    Assert.assertEquals("Option1", checkbox.getWidgets().get(0).getAppearanceState().getName());
    Assert.assertEquals("Off", checkbox.getWidgets().get(1).getAppearanceState().getName());
    Assert.assertEquals("Off", checkbox.getWidgets().get(2).getAppearanceState().getName());
    Assert.assertEquals("Off", checkbox.getWidgets().get(3).getAppearanceState().getName());

    // test a value which sets two of the individual chekboxes within the group
    // as the have the same name entry for being checked
    checkbox.setValue("Option3");
    Assert.assertEquals("Option3", checkbox.getValue());
    Assert.assertEquals("Option3", checkbox.getValueAsString());

    // ensure that for both widgets representing the individual checkboxes
    // the AS entry has been set
    Assert.assertEquals("Off", checkbox.getWidgets().get(0).getAppearanceState().getName());
    Assert.assertEquals("Off", checkbox.getWidgets().get(1).getAppearanceState().getName());
    Assert.assertEquals("Option3", checkbox.getWidgets().get(2).getAppearanceState().getName());
    Assert.assertEquals("Option3", checkbox.getWidgets().get(3).getAppearanceState().getName());
  }

  @Test
  public void setValueForAbstractedCheckBoxGroup() throws IOException {
    final PDField checkbox = acrobatAcroForm.getField("CheckboxGroup");

    // test a value which sets one of the individual checkboxes within the group
    checkbox.setValue("Option1");
    Assert.assertEquals("Option1", checkbox.getValueAsString());

    // ensure that for the widgets representing the individual checkboxes
    // the AS entry has been set
    Assert.assertEquals("Option1", checkbox.getWidgets().get(0).getAppearanceState().getName());
    Assert.assertEquals("Off", checkbox.getWidgets().get(1).getAppearanceState().getName());
    Assert.assertEquals("Off", checkbox.getWidgets().get(2).getAppearanceState().getName());
    Assert.assertEquals("Off", checkbox.getWidgets().get(3).getAppearanceState().getName());

    // test a value which sets two of the individual chekboxes within the group
    // as the have the same name entry for being checked
    checkbox.setValue("Option3");
    Assert.assertEquals("Option3", checkbox.getValueAsString());

    // ensure that for both widgets representing the individual checkboxes
    // the AS entry has been set
    Assert.assertEquals("Off", checkbox.getWidgets().get(0).getAppearanceState().getName());
    Assert.assertEquals("Off", checkbox.getWidgets().get(1).getAppearanceState().getName());
    Assert.assertEquals("Option3", checkbox.getWidgets().get(2).getAppearanceState().getName());
    Assert.assertEquals("Option3", checkbox.getWidgets().get(3).getAppearanceState().getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void setCheckboxInvalidValue() throws IOException {
    final PDCheckBox checkbox = (PDCheckBox) acrobatAcroForm.getField("Checkbox");
    // Set a value which doesn't match the radio button list
    checkbox.setValue("InvalidValue");
  }

  @Test(expected = IllegalArgumentException.class)
  public void setCheckboxGroupInvalidValue() throws IOException {
    final PDCheckBox checkbox = (PDCheckBox) acrobatAcroForm.getField("CheckboxGroup");
    // Set a value which doesn't match the radio button list
    checkbox.setValue("InvalidValue");
  }

  @Test(expected = IllegalArgumentException.class)
  public void setAbstractedCheckboxInvalidValue() throws IOException {
    final PDField checkbox = acrobatAcroForm.getField("Checkbox");
    // Set a value which doesn't match the radio button list
    checkbox.setValue("InvalidValue");
  }

  @Test(expected = IllegalArgumentException.class)
  public void setAbstractedCheckboxGroupInvalidValue() throws IOException {
    final PDField checkbox = acrobatAcroForm.getField("CheckboxGroup");
    // Set a value which doesn't match the radio button list
    checkbox.setValue("InvalidValue");
  }

  @Test
  public void retrieveAcrobatRadioButtonProperties() {
    final PDRadioButton radioButton = (PDRadioButton) acrobatAcroForm.getField("RadioButtonGroup");
    Assert.assertNotNull(radioButton);
    Assert.assertEquals(radioButton.getOnValues().size(), 2);
    Assert.assertTrue(radioButton.getOnValues().contains("RadioButton01"));
    Assert.assertTrue(radioButton.getOnValues().contains("RadioButton02"));
  }

  @Test
  public void testAcrobatRadioButtonProperties() throws IOException {
    final PDRadioButton radioButton = (PDRadioButton) acrobatAcroForm.getField("RadioButtonGroup");

    // Set value so that first radio button option is selected
    radioButton.setValue("RadioButton01");
    Assert.assertEquals(radioButton.getValue(), "RadioButton01");
    // First option shall have /RadioButton01, second shall have /Off
    Assert.assertEquals(radioButton.getWidgets().get(0).getCOSObject().getDictionaryObject(COSName.AS),
        COSName.getPDFName("RadioButton01"));
    Assert.assertEquals(radioButton.getWidgets().get(1).getCOSObject().getDictionaryObject(COSName.AS), COSName.Off);

    // Set value so that second radio button option is selected
    radioButton.setValue("RadioButton02");
    Assert.assertEquals(radioButton.getValue(), "RadioButton02");
    // First option shall have /Off, second shall have /RadioButton02
    Assert.assertEquals(radioButton.getWidgets().get(0).getCOSObject().getDictionaryObject(COSName.AS), COSName.Off);
    Assert.assertEquals(radioButton.getWidgets().get(1).getCOSObject().getDictionaryObject(COSName.AS),
        COSName.getPDFName("RadioButton02"));
  }

  @Test
  public void setValueForAbstractedAcrobatRadioButton() throws IOException {
    final PDField radioButton = acrobatAcroForm.getField("RadioButtonGroup");

    // Set value so that first radio button option is selected
    radioButton.setValue("RadioButton01");
    Assert.assertEquals(radioButton.getValueAsString(), "RadioButton01");
    // First option shall have /RadioButton01, second shall have /Off
    Assert.assertEquals(radioButton.getWidgets().get(0).getCOSObject().getDictionaryObject(COSName.AS),
        COSName.getPDFName("RadioButton01"));
    Assert.assertEquals(radioButton.getWidgets().get(1).getCOSObject().getDictionaryObject(COSName.AS), COSName.Off);

    // Set value so that second radio button option is selected
    radioButton.setValue("RadioButton02");
    Assert.assertEquals(radioButton.getValueAsString(), "RadioButton02");
    // First option shall have /Off, second shall have /RadioButton02
    Assert.assertEquals(radioButton.getWidgets().get(0).getCOSObject().getDictionaryObject(COSName.AS), COSName.Off);
    Assert.assertEquals(radioButton.getWidgets().get(1).getCOSObject().getDictionaryObject(COSName.AS),
        COSName.getPDFName("RadioButton02"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void setRadioButtonInvalidValue() throws IOException {
    final PDRadioButton radioButton = (PDRadioButton) acrobatAcroForm.getField("RadioButtonGroup");
    // Set a value which doesn't match the radio button list
    radioButton.setValue("InvalidValue");
  }

  @Test(expected = IllegalArgumentException.class)
  public void setAbstractedRadioButtonInvalidValue() throws IOException {
    final PDField radioButton = acrobatAcroForm.getField("RadioButtonGroup");
    // Set a value which doesn't match the radio button list
    radioButton.setValue("InvalidValue");
  }

  @After
  public void tearDown() throws IOException {
    document.close();
    acrobatDocument.close();
  }

}
