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
package org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * A default attribute object.
 *
 * @author Johannes Koch
 */
public class PDDefaultAttributeObject extends PDAttributeObject {

  /**
   * Default constructor.
   */
  public PDDefaultAttributeObject() {
  }

  /**
   * Creates a default attribute object with a given dictionary.
   *
   * @param dictionary the dictionary
   */
  public PDDefaultAttributeObject(final COSDictionary dictionary) {
    super(dictionary);
  }

  /**
   * Gets the attribute names.
   *
   * @return the attribute names
   */
  public List<String> getAttributeNames() {
    final List<String> attrNames = new ArrayList<>();
    for (final Entry<COSName, COSBase> entry : getCOSObject().entrySet()) {
      final COSName key = entry.getKey();
      if (!COSName.O.equals(key)) {
        attrNames.add(key.getName());
      }
    }
    return attrNames;
  }

  /**
   * Gets the attribute value for a given name.
   *
   * @param attrName the given attribute name
   * @return the attribute value for a given name
   */
  public COSBase getAttributeValue(final String attrName) {
    return getCOSObject().getDictionaryObject(attrName);
  }

  /**
   * Gets the attribute value for a given name.
   *
   * @param attrName     the given attribute name
   * @param defaultValue the default value
   * @return the attribute value for a given name
   */
  protected COSBase getAttributeValue(final String attrName, final COSBase defaultValue) {
    final COSBase value = getCOSObject().getDictionaryObject(attrName);
    if (value == null)
      return defaultValue;
    return value;
  }

  /**
   * Sets an attribute.
   *
   * @param attrName  the attribute name
   * @param attrValue the attribute value
   */
  public void setAttribute(final String attrName, final COSBase attrValue) {
    final COSBase old = this.getAttributeValue(attrName);
    getCOSObject().setItem(COSName.getPDFName(attrName), attrValue);
    potentiallyNotifyChanged(old, attrValue);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder().append(super.toString()).append(", attributes={");
    final Iterator<String> it = getAttributeNames().iterator();
    while (it.hasNext()) {
      final String name = it.next();
      sb.append(name).append('=').append(this.getAttributeValue(name));
      if (it.hasNext()) {
        sb.append(", ");
      }
    }
    return sb.append('}').toString();
  }

}
