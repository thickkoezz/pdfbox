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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;

/**
 * A structure element.
 *
 * @author Ben Litchfield
 * @author Johannes Koch
 */
public class PDStructureElement extends PDStructureNode {

  public static final String TYPE = "StructElem";

  /**
   * Constructor with required values.
   *
   * @param structureType the structure type
   * @param parent        the parent structure node
   */
  public PDStructureElement(final String structureType, final PDStructureNode parent) {
    super(PDStructureElement.TYPE);
    setStructureType(structureType);
    setParent(parent);
  }

  /**
   * Constructor for an existing structure element.
   *
   * @param dic The existing dictionary.
   */
  public PDStructureElement(final COSDictionary dic) {
    super(dic);
  }

  /**
   * Returns the structure type (S).
   *
   * @return the structure type
   */
  public String getStructureType() {
    return getCOSObject().getNameAsString(COSName.S);
  }

  /**
   * Sets the structure type (S).
   *
   * @param structureType the structure type
   */
  public final void setStructureType(final String structureType) {
    getCOSObject().setName(COSName.S, structureType);
  }

  /**
   * Returns the parent in the structure hierarchy (P).
   *
   * @return the parent in the structure hierarchy
   */
  public PDStructureNode getParent() {
    final COSBase base = getCOSObject().getDictionaryObject(COSName.P);
    if (base instanceof COSDictionary)
      return PDStructureNode.create((COSDictionary) base);
    return null;
  }

  /**
   * Sets the parent in the structure hierarchy (P).
   *
   * @param structureNode the parent in the structure hierarchy
   */
  public final void setParent(final PDStructureNode structureNode) {
    getCOSObject().setItem(COSName.P, structureNode);
  }

  /**
   * Returns the element identifier (ID).
   *
   * @return the element identifier
   */
  public String getElementIdentifier() {
    return getCOSObject().getString(COSName.ID);
  }

  /**
   * Sets the element identifier (ID).
   *
   * @param id the element identifier
   */
  public void setElementIdentifier(final String id) {
    getCOSObject().setString(COSName.ID, id);
  }

  /**
   * Returns the page on which some or all of the content items designated by the
   * K entry shall be rendered (Pg).
   *
   * @return the page on which some or all of the content items designated by the
   *         K entry shall be rendered
   */
  public PDPage getPage() {
    final COSBase base = getCOSObject().getDictionaryObject(COSName.PG);
    if (base instanceof COSDictionary)
      return new PDPage((COSDictionary) base);
    return null;
  }

  /**
   * Sets the page on which some or all of the content items designated by the K
   * entry shall be rendered (Pg).
   *
   * @param page the page on which some or all of the content items designated by
   *             the K entry shall be rendered.
   */
  public void setPage(final PDPage page) {
    getCOSObject().setItem(COSName.PG, page);
  }

  /**
   * Returns the attributes together with their revision numbers (A).
   *
   * @return the attributes as a list, never null.
   */
  public Revisions<PDAttributeObject> getAttributes() {
    final Revisions<PDAttributeObject> attributes = new Revisions<>();
    final COSBase a = getCOSObject().getDictionaryObject(COSName.A);
    if (a instanceof COSArray) {
      final COSArray aa = (COSArray) a;
      final Iterator<COSBase> it = aa.iterator();
      PDAttributeObject ao = null;
      while (it.hasNext()) {
        COSBase item = it.next();
        if (item instanceof COSObject) {
          item = ((COSObject) item).getObject();
        }
        if (item instanceof COSDictionary) {
          ao = PDAttributeObject.create((COSDictionary) item);
          ao.setStructureElement(this);
          attributes.addObject(ao, 0);
        } else if (item instanceof COSInteger) {
          attributes.setRevisionNumber(ao, ((COSNumber) item).intValue());
        }
      }
    }
    if (a instanceof COSDictionary) {
      final PDAttributeObject ao = PDAttributeObject.create((COSDictionary) a);
      ao.setStructureElement(this);
      attributes.addObject(ao, 0);
    }
    return attributes;
  }

