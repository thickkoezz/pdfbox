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
package org.apache.pdfbox.pdmodel.interactive.pagenavigation;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Andrea Vacondio
 *
 */
public class PDTransitionTest {

  @Test
  public void defaultStyle() {
    final PDTransition transition = new PDTransition();
    Assert.assertEquals(COSName.TRANS, transition.getCOSObject().getCOSName(COSName.TYPE));
    Assert.assertEquals(PDTransitionStyle.R.name(), transition.getStyle());
  }

  @Test
  public void getStyle() {
    final PDTransition transition = new PDTransition(PDTransitionStyle.Fade);
    Assert.assertEquals(COSName.TRANS, transition.getCOSObject().getCOSName(COSName.TYPE));
    Assert.assertEquals(PDTransitionStyle.Fade.name(), transition.getStyle());
  }

  @Test
  public void defaultValues() {
    final PDTransition transition = new PDTransition(new COSDictionary());
    Assert.assertEquals(PDTransitionStyle.R.name(), transition.getStyle());
    Assert.assertEquals(PDTransitionDimension.H.name(), transition.getDimension());
    Assert.assertEquals(PDTransitionMotion.I.name(), transition.getMotion());
    Assert.assertEquals(COSInteger.ZERO, transition.getDirection());
    Assert.assertEquals(1, transition.getDuration(), 0);
    Assert.assertEquals(1, transition.getFlyScale(), 0);
    Assert.assertFalse(transition.isFlyAreaOpaque());
  }

  @Test
  public void dimension() {
    final PDTransition transition = new PDTransition();
    transition.setDimension(PDTransitionDimension.H);
    Assert.assertEquals(PDTransitionDimension.H.name(), transition.getDimension());
  }

  @Test
  public void directionNone() {
    final PDTransition transition = new PDTransition();
    transition.setDirection(PDTransitionDirection.NONE);
    Assert.assertEquals(COSName.class.getName(), transition.getDirection().getClass().getName());
    Assert.assertEquals(COSName.NONE, transition.getDirection());
  }

  @Test
  public void directionNumber() {
    final PDTransition transition = new PDTransition();
    transition.setDirection(PDTransitionDirection.LEFT_TO_RIGHT);
    Assert.assertEquals(COSInteger.class.getName(), transition.getDirection().getClass().getName());
    Assert.assertEquals(COSInteger.ZERO, transition.getDirection());
  }

  @Test
  public void motion() {
    final PDTransition transition = new PDTransition();
    transition.setMotion(PDTransitionMotion.O);
    Assert.assertEquals(PDTransitionMotion.O.name(), transition.getMotion());
  }

  @Test
  public void duration() {
    final PDTransition transition = new PDTransition();
    transition.setDuration(4);
    Assert.assertEquals(4, transition.getDuration(), 0);
  }

  @Test
  public void flyScale() {
    final PDTransition transition = new PDTransition();
    transition.setFlyScale(4);
    Assert.assertEquals(4, transition.getFlyScale(), 0);
  }

  @Test
  public void flyArea() {
    final PDTransition transition = new PDTransition();
    transition.setFlyAreaOpaque(true);
    Assert.assertTrue(transition.isFlyAreaOpaque());
  }
}
