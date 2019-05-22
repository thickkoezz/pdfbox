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
import org.junit.Test;

/**
 * @author Andrea Vacondio
 *
 */
public class PDOutlineItemIteratorTest {

  @Test
  public void singleItem() {
    final PDOutlineItem first = new PDOutlineItem();
    final PDOutlineItemIterator iterator = new PDOutlineItemIterator(first);
    Assert.assertTrue(iterator.hasNext());
    Assert.assertEquals(first, iterator.next());
    Assert.assertFalse(iterator.hasNext());
  }

  @Test
  public void multipleItem() {
    final PDOutlineItem first = new PDOutlineItem();
    final PDOutlineItem second = new PDOutlineItem();
    first.setNextSibling(second);
    final PDOutlineItemIterator iterator = new PDOutlineItemIterator(first);
    Assert.assertTrue(iterator.hasNext());
    Assert.assertEquals(first, iterator.next());
    Assert.assertTrue(iterator.hasNext());
    Assert.assertEquals(second, iterator.next());
    Assert.assertFalse(iterator.hasNext());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void removeUnsupported() {
    new PDOutlineItemIterator(new PDOutlineItem()).remove();
  }

  @Test
  public void noChildren() {
    final PDOutlineItemIterator iterator = new PDOutlineItemIterator(null);
    Assert.assertFalse(iterator.hasNext());
  }
}