  /**
   * Sets the attributes together with their revision numbers (A).
   *
   * @param attributes the attributes
   */
  public void setAttributes(final Revisions<PDAttributeObject> attributes) {
    final COSName key = COSName.A;
    if (attributes.size() == 1 && attributes.getRevisionNumber(0) == 0) {
      final PDAttributeObject attributeObject = attributes.getObject(0);
      attributeObject.setStructureElement(this);
      getCOSObject().setItem(key, attributeObject);
      return;
    }
    final COSArray array = new COSArray();
    for (int i = 0; i < attributes.size(); i++) {
      final PDAttributeObject attributeObject = attributes.getObject(i);
      attributeObject.setStructureElement(this);
      final int revisionNumber = attributes.getRevisionNumber(i);
      if (revisionNumber < 0)
        throw new IllegalArgumentException("The revision number shall be > -1");
      array.add(attributeObject);
      array.add(COSInteger.get(revisionNumber));
    }
    getCOSObject().setItem(key, array);
  }

  /**
   * Adds an attribute object.
   *
   * @param attributeObject the attribute object
   */
  public void addAttribute(final PDAttributeObject attributeObject) {
    final COSName key = COSName.A;
    attributeObject.setStructureElement(this);
    final COSBase a = getCOSObject().getDictionaryObject(key);
    COSArray array;
    if (a instanceof COSArray) {
      array = (COSArray) a;
    } else {
      array = new COSArray();
      if (a != null) {
        array.add(a);
        array.add(COSInteger.get(0));
      }
    }
    getCOSObject().setItem(key, array);
    array.add(attributeObject);
    array.add(COSInteger.get(getRevisionNumber()));
  }

  /**
   * Removes an attribute object.
   *
   * @param attributeObject the attribute object
   */
  public void removeAttribute(final PDAttributeObject attributeObject) {
    final COSName key = COSName.A;
    final COSBase a = getCOSObject().getDictionaryObject(key);
    if (a instanceof COSArray) {
      final COSArray array = (COSArray) a;
      array.remove(attributeObject.getCOSObject());
      if (array.size() == 2 && array.getInt(1) == 0) {
        getCOSObject().setItem(key, array.getObject(0));
      }
    } else {
      COSBase directA = a;
      if (a instanceof COSObject) {
        directA = ((COSObject) a).getObject();
      }
      if (attributeObject.getCOSObject().equals(directA)) {
        getCOSObject().setItem(key, null);
      }
    }
    attributeObject.setStructureElement(null);
  }

  /**
   * Updates the revision number for the given attribute object.
   *
   * @param attributeObject the attribute object
   */
  public void attributeChanged(final PDAttributeObject attributeObject) {
    final COSName key = COSName.A;
    final COSBase a = getCOSObject().getDictionaryObject(key);
    if (a instanceof COSArray) {
      final COSArray array = (COSArray) a;
      for (int i = 0; i < array.size(); i++) {
        final COSBase entry = array.getObject(i);
        if (entry.equals(attributeObject.getCOSObject())) {
          final COSBase next = array.get(i + 1);
          if (next instanceof COSInteger) {
            array.set(i + 1, COSInteger.get(getRevisionNumber()));
          }
        }
      }
    } else {
      final COSArray array = new COSArray();
      array.add(a);
      array.add(COSInteger.get(getRevisionNumber()));
      getCOSObject().setItem(key, array);
    }
  }

  /**
   * Returns the class names together with their revision numbers (C).
   *
   * @return the class names as a list, never null.
   */
  public Revisions<String> getClassNames() {
    final COSName key = COSName.C;
    final Revisions<String> classNames = new Revisions<>();
    final COSBase c = getCOSObject().getDictionaryObject(key);
    if (c instanceof COSName) {
      classNames.addObject(((COSName) c).getName(), 0);
    }
    if (c instanceof COSArray) {
      final COSArray array = (COSArray) c;
      final Iterator<COSBase> it = array.iterator();
      String className = null;
      while (it.hasNext()) {
        COSBase item = it.next();
        if (item instanceof COSObject) {
          item = ((COSObject) item).getObject();
        }
        if (item instanceof COSName) {
          className = ((COSName) item).getName();
          classNames.addObject(className, 0);
        } else if (item instanceof COSInteger) {
          classNames.setRevisionNumber(className, ((COSNumber) item).intValue());
        }
      }
    }
    return classNames;
  }

