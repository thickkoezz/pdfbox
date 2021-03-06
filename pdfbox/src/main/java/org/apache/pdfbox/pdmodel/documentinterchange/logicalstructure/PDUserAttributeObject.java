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
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * A User attribute object.
 *
 * @author Johannes Koch
 */
public class PDUserAttributeObject extends PDAttributeObject {

  /**
   * Attribute owner for user properties
   */
  public static final String OWNER_USER_PROPERTIES = "UserProperties";

  /**
   * Default constructor
   */
  public PDUserAttributeObject() {
    setOwner(PDUserAttributeObject.OWNER_USER_PROPERTIES);
  }

  /**
   *
   * @param dictionary the dictionary
   */
  public PDUserAttributeObject(final COSDictionary dictionary) {
    super(dictionary);
  }

  /**
   * Returns the user properties.
   *
   * @return the user properties
   */
  public List<PDUserProperty> getOwnerUserProperties() {
    final COSArray p = (COSArray) getCOSObject().getDictionaryObject(COSName.P);
    final List<PDUserProperty> properties = new ArrayList<>(p.size());
    for (int i = 0; i < p.size(); i++) {
      properties.add(new PDUserProperty((COSDictionary) p.getObject(i), this));
    }
    return properties;
  }

  /**
   * Sets the user properties.
   *
   * @param userProperties the user properties
   */
  public void setUserProperties(final List<PDUserProperty> userProperties) {
    final COSArray p = new COSArray();
    for (final PDUserProperty userProperty : userProperties) {
      p.add(userProperty);
    }
    getCOSObject().setItem(COSName.P, p);
  }

  /**
   * Adds a user property.
   *
   * @param userProperty the user property
   */
  public void addUserProperty(final PDUserProperty userProperty) {
    final COSArray p = (COSArray) getCOSObject().getDictionaryObject(COSName.P);
    p.add(userProperty);
    notifyChanged();
  }

  /**
   * Removes a user property.
   *
   * @param userProperty the user property
   */
  public void removeUserProperty(final PDUserProperty userProperty) {
    if (userProperty == null)
      return;
    final COSArray p = (COSArray) getCOSObject().getDictionaryObject(COSName.P);
    p.remove(userProperty.getCOSObject());
    notifyChanged();
  }

  /**
   * @param userProperty
   */
  public void userPropertyChanged(final PDUserProperty userProperty) {

  }

  @Override
  public String toString() {
    return super.toString() + ", userProperties=" + getOwnerUserProperties();
  }

}
