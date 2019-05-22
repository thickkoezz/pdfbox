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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This will test the functionality of Radio Buttons in PDFBox.
 */
public class TestRadioButtons extends TestCase {

  /**
   * Constructor.
   *
   * @param name The name of the test to run.
   */
  public TestRadioButtons(final String name) {
    super(name);
  }

  /**
   * This will get the suite of test that this class holds.
   *
   * @return All of the tests that this class holds.
   */
  public static Test suite() {
    return new TestSuite(TestRadioButtons.class);
  }

  /**
   * infamous main method.
   *
   * @param args The command line arguments.
   */
  public static void main(final String[] args) {
    final String[] arg = { TestRadioButtons.class.getName() };
    junit.textui.TestRunner.main(arg);
  }

  /**
   * This will test the radio button PDModel.
   *
   * @throws IOException If there is an error creating the field.
   */
  public void testRadioButtonPDModel() throws IOException {
    try (PDDocument doc = new PDDocument()) {
      final PDAcroForm form = new PDAcroForm(doc);
      final PDRadioButton radioButton = new PDRadioButton(form);

      // test that there are no nulls returned for an empty field
      // only specific methods are tested here
      TestCase.assertNotNull(radioButton.getDefaultValue());
      TestCase.assertNotNull(radioButton.getSelectedExportValues());
      TestCase.assertNotNull(radioButton.getExportValues());
      TestCase.assertNotNull(radioButton.getValue());

      // Test setting/getting option values - the dictionaries Opt entry
      final List<String> options = new ArrayList<>();
      options.add("Value01");
      options.add("Value02");
      radioButton.setExportValues(options);

      final COSArray optItem = (COSArray) radioButton.getCOSObject().getItem(COSName.OPT);

      // assert that the values have been correctly set
      TestCase.assertNotNull(radioButton.getCOSObject().getItem(COSName.OPT));
      TestCase.assertEquals(optItem.size(), 2);
      TestCase.assertEquals(options.get(0), optItem.getString(0));

      // assert that the values can be retrieved correctly
      final List<String> retrievedOptions = radioButton.getExportValues();
      TestCase.assertEquals(retrievedOptions.size(), 2);
      TestCase.assertEquals(retrievedOptions, options);

      // assert that the Opt entry is removed
      radioButton.setExportValues(null);
      TestCase.assertNull(radioButton.getCOSObject().getItem(COSName.OPT));
      // if there is no Opt entry an empty List shall be returned
      TestCase.assertEquals(radioButton.getExportValues(), new ArrayList<String>());
    }
  }
}