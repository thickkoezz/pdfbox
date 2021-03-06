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

package org.apache.pdfbox.pdmodel.fdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.junit.Assert;
import org.junit.Test;

/*
 * Test some characteristics of FDFFields
 */
public class FDFFieldTest {
  @Test
  public void testCOSStringValue() throws IOException {
    final String testString = "Test value";
    final COSString testCOSString = new COSString(testString);

    final FDFField field = new FDFField();
    field.setValue(testCOSString);

    Assert.assertEquals(testCOSString, field.getCOSValue());
    Assert.assertEquals(testString, field.getValue());
  }

  @Test
  public void testTextAsCOSStreamValue() throws IOException {
    final String testString = "Test value";
    final byte[] testBytes = testString.getBytes("ASCII");
    final COSStream stream = createStream(testBytes, null);

    final FDFField field = new FDFField();
    field.setValue(stream);

    Assert.assertEquals(testString, field.getValue());
  }

  @Test
  public void testCOSNameValue() throws IOException {
    final String testString = "Yes";
    final COSName testCOSSName = COSName.getPDFName(testString);

    final FDFField field = new FDFField();
    field.setValue(testCOSSName);

    Assert.assertEquals(testCOSSName, field.getCOSValue());
    Assert.assertEquals(testString, field.getValue());
  }

  @Test
  public void testCOSArrayValue() throws IOException {
    final List<String> testList = new ArrayList<>();
    testList.add("A");
    testList.add("B");

    final COSArray testCOSArray = COSArrayList.convertStringListToCOSStringCOSArray(testList);

    final FDFField field = new FDFField();
    field.setValue(testCOSArray);

    Assert.assertEquals(testCOSArray, field.getCOSValue());
    Assert.assertEquals(testList, field.getValue());
  }

  private COSStream createStream(final byte[] testString, final COSBase filters) throws IOException {
    final COSStream stream = new COSStream();
    final OutputStream output = stream.createOutputStream(filters);
    output.write(testString);
    output.close();
    return stream;
  }
}