  /**
   * Sets the class names together with their revision numbers (C).
   *
   * @param classNames the class names
   */
  public void setClassNames(final Revisions<String> classNames) {
    if (classNames == null)
      return;
    final COSName key = COSName.C;
    if (classNames.size() == 1 && classNames.getRevisionNumber(0) == 0) {
      final String className = classNames.getObject(0);
      getCOSObject().setName(key, className);
      return;
    }
    final COSArray array = new COSArray();
    for (int i = 0; i < classNames.size(); i++) {
      final String className = classNames.getObject(i);
      final int revisionNumber = classNames.getRevisionNumber(i);
      if (revisionNumber < 0)
        throw new IllegalArgumentException("The revision number shall be > -1");
      array.add(COSName.getPDFName(className));
      array.add(COSInteger.get(revisionNumber));
    }
    getCOSObject().setItem(key, array);
  }

  /**
   * Adds a class name.
   *
   * @param className the class name
   */
  public void addClassName(final String className) {
    if (className == null)
      return;
    final COSName key = COSName.C;
    final COSBase c = getCOSObject().getDictionaryObject(key);
    COSArray array;
    if (c instanceof COSArray) {
      array = (COSArray) c;
    } else {
      array = new COSArray();
      if (c != null) {
        array.add(c);
        array.add(COSInteger.get(0));
      }
    }
    getCOSObject().setItem(key, array);
    array.add(COSName.getPDFName(className));
    array.add(COSInteger.get(getRevisionNumber()));
  }

  /**
   * Removes a class name.
   *
   * @param className the class name
   */
  public void removeClassName(final String className) {
    if (className == null)
      return;
    final COSName key = COSName.C;
    final COSBase c = getCOSObject().getDictionaryObject(key);
    final COSName name = COSName.getPDFName(className);
    if (c instanceof COSArray) {
      final COSArray array = (COSArray) c;
      array.remove(name);
      if (array.size() == 2 && array.getInt(1) == 0) {
        getCOSObject().setItem(key, array.getObject(0));
      }
    } else {
      COSBase directC = c;
      if (c instanceof COSObject) {
        directC = ((COSObject) c).getObject();
      }
      if (name.equals(directC)) {
        getCOSObject().setItem(key, null);
      }
    }
  }

  /**
   * Returns the revision number (R).
   *
   * @return the revision number
   */
  public int getRevisionNumber() {
    return getCOSObject().getInt(COSName.R, 0);
  }

  /**
   * Sets the revision number (R).
   *
   * @param revisionNumber the revision number
   */
  public void setRevisionNumber(final int revisionNumber) {
    if (revisionNumber < 0)
      throw new IllegalArgumentException("The revision number shall be > -1");
    getCOSObject().setInt(COSName.R, revisionNumber);
  }

  /**
   * Increments th revision number.
   */
  public void incrementRevisionNumber() {
    setRevisionNumber(getRevisionNumber() + 1);
  }

  /**
   * Returns the title (T).
   *
   * @return the title
   */
  public String getTitle() {
    return getCOSObject().getString(COSName.T);
  }

  /**
   * Sets the title (T).
   *
   * @param title the title
   */
  public void setTitle(final String title) {
    getCOSObject().setString(COSName.T, title);
  }

  /**
   * Returns the language (Lang).
   *
   * @return the language
   */
  public String getLanguage() {
    return getCOSObject().getString(COSName.LANG);
  }

  /**
   * Sets the language (Lang).
   *
   * @param language the language
   */
  public void setLanguage(final String language) {
    getCOSObject().setString(COSName.LANG, language);
  }

  /**
   * Returns the alternate description (Alt).
   *
   * @return the alternate description
   */
  public String getAlternateDescription() {
    return getCOSObject().getString(COSName.ALT);
  }

  /**
   * Sets the alternate description (Alt).
   *
   * @param alternateDescription the alternate description
   */
  public void setAlternateDescription(final String alternateDescription) {
    getCOSObject().setString(COSName.ALT, alternateDescription);
  }

  /**
   * Returns the expanded form (E).
   *
   * @return the expanded form
   */
  public String getExpandedForm() {
    return getCOSObject().getString(COSName.E);
  }

