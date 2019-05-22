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
package org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDAttributeObject;

/**
 * A Table attribute object.
 *
 * @author Johannes Koch
 */
public class PDTableAttributeObject extends PDStandardAttributeObject {

  /**
   * standard attribute owner: Table
   */
  public static final String OWNER_TABLE = "Table";

  protected static final String ROW_SPAN = "RowSpan";
  protected static final String COL_SPAN = "ColSpan";
  protected static final String HEADERS = "Headers";
  protected static final String SCOPE = "Scope";
  protected static final String SUMMARY = "Summary";

  /**
   * Scope: Both
   */
  public static final String SCOPE_BOTH = "Both";
  /**
   * Scope: Column
   */
  public static final String SCOPE_COLUMN = "Column";
  /**
   * Scope: Row
   */
  public static final String SCOPE_ROW = "Row";

  /**
   * Default constructor.
   */
  public PDTableAttributeObject() {
    setOwner(PDTableAttributeObject.OWNER_TABLE);
  }

  /**
   * Creates a new Table attribute object with a given dictionary.
   *
   * @param dictionary the dictionary
   */
  public PDTableAttributeObject(final COSDictionary dictionary) {
    super(dictionary);
  }

  /**
   * Gets the number of rows in the enclosing table that shall be spanned by the
   * cell (RowSpan). The default value is 1.
   *
   * @return the row span
   */
  public int getRowSpan() {
    return getInteger(PDTableAttributeObject.ROW_SPAN, 1);
  }

  /**
   * Sets the number of rows in the enclosing table that shall be spanned by the
   * cell (RowSpan).
   *
   * @param rowSpan the row span
   */
  public void setRowSpan(final int rowSpan) {
    setInteger(PDTableAttributeObject.ROW_SPAN, rowSpan);
  }

  /**
   * Gets the number of columns in the enclosing table that shall be spanned by
   * the cell (ColSpan). The default value is 1.
   *
   * @return the column span
   */
  public int getColSpan() {
    return getInteger(PDTableAttributeObject.COL_SPAN, 1);
  }

  /**
   * Sets the number of columns in the enclosing table that shall be spanned by
   * the cell (ColSpan).
   *
   * @param colSpan the column span
   */
  public void setColSpan(final int colSpan) {
    setInteger(PDTableAttributeObject.COL_SPAN, colSpan);
  }

  /**
   * Gets the headers (Headers). An array of byte strings, where each string shall
   * be the element identifier (see the
   * {@link org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement#getElementIdentifier()})
   * for a TH structure element that shall be used as a header associated with
   * this cell.
   *
   * @return the headers.
   */
  public String[] getHeaders() {
    return getArrayOfString(PDTableAttributeObject.HEADERS);
  }

  /**
   * Sets the headers (Headers). An array of byte strings, where each string shall
   * be the element identifier (see the
   * {@link org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement#getElementIdentifier()})
   * for a TH structure element that shall be used as a header associated with
   * this cell.
   *
   * @param headers the headers
   */
  public void setHeaders(final String[] headers) {
    setArrayOfString(PDTableAttributeObject.HEADERS, headers);
  }

  /**
   * Gets the scope (Scope). It shall reflect whether the header cell applies to
   * the rest of the cells in the row that contains it, the column that contains
   * it, or both the row and the column that contain it.
   *
   * @return the scope
   */
  public String getScope() {
    return this.getName(PDTableAttributeObject.SCOPE);
  }

  /**
   * Sets the scope (Scope). It shall reflect whether the header cell applies to
   * the rest of the cells in the row that contains it, the column that contains
   * it, or both the row and the column that contain it. The value shall be one of
   * the following:
   * <ul>
   * <li>{@link #SCOPE_ROW},</li>
   * <li>{@link #SCOPE_COLUMN}, or</li>
   * <li>{@link #SCOPE_BOTH}.</li>
   * </ul>
   *
   * @param scope the scope
   */
  public void setScope(final String scope) {
    setName(PDTableAttributeObject.SCOPE, scope);
  }

  /**
   * Gets the summary of the table’s purpose and structure.
   *
   * @return the summary
   */
  public String getSummary() {
    return getString(PDTableAttributeObject.SUMMARY);
  }

  /**
   * Sets the summary of the table’s purpose and structure.
   *
   * @param summary the summary
   */
  public void setSummary(final String summary) {
    setString(PDTableAttributeObject.SUMMARY, summary);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder().append(super.toString());
    if (isSpecified(PDTableAttributeObject.ROW_SPAN)) {
      sb.append(", RowSpan=").append(String.valueOf(getRowSpan()));
    }
    if (isSpecified(PDTableAttributeObject.COL_SPAN)) {
      sb.append(", ColSpan=").append(String.valueOf(getColSpan()));
    }
    if (isSpecified(PDTableAttributeObject.HEADERS)) {
      sb.append(", Headers=").append(PDAttributeObject.arrayToString(getHeaders()));
    }
    if (isSpecified(PDTableAttributeObject.SCOPE)) {
      sb.append(", Scope=").append(getScope());
    }
    if (isSpecified(PDTableAttributeObject.SUMMARY)) {
      sb.append(", Summary=").append(getSummary());
    }
    return sb.toString();
  }

}
