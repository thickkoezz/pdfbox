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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.pagenavigation.PDTransition;
import org.apache.pdfbox.pdmodel.interactive.pagenavigation.PDTransitionDirection;
import org.apache.pdfbox.pdmodel.interactive.pagenavigation.PDTransitionStyle;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Andrea Vacondio
 *
 */
public class TestPDPageTransitions {

  @Test
  public void readTransitions() throws IOException, URISyntaxException {
    try (PDDocument doc = PDDocument.load(new File(this.getClass()
        .getResource("/org/apache/pdfbox/pdmodel/interactive/pagenavigation/transitions_test.pdf").toURI()))) {
      final PDTransition firstTransition = doc.getPages().get(0).getTransition();
      Assert.assertEquals(PDTransitionStyle.Glitter.name(), firstTransition.getStyle());
      Assert.assertEquals(2, firstTransition.getDuration(), 0);
      Assert.assertEquals(PDTransitionDirection.TOP_LEFT_TO_BOTTOM_RIGHT.getCOSBase(), firstTransition.getDirection());
    }
  }

  @Test
  public void saveAndReadTransitions() throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    // save
    try (PDDocument document = new PDDocument()) {
      final PDPage page = new PDPage();
      document.addPage(page);
      final PDTransition transition = new PDTransition(PDTransitionStyle.Fly);
      transition.setDirection(PDTransitionDirection.NONE);
      transition.setFlyScale(0.5f);
      page.setTransition(transition, 2);
      document.save(baos);
    }

    // read
    try (PDDocument doc = PDDocument.load(baos.toByteArray())) {
      final PDPage page = doc.getPages().get(0);
      final PDTransition loadedTransition = page.getTransition();
      Assert.assertEquals(PDTransitionStyle.Fly.name(), loadedTransition.getStyle());
      Assert.assertEquals(2, page.getCOSObject().getFloat(COSName.DUR), 0);
      Assert.assertEquals(PDTransitionDirection.NONE.getCOSBase(), loadedTransition.getDirection());
    }
  }
}
