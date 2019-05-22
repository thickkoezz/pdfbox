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
public class PDOutlineNodeTest {
  private PDOutlineItem root;

  @Before
  public void setUp() {
    root = new PDOutlineItem();
  }

  @Test
  public void getParent() {
    final PDOutlineItem child = new PDOutlineItem();
    root.addLast(child);
    final PDDocumentOutline outline = new PDDocumentOutline();
    outline.addLast(root);
    Assert.assertNull(outline.getParent());
    Assert.assertEquals(outline, root.getParent());
    Assert.assertEquals(root, child.getParent());
  }

  @Test
  public void nullLastChild() {
    Assert.assertNull(root.getLastChild());
  }

  @Test
  public void nullFirstChild() {
    Assert.assertNull(root.getFirstChild());
  }

  @Test
  public void openAlreadyOpenedRootNode() {
    final PDOutlineItem child = new PDOutlineItem();
    Assert.assertEquals(0, root.getOpenCount());
    root.addLast(child);
    root.openNode();
    Assert.assertTrue(root.isNodeOpen());
    Assert.assertEquals(1, root.getOpenCount());
    root.openNode();
    Assert.assertTrue(root.isNodeOpen());
    Assert.assertEquals(1, root.getOpenCount());
  }

  @Test
  public void closeAlreadyClosedRootNode() {
    final PDOutlineItem child = new PDOutlineItem();
    Assert.assertEquals(0, root.getOpenCount());
    root.addLast(child);
    root.openNode();
    root.closeNode();
    Assert.assertFalse(root.isNodeOpen());
    Assert.assertEquals(-1, root.getOpenCount());
    root.closeNode();
    Assert.assertFalse(root.isNodeOpen());
    Assert.assertEquals(-1, root.getOpenCount());
  }

  @Test
  public void openLeaf() {
    final PDOutlineItem child = new PDOutlineItem();
    root.addLast(child);
    child.openNode();
    Assert.assertFalse(child.isNodeOpen());
  }

  @Test
  public void nodeClosedByDefault() {
    final PDOutlineItem child = new PDOutlineItem();
    root.addLast(child);
    Assert.assertFalse(root.isNodeOpen());
    Assert.assertEquals(-1, root.getOpenCount());
  }

  @Test
  public void closeNodeWithOpendParent() {
    final PDOutlineItem child = new PDOutlineItem();
    child.addLast(new PDOutlineItem());
    child.addLast(new PDOutlineItem());
    child.openNode();
    root.addLast(child);
    root.openNode();
    Assert.assertEquals(3, root.getOpenCount());
    Assert.assertEquals(2, child.getOpenCount());
    child.closeNode();
    Assert.assertEquals(1, root.getOpenCount());
    Assert.assertEquals(-2, child.getOpenCount());
  }

  @Test
  public void closeNodeWithClosedParent() {
    final PDOutlineItem child = new PDOutlineItem();
    child.addLast(new PDOutlineItem());
    child.addLast(new PDOutlineItem());
    child.openNode();
    root.addLast(child);
    Assert.assertEquals(-3, root.getOpenCount());
    Assert.assertEquals(2, child.getOpenCount());
    child.closeNode();
    Assert.assertEquals(-1, root.getOpenCount());
    Assert.assertEquals(-2, child.getOpenCount());
  }

  @Test
  public void openNodeWithOpendParent() {
    final PDOutlineItem child = new PDOutlineItem();
    child.addLast(new PDOutlineItem());
    child.addLast(new PDOutlineItem());
    root.addLast(child);
    root.openNode();
    Assert.assertEquals(1, root.getOpenCount());
    Assert.assertEquals(-2, child.getOpenCount());
    child.openNode();
    Assert.assertEquals(3, root.getOpenCount());
    Assert.assertEquals(2, child.getOpenCount());
  }

  @Test
  public void openNodeWithClosedParent() {
    final PDOutlineItem child = new PDOutlineItem();
    child.addLast(new PDOutlineItem());
    child.addLast(new PDOutlineItem());
    root.addLast(child);
    Assert.assertEquals(-1, root.getOpenCount());
    Assert.assertEquals(-2, child.getOpenCount());
    child.openNode();
    Assert.assertEquals(-3, root.getOpenCount());
    Assert.assertEquals(2, child.getOpenCount());
  }

  @Test
  public void addLastSingleChild() {
    final PDOutlineItem child = new PDOutlineItem();
    root.addLast(child);
    Assert.assertEquals(child, root.getFirstChild());
    Assert.assertEquals(child, root.getLastChild());
  }

  @Test
  public void addFirstSingleChild() {
    final PDOutlineItem child = new PDOutlineItem();
    root.addFirst(child);
    Assert.assertEquals(child, root.getFirstChild());
    Assert.assertEquals(child, root.getLastChild());
  }

  @Test
  public void addLastOpenChildToOpenParent() {
    final PDOutlineItem child = new PDOutlineItem();
    child.addLast(new PDOutlineItem());
    child.addLast(new PDOutlineItem());
    child.openNode();
    root.addLast(new PDOutlineItem());
    root.openNode();
    Assert.assertEquals(1, root.getOpenCount());
    Assert.assertEquals(2, child.getOpenCount());
    root.addLast(child);
    Assert.assertNotEquals(child, root.getFirstChild());
    Assert.assertEquals(child, root.getLastChild());
    Assert.assertEquals(4, root.getOpenCount());
  }

