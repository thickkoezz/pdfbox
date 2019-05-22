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
package org.apache.pdfbox.pdmodel.common;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.pdfbox.cos.COSInteger;
import org.junit.Assert;

import junit.framework.TestCase;

/**
 * A test case for PDNameTreeNode.
 *
 * @author Koch
 */
public class TestPDNameTreeNode extends TestCase {
  private PDNameTreeNode<COSInteger> node1;
  private PDNameTreeNode<COSInteger> node2;
  private PDNameTreeNode<COSInteger> node4;
  private PDNameTreeNode<COSInteger> node5;
  private PDNameTreeNode<COSInteger> node24;

  @Override
  protected void setUp() throws Exception {
    node5 = new PDIntegerNameTreeNode();
    Map<String, COSInteger> names = new TreeMap<>();
    names.put("Actinium", COSInteger.get(89));
    names.put("Aluminum", COSInteger.get(13));
    names.put("Americium", COSInteger.get(95));
    names.put("Antimony", COSInteger.get(51));
    names.put("Argon", COSInteger.get(18));
    names.put("Arsenic", COSInteger.get(33));
    names.put("Astatine", COSInteger.get(85));
    node5.setNames(names);

    node24 = new PDIntegerNameTreeNode();
    names = new TreeMap<>();
    names.put("Xenon", COSInteger.get(54));
    names.put("Ytterbium", COSInteger.get(70));
    names.put("Yttrium", COSInteger.get(39));
    names.put("Zinc", COSInteger.get(30));
    names.put("Zirconium", COSInteger.get(40));
    node24.setNames(names);

    node2 = new PDIntegerNameTreeNode();
    List<PDNameTreeNode<COSInteger>> kids = node2.getKids();
    if (kids == null) {
      kids = new COSArrayList<>();
    }
    kids.add(node5);
    node2.setKids(kids);

    node4 = new PDIntegerNameTreeNode();
    kids = node4.getKids();
    if (kids == null) {
      kids = new COSArrayList<>();
    }
    kids.add(node24);
    node4.setKids(kids);

    node1 = new PDIntegerNameTreeNode();
    kids = node1.getKids();
    if (kids == null) {
      kids = new COSArrayList<>();
    }
    kids.add(node2);
    kids.add(node4);
    node1.setKids(kids);
  }

  public void testUpperLimit() throws IOException {
    Assert.assertEquals("Astatine", node5.getUpperLimit());
    Assert.assertEquals("Astatine", node2.getUpperLimit());

    Assert.assertEquals("Zirconium", node24.getUpperLimit());
    Assert.assertEquals("Zirconium", node4.getUpperLimit());

    Assert.assertEquals(null, node1.getUpperLimit());
  }

  public void testLowerLimit() throws IOException {
    Assert.assertEquals("Actinium", node5.getLowerLimit());
    Assert.assertEquals("Actinium", node2.getLowerLimit());

    Assert.assertEquals("Xenon", node24.getLowerLimit());
    Assert.assertEquals("Xenon", node4.getLowerLimit());

    Assert.assertEquals(null, node1.getLowerLimit());
  }
}
