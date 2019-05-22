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
package org.apache.pdfbox.pdmodel.interactive.measurement;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * This class represents a measure dictionary.
 *
 */
public class PDMeasureDictionary implements COSObjectable {
  /**
   * The type of the dictionary.
   */
  public static final String TYPE = "Measure";

  private final COSDictionary measureDictionary;

  /**
   * Constructor.
   */
  protected PDMeasureDictionary() {
    measureDictionary = new COSDictionary();
    getCOSObject().setName(COSName.TYPE, PDMeasureDictionary.TYPE);
  }

  /**
   * Constructor.
   *
   * @param dictionary the corresponding dictionary
   */
  public PDMeasureDictionary(final COSDictionary dictionary) {
    measureDictionary = dictionary;
  }

  /**
   * This will return the corresponding dictionary.
   *
   * @return the measure dictionary
   */
  @Override
  public COSDictionary getCOSObject() {
    return measureDictionary;
  }

  /**
   * This will return the type of the measure dictionary. It must be "Measure"
   *
   * @return the type
   */
  public String getType() {
    return PDMeasureDictionary.TYPE;
  }

  /**
   * returns the subtype of the measure dictionary.
   *
   * @return the subtype of the measure data dictionary
   */

  public String getSubtype() {
    return getCOSObject().getNameAsString(COSName.SUBTYPE, PDRectlinearMeasureDictionary.SUBTYPE);
  }

  /**
   * This will set the subtype of the measure dictionary.
   *
   * @param subtype the subtype of the measure dictionary
   */
  protected void setSubtype(final String subtype) {
    getCOSObject().setName(COSName.SUBTYPE, subtype);
  }

}