  @Test
  public void addFirstOpenChildToOpenParent() {
    final PDOutlineItem child = new PDOutlineItem();
    child.addFirst(new PDOutlineItem());
    child.addFirst(new PDOutlineItem());
    child.openNode();
    root.addFirst(new PDOutlineItem());
    root.openNode();
    Assert.assertEquals(1, root.getOpenCount());
    Assert.assertEquals(2, child.getOpenCount());
    root.addFirst(child);
    Assert.assertNotEquals(child, root.getLastChild());
    Assert.assertEquals(child, root.getFirstChild());
    Assert.assertEquals(4, root.getOpenCount());
  }

  @Test
  public void addLastOpenChildToClosedParent() {
    final PDOutlineItem child = new PDOutlineItem();
    child.addLast(new PDOutlineItem());
    child.addLast(new PDOutlineItem());
    child.openNode();
    root.addLast(new PDOutlineItem());
    Assert.assertEquals(-1, root.getOpenCount());
    Assert.assertEquals(2, child.getOpenCount());
    root.addLast(child);
    Assert.assertNotEquals(child, root.getFirstChild());
    Assert.assertEquals(child, root.getLastChild());
    Assert.assertEquals(-4, root.getOpenCount());
  }

  @Test
  public void addFirstOpenChildToClosedParent() {
    final PDOutlineItem child = new PDOutlineItem();
    child.addFirst(new PDOutlineItem());
    child.addFirst(new PDOutlineItem());
    child.openNode();
    root.addFirst(new PDOutlineItem());
    Assert.assertEquals(-1, root.getOpenCount());
    Assert.assertEquals(2, child.getOpenCount());
    root.addFirst(child);
    Assert.assertNotEquals(child, root.getLastChild());
    Assert.assertEquals(child, root.getFirstChild());
    Assert.assertEquals(-4, root.getOpenCount());
  }

  @Test
  public void addLastClosedChildToOpenParent() {
    final PDOutlineItem child = new PDOutlineItem();
    child.addLast(new PDOutlineItem());
    child.addLast(new PDOutlineItem());
    root.addLast(new PDOutlineItem());
    root.openNode();
    Assert.assertEquals(1, root.getOpenCount());
    Assert.assertEquals(-2, child.getOpenCount());
    root.addLast(child);
    Assert.assertNotEquals(child, root.getFirstChild());
    Assert.assertEquals(child, root.getLastChild());
    Assert.assertEquals(2, root.getOpenCount());
  }

  @Test
  public void addFirstClosedChildToOpenParent() {
    final PDOutlineItem child = new PDOutlineItem();
    child.addFirst(new PDOutlineItem());
    child.addFirst(new PDOutlineItem());
    root.addFirst(new PDOutlineItem());
    root.openNode();
    Assert.assertEquals(1, root.getOpenCount());
    Assert.assertEquals(-2, child.getOpenCount());
    root.addFirst(child);
    Assert.assertNotEquals(child, root.getLastChild());
    Assert.assertEquals(child, root.getFirstChild());
    Assert.assertEquals(2, root.getOpenCount());
  }

  @Test
  public void addLastClosedChildToClosedParent() {
    final PDOutlineItem child = new PDOutlineItem();
    child.addLast(new PDOutlineItem());
    child.addLast(new PDOutlineItem());
    root.addLast(new PDOutlineItem());
    Assert.assertEquals(-1, root.getOpenCount());
    Assert.assertEquals(-2, child.getOpenCount());
    root.addLast(child);
    Assert.assertNotEquals(child, root.getFirstChild());
    Assert.assertEquals(child, root.getLastChild());
    Assert.assertEquals(-2, root.getOpenCount());
  }

  @Test
  public void addFirstClosedChildToClosedParent() {
    final PDOutlineItem child = new PDOutlineItem();
    child.addFirst(new PDOutlineItem());
    child.addFirst(new PDOutlineItem());
    root.addFirst(new PDOutlineItem());
    Assert.assertEquals(-1, root.getOpenCount());
    Assert.assertEquals(-2, child.getOpenCount());
    root.addFirst(child);
    Assert.assertNotEquals(child, root.getLastChild());
    Assert.assertEquals(child, root.getFirstChild());
    Assert.assertEquals(-2, root.getOpenCount());
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotAddLastAList() {
    final PDOutlineItem child = new PDOutlineItem();
    child.insertSiblingAfter(new PDOutlineItem());
    child.insertSiblingAfter(new PDOutlineItem());
    root.addLast(child);
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotAddFirstAList() {
    final PDOutlineItem child = new PDOutlineItem();
    child.insertSiblingAfter(new PDOutlineItem());
    child.insertSiblingAfter(new PDOutlineItem());
    root.addFirst(child);
  }

  @Test
  public void equalsNode() {
    root.addFirst(new PDOutlineItem());
    Assert.assertEquals(root.getFirstChild(), root.getLastChild());
  }

  @Test
  public void iterator() {
    final PDOutlineItem first = new PDOutlineItem();
    root.addFirst(first);
    root.addLast(new PDOutlineItem());
    final PDOutlineItem second = new PDOutlineItem();
    first.insertSiblingAfter(second);
    int counter = 0;
    for (final PDOutlineItem current : root.children()) {
      counter++;
    }
    Assert.assertEquals(3, counter);
  }

  @Test
  public void iteratorNoChildre() {
    int counter = 0;
    for (final PDOutlineItem current : new PDOutlineItem().children()) {
      counter++;
    }
    Assert.assertEquals(0, counter);
  }

  @Test
  public void openNodeAndAppend() {
    // TODO
  }

}
