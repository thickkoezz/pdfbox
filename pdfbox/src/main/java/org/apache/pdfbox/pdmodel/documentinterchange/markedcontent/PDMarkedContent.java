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
package org.apache.pdfbox.pdmodel.documentinterchange.markedcontent;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.PDArtifactMarkedContent;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.text.TextPosition;

/**
 * A marked content.
 *
 * @author Johannes Koch
 */
public class PDMarkedContent {

  /**
   * Creates a marked-content sequence.
   *
   * @param tag        the tag
   * @param properties the properties
   * @return the marked-content sequence
   */
  public static PDMarkedContent create(final COSName tag, final COSDictionary properties) {
    if (COSName.ARTIFACT.equals(tag))
      return new PDArtifactMarkedContent(properties);
    return new PDMarkedContent(tag, properties);
  }

  private final String tag;
  private final COSDictionary properties;
  private final List<Object> contents;

  /**
   * Creates a new marked content object.
   *
   * @param tag        the tag
   * @param properties the properties
   */
  public PDMarkedContent(final COSName tag, final COSDictionary properties) {
    this.tag = tag == null ? null : tag.getName();
    this.properties = properties;
    contents = new ArrayList<>();
  }

  /**
   * Gets the tag.
   *
   * @return the tag
   */
  public String getTag() {
    return tag;
  }

  /**
   * Gets the properties.
   *
   * @return the properties
   */
  public COSDictionary getProperties() {
    return properties;
  }

  /**
   * Gets the marked-content identifier.
   *
   * @return the marked-content identifier, or -1 if it doesn't exist.
   */
  public int getMCID() {
    return getProperties() == null ? -1 : getProperties().getInt(COSName.MCID);
  }

  /**
   * Gets the language (Lang).
   *
   * @return the language
   */
  public String getLanguage() {
    return getProperties() == null ? null : getProperties().getNameAsString(COSName.LANG);
  }

  /**
   * Gets the actual text (ActualText).
   *
   * @return the actual text
   */
  public String getActualText() {
    return getProperties() == null ? null : getProperties().getString(COSName.ACTUAL_TEXT);
  }

  /**
   * Gets the alternate description (Alt).
   *
   * @return the alternate description
   */
  public String getAlternateDescription() {
    return getProperties() == null ? null : getProperties().getString(COSName.ALT);
  }

  /**
   * Gets the expanded form (E).
   *
   * @return the expanded form
   */
  public String getExpandedForm() {
    return getProperties() == null ? null : getProperties().getString(COSName.E);
  }

  /**
   * Gets the contents of the marked content sequence. Can be
   * <ul>
   * <li>{@link TextPosition},</li>
   * <li>{@link PDMarkedContent}, or</li>
   * <li>{@link PDXObject}.</li>
   * </ul>
   *
   * @return the contents of the marked content sequence
   */
  public List<Object> getContents() {
    return contents;
  }

  /**
   * Adds a text position to the contents.
   *
   * @param text the text position
   */
  public void addText(final TextPosition text) {
    getContents().add(text);
  }

  /**
   * Adds a marked content to the contents.
   *
   * @param markedContent the marked content
   */
  public void addMarkedContent(final PDMarkedContent markedContent) {
    getContents().add(markedContent);
  }

  /**
   * Adds an XObject to the contents.
   *
   * @param xobject the XObject
   */
  public void addXObject(final PDXObject xobject) {
    getContents().add(xobject);
  }

  @Override
  public String toString() {
    return "tag=" + tag + ", properties=" + properties + ", contents=" + contents;
  }

}
