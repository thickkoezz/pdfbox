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
package org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrea Vacondio
 *
 */
public class PDOutlineItemTest {
  private PDOutlineItem root;
  private PDOutlineItem first;
  private PDOutlineItem second;
  private PDOutlineItem newSibling;

  @Before
  public void setUp() {
    root = new PDOutlineItem();
    first = new PDOutlineItem();
    second = new PDOutlineItem();
    root.addLast(first);
    root.addLast(second);
    newSibling = new PDOutlineItem();
    newSibling.addLast(new PDOutlineItem());
    newSibling.addLast(new PDOutlineItem());
  }

  @Test
  public void insertSiblingAfter_OpenChildToOpenParent() {
    newSibling.openNode();
    root.openNode();
    Assert.assertEquals(2, root.getOpenCount());
    first.insertSiblingAfter(newSibling);
    Assert.assertEquals(first.getNextSibling(), newSibling);
    Assert.assertEquals(second.getPreviousSibling(), newSibling);
    Assert.assertEquals(5, root.getOpenCount());
  }

  @Test
  public void insertSiblingBefore_OpenChildToOpenParent() {
    newSibling.openNode();
    root.openNode();
    Assert.assertEquals(2, root.getOpenCount());
    second.insertSiblingBefore(newSibling);
    Assert.assertEquals(first.getNextSibling(), newSibling);
    Assert.assertEquals(second.getPreviousSibling(), newSibling);
    Assert.assertEquals(5, root.getOpenCount());
  }

  @Test
  public void insertSiblingAfter_OpenChildToClosedParent() {
    newSibling.openNode();
    Assert.assertEquals(-2, root.getOpenCount());
    first.insertSiblingAfter(newSibling);
    Assert.assertEquals(first.getNextSibling(), newSibling);
    Assert.assertEquals(second.getPreviousSibling(), newSibling);
    Assert.assertEquals(-5, root.getOpenCount());
  }

  @Test
  public void insertSiblingBefore_OpenChildToClosedParent() {
    newSibling.openNode();
    Assert.assertEquals(-2, root.getOpenCount());
    second.insertSiblingBefore(newSibling);
    Assert.assertEquals(first.getNextSibling(), newSibling);
    Assert.assertEquals(second.getPreviousSibling(), newSibling);
    Assert.assertEquals(-5, root.getOpenCount());
  }

  @Test
  public void insertSiblingAfter_ClosedChildToOpenParent() {
    root.openNode();
    Assert.assertEquals(2, root.getOpenCount());
    first.insertSiblingAfter(newSibling);
    Assert.assertEquals(first.getNextSibling(), newSibling);
    Assert.assertEquals(second.getPreviousSibling(), newSibling);
    Assert.assertEquals(3, root.getOpenCount());
  }

  @Test
  public void insertSiblingBefore_ClosedChildToOpenParent() {
    root.openNode();
    Assert.assertEquals(2, root.getOpenCount());
    second.insertSiblingBefore(newSibling);
    Assert.assertEquals(first.getNextSibling(), newSibling);
    Assert.assertEquals(second.getPreviousSibling(), newSibling);
    Assert.assertEquals(3, root.getOpenCount());
  }

  @Test
  public void insertSiblingAfter_ClosedChildToClosedParent() {
    Assert.assertEquals(-2, root.getOpenCount());
    first.insertSiblingAfter(newSibling);
    Assert.assertEquals(first.getNextSibling(), newSibling);
    Assert.assertEquals(second.getPreviousSibling(), newSibling);
    Assert.assertEquals(-3, root.getOpenCount());
  }

  @Test
  public void insertSiblingBefore_ClosedChildToClosedParent() {
    Assert.assertEquals(-2, root.getOpenCount());
    second.insertSiblingBefore(newSibling);
    Assert.assertEquals(first.getNextSibling(), newSibling);
    Assert.assertEquals(second.getPreviousSibling(), newSibling);
    Assert.assertEquals(-3, root.getOpenCount());
  }

  @Test
  public void insertSiblingTop() {
    Assert.assertEquals(root.getFirstChild(), first);
    final PDOutlineItem newSibling = new PDOutlineItem();
    first.insertSiblingBefore(newSibling);
    Assert.assertEquals(first.getPreviousSibling(), newSibling);
    Assert.assertEquals(root.getFirstChild(), newSibling);
  }

  @Test
  public void insertSiblingTopNoParent() {
    Assert.assertEquals(root.getFirstChild(), first);
    final PDOutlineItem newSibling = new PDOutlineItem();
    root.insertSiblingBefore(newSibling);
    Assert.assertEquals(root.getPreviousSibling(), newSibling);
  }

  @Test
  public void insertSiblingBottom() {
    Assert.assertEquals(root.getLastChild(), second);
    final PDOutlineItem newSibling = new PDOutlineItem();
    second.insertSiblingAfter(newSibling);
    Assert.assertEquals(second.getNextSibling(), newSibling);
    Assert.assertEquals(root.getLastChild(), newSibling);
  }

  @Test
  public void insertSiblingBottomNoParent() {
    Assert.assertEquals(root.getLastChild(), second);
    final PDOutlineItem newSibling = new PDOutlineItem();
    root.insertSiblingAfter(newSibling);
    Assert.assertEquals(root.getNextSibling(), newSibling);
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotInsertSiblingBeforeAList() {
    final PDOutlineItem child = new PDOutlineItem();
    child.insertSiblingAfter(new PDOutlineItem());
    child.insertSiblingAfter(new PDOutlineItem());
    root.insertSiblingBefore(child);
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotInsertSiblingAfterAList() {
    final PDOutlineItem child = new PDOutlineItem();
    child.insertSiblingAfter(new PDOutlineItem());
    child.insertSiblingAfter(new PDOutlineItem());
    root.insertSiblingAfter(child);
  }
}