  /**
   * Sets the expanded form (E).
   *
   * @param expandedForm the expanded form
   */
  public void setExpandedForm(final String expandedForm) {
    getCOSObject().setString(COSName.E, expandedForm);
  }

  /**
   * Returns the actual text (ActualText).
   *
   * @return the actual text
   */
  public String getActualText() {
    return getCOSObject().getString(COSName.ACTUAL_TEXT);
  }

  /**
   * Sets the actual text (ActualText).
   *
   * @param actualText the actual text
   */
  public void setActualText(final String actualText) {
    getCOSObject().setString(COSName.ACTUAL_TEXT, actualText);
  }

  /**
   * Returns the standard structure type, the actual structure type is mapped to
   * in the role map.
   *
   * @return the standard structure type
   */
  public String getStandardStructureType() {
    String type = getStructureType();
    final Map<String, Object> roleMap = getRoleMap();
    if (roleMap.containsKey(type)) {
      final Object mappedValue = getRoleMap().get(type);
      if (mappedValue instanceof String) {
        type = (String) mappedValue;
      }
    }
    return type;
  }

  /**
   * Appends a marked-content sequence kid.
   *
   * @param markedContent the marked-content sequence
   */
  public void appendKid(final PDMarkedContent markedContent) {
    if (markedContent == null)
      return;
    this.appendKid(COSInteger.get(markedContent.getMCID()));
  }

  /**
   * Appends a marked-content reference kid.
   *
   * @param markedContentReference the marked-content reference
   */
  public void appendKid(final PDMarkedContentReference markedContentReference) {
    appendObjectableKid(markedContentReference);
  }

  /**
   * Appends an object reference kid.
   *
   * @param objectReference the object reference
   */
  public void appendKid(final PDObjectReference objectReference) {
    appendObjectableKid(objectReference);
  }

  /**
   * Inserts a marked-content identifier kid before a reference kid.
   *
   * @param markedContentIdentifier the marked-content identifier
   * @param refKid                  the reference kid
   */
  public void insertBefore(final COSInteger markedContentIdentifier, final Object refKid) {
    this.insertBefore((COSBase) markedContentIdentifier, refKid);
  }

  /**
   * Inserts a marked-content reference kid before a reference kid.
   *
   * @param markedContentReference the marked-content reference
   * @param refKid                 the reference kid
   */
  public void insertBefore(final PDMarkedContentReference markedContentReference, final Object refKid) {
    insertObjectableBefore(markedContentReference, refKid);
  }

  /**
   * Inserts an object reference kid before a reference kid.
   *
   * @param objectReference the object reference
   * @param refKid          the reference kid
   */
  public void insertBefore(final PDObjectReference objectReference, final Object refKid) {
    insertObjectableBefore(objectReference, refKid);
  }

  /**
   * Removes a marked-content identifier kid.
   *
   * @param markedContentIdentifier the marked-content identifier
   */
  public void removeKid(final COSInteger markedContentIdentifier) {
    this.removeKid((COSBase) markedContentIdentifier);
  }

  /**
   * Removes a marked-content reference kid.
   *
   * @param markedContentReference the marked-content reference
   */
  public void removeKid(final PDMarkedContentReference markedContentReference) {
    removeObjectableKid(markedContentReference);
  }

  /**
   * Removes an object reference kid.
   *
   * @param objectReference the object reference
   */
  public void removeKid(final PDObjectReference objectReference) {
    removeObjectableKid(objectReference);
  }

  /**
   * Returns the structure tree root.
   *
   * @return the structure tree root
   */
  private PDStructureTreeRoot getStructureTreeRoot() {
    PDStructureNode parent = getParent();
    while (parent instanceof PDStructureElement) {
      parent = ((PDStructureElement) parent).getParent();
    }
    if (parent instanceof PDStructureTreeRoot)
      return (PDStructureTreeRoot) parent;
    return null;
  }

  /**
   * Returns the role map.
   *
   * @return the role map
   */
  private Map<String, Object> getRoleMap() {
    final PDStructureTreeRoot root = getStructureTreeRoot();
    if (root != null)
      return root.getRoleMap();
    return Collections.emptyMap();
  }

}
