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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDAttributeObject;
import org.apache.pdfbox.pdmodel.graphics.color.PDGamma;

/**
 * A Layout attribute object.
 *
 * @author Johannes Koch
 */
public class PDLayoutAttributeObject extends PDStandardAttributeObject {

  /**
   * standard attribute owner: Layout
   */
  public static final String OWNER_LAYOUT = "Layout";

  private static final String PLACEMENT = "Placement";
  private static final String WRITING_MODE = "WritingMode";
  private static final String BACKGROUND_COLOR = "BackgroundColor";
  private static final String BORDER_COLOR = "BorderColor";
  private static final String BORDER_STYLE = "BorderStyle";
  private static final String BORDER_THICKNESS = "BorderThickness";
  private static final String PADDING = "Padding";
  private static final String COLOR = "Color";
  private static final String SPACE_BEFORE = "SpaceBefore";
  private static final String SPACE_AFTER = "SpaceAfter";
  private static final String START_INDENT = "StartIndent";
  private static final String END_INDENT = "EndIndent";
  private static final String TEXT_INDENT = "TextIndent";
  private static final String TEXT_ALIGN = "TextAlign";
  private static final String BBOX = "BBox";
  private static final String WIDTH = "Width";
  private static final String HEIGHT = "Height";
  private static final String BLOCK_ALIGN = "BlockAlign";
  private static final String INLINE_ALIGN = "InlineAlign";
  private static final String T_BORDER_STYLE = "TBorderStyle";
  private static final String T_PADDING = "TPadding";
  private static final String BASELINE_SHIFT = "BaselineShift";
  private static final String LINE_HEIGHT = "LineHeight";
  private static final String TEXT_DECORATION_COLOR = "TextDecorationColor";
  private static final String TEXT_DECORATION_THICKNESS = "TextDecorationThickness";
  private static final String TEXT_DECORATION_TYPE = "TextDecorationType";
  private static final String RUBY_ALIGN = "RubyAlign";
  private static final String RUBY_POSITION = "RubyPosition";
  private static final String GLYPH_ORIENTATION_VERTICAL = "GlyphOrientationVertical";
  private static final String COLUMN_COUNT = "ColumnCount";
  private static final String COLUMN_GAP = "ColumnGap";
  private static final String COLUMN_WIDTHS = "ColumnWidths";

  /**
   * Placement: Block: Stacked in the block-progression direction within an
   * enclosing reference area or parent BLSE.
   */
  public static final String PLACEMENT_BLOCK = "Block";
  /**
   * Placement: Inline: Packed in the inline-progression direction within an
   * enclosing BLSE.
   */
  public static final String PLACEMENT_INLINE = "Inline";
  /**
   * Placement: Before: Placed so that the before edge of the element’s allocation
   * rectangle coincides with that of the nearest enclosing reference area. The
   * element may float, if necessary, to achieve the specified placement. The
   * element shall be treated as a block occupying the full extent of the
   * enclosing reference area in the inline direction. Other content shall be
   * stacked so as to begin at the after edge of the element’s allocation
   * rectangle.
   */
  public static final String PLACEMENT_BEFORE = "Before";
  /**
   * Placement: Start: Placed so that the start edge of the element’s allocation
   * rectangle coincides with that of the nearest enclosing reference area. The
   * element may float, if necessary, to achieve the specified placement. Other
   * content that would intrude into the element’s allocation rectangle shall be
   * laid out as a runaround.
   */
  public static final String PLACEMENT_START = "Start";
  /**
   * Placement: End: Placed so that the end edge of the element’s allocation
   * rectangle coincides with that of the nearest enclosing reference area. The
   * element may float, if necessary, to achieve the specified placement. Other
   * content that would intrude into the element’s allocation rectangle shall be
   * laid out as a runaround.
   */
  public static final String PLACEMENT_END = "End";
  /**
   * WritingMode: LrTb: Inline progression from left to right; block progression
   * from top to bottom. This is the typical writing mode for Western writing
   * systems.
   */
  public static final String WRITING_MODE_LRTB = "LrTb";
  /**
   * WritingMode: RlTb: Inline progression from right to left; block progression
   * from top to bottom. This is the typical writing mode for Arabic and Hebrew
   * writing systems.
   */
  public static final String WRITING_MODE_RLTB = "RlTb";
  /**
   * WritingMode: TbRl: Inline progression from top to bottom; block progression
   * from right to left. This is the typical writing mode for Chinese and Japanese
   * writing systems.
   */
  public static final String WRITING_MODE_TBRL = "TbRl";
  /**
   * BorderStyle: None: No border. Forces the computed value of BorderThickness to
   * be 0.
   */
  public static final String BORDER_STYLE_NONE = "None";
  /**
   * BorderStyle: Hidden: Same as {@link #BORDER_STYLE_NONE}, except in terms of
   * border conflict resolution for table elements.
   */
  public static final String BORDER_STYLE_HIDDEN = "Hidden";
  /**
   * BorderStyle: Dotted: The border is a series of dots.
   */
  public static final String BORDER_STYLE_DOTTED = "Dotted";
  /**
   * BorderStyle: Dashed: The border is a series of short line segments.
   */
  public static final String BORDER_STYLE_DASHED = "Dashed";
  /**
   * BorderStyle: Solid: The border is a single line segment.
   */
  public static final String BORDER_STYLE_SOLID = "Solid";
  /**
   * BorderStyle: Double: The border is two solid lines. The sum of the two lines
   * and the space between them equals the value of BorderThickness.
   */
  public static final String BORDER_STYLE_DOUBLE = "Double";
  /**
   * BorderStyle: Groove: The border looks as though it were carved into the
   * canvas.
   */
  public static final String BORDER_STYLE_GROOVE = "Groove";
  /**
   * BorderStyle: Ridge: The border looks as though it were coming out of the
   * canvas (the opposite of {@link #BORDER_STYLE_GROOVE}).
   */
  public static final String BORDER_STYLE_RIDGE = "Ridge";
  /**
   * BorderStyle: Inset: The border makes the entire box look as though it were
   * embedded in the canvas.
   */
  public static final String BORDER_STYLE_INSET = "Inset";
  /**
   * BorderStyle: Outset: The border makes the entire box look as though it were
   * coming out of the canvas (the opposite of {@link #BORDER_STYLE_INSET}.
   */
  public static final String BORDER_STYLE_OUTSET = "Outset";
  /**
   * TextAlign: Start: Aligned with the start edge.
   */
  public static final String TEXT_ALIGN_START = "Start";
  /**
   * TextAlign: Center: Centered between the start and end edges.
   */
  public static final String TEXT_ALIGN_CENTER = "Center";
  /**
   * TextAlign: End: Aligned with the end edge.
   */
  public static final String TEXT_ALIGN_END = "End";
  /**
   * TextAlign: Justify: Aligned with both the start and end edges, with internal
   * spacing within each line expanded, if necessary, to achieve such alignment.
   * The last (or only) line shall be aligned with the start edge only.
   */
  public static final String TEXT_ALIGN_JUSTIFY = "Justify";
  /**
   * Width: Auto
   */
  public static final String WIDTH_AUTO = "Auto";
  /**
   * Height: Auto
   */
  public static final String HEIGHT_AUTO = "Auto";
  /**
   * BlockAlign: Before: Before edge of the first child’s allocation rectangle
   * aligned with that of the table cell’s content rectangle.
   */
  public static final String BLOCK_ALIGN_BEFORE = "Before";
  /**
   * BlockAlign: Middle: Children centered within the table cell. The distance
   * between the before edge of the first child’s allocation rectangle and that of
   * the table cell’s content rectangle shall be the same as the distance between
   * the after edge of the last child’s allocation rectangle and that of the table
   * cell’s content rectangle.
   */
  public static final String BLOCK_ALIGN_MIDDLE = "Middle";
  /**
   * BlockAlign: After: After edge of the last child’s allocation rectangle
   * aligned with that of the table cell’s content rectangle.
   */
  public static final String BLOCK_ALIGN_AFTER = "After";
  /**
   * BlockAlign: Justify: Children aligned with both the before and after edges of
   * the table cell’s content rectangle. The first child shall be placed as
   * described for {@link #BLOCK_ALIGN_BEFORE} and the last child as described for
   * {@link #BLOCK_ALIGN_AFTER}, with equal spacing between the children. If there
   * is only one child, it shall be aligned with the before edge only, as for
   * {@link #BLOCK_ALIGN_BEFORE}.
   */
  public static final String BLOCK_ALIGN_JUSTIFY = "Justify";
  /**
   * InlineAlign: Start: Start edge of each child’s allocation rectangle aligned
   * with that of the table cell’s content rectangle.
   */
  public static final String INLINE_ALIGN_START = "Start";
  /**
   * InlineAlign: Center: Each child centered within the table cell. The distance
   * between the start edges of the child’s allocation rectangle and the table
   * cell’s content rectangle shall be the same as the distance between their end
   * edges.
   */
  public static final String INLINE_ALIGN_CENTER = "Center";
  /**
   * InlineAlign: End: End edge of each child’s allocation rectangle aligned with
   * that of the table cell’s content rectangle.
   */
  public static final String INLINE_ALIGN_END = "End";
  /**
   * LineHeight: NormalAdjust the line height to include any nonzero value
   * specified for BaselineShift.
   */
  public static final String LINE_HEIGHT_NORMAL = "Normal";
  /**
   * LineHeight: Auto: Adjustment for the value of BaselineShift shall not be
   * made.
   */
  public static final String LINE_HEIGHT_AUTO = "Auto";
  /**
   * TextDecorationType: None: No text decoration
   */
  public static final String TEXT_DECORATION_TYPE_NONE = "None";
  /**
   * TextDecorationType: Underline: A line below the text
   */
  public static final String TEXT_DECORATION_TYPE_UNDERLINE = "Underline";
  /**
   * TextDecorationType: Overline: A line above the text
   */
  public static final String TEXT_DECORATION_TYPE_OVERLINE = "Overline";
  /**
   * TextDecorationType: LineThrough: A line through the middle of the text
   */
  public static final String TEXT_DECORATION_TYPE_LINE_THROUGH = "LineThrough";
  /**
   * RubyAlign: Start: The content shall be aligned on the start edge in the
   * inline-progression direction.
   */
  public static final String RUBY_ALIGN_START = "Start";
  /**
   * RubyAlign: Center: The content shall be centered in the inline-progression
   * direction.
   */
  public static final String RUBY_ALIGN_CENTER = "Center";
  /**
   * RubyAlign: End: The content shall be aligned on the end edge in the
   * inline-progression direction.
   */
  public static final String RUBY_ALIGN_END = "End";
  /**
   * RubyAlign: Justify: The content shall be expanded to fill the available width
   * in the inline-progression direction.
   */
  public static final String RUBY_ALIGN_JUSTIFY = "Justify";
  /**
   * RubyAlign: Distribute: The content shall be expanded to fill the available
   * width in the inline-progression direction. However, space shall also be
   * inserted at the start edge and end edge of the text. The spacing shall be
   * distributed using a 1:2:1 (start:infix:end) ratio. It shall be changed to a
   * 0:1:1 ratio if the ruby appears at the start of a text line or to a 1:1:0
   * ratio if the ruby appears at the end of the text line.
   */
  public static final String RUBY_ALIGN_DISTRIBUTE = "Distribute";
  /**
   * RubyPosition: Before: The RT content shall be aligned along the before edge
   * of the element.
   */
  public static final String RUBY_POSITION_BEFORE = "Before";
  /**
   * RubyPosition: After: The RT content shall be aligned along the after edge of
   * the element.
   */
  public static final String RUBY_POSITION_AFTER = "After";
  /**
   * RubyPosition: Warichu: The RT and associated RP elements shall be formatted
   * as a warichu, following the RB element.
   */
  public static final String RUBY_POSITION_WARICHU = "Warichu";
  /**
   * RubyPosition: Inline: The RT and associated RP elements shall be formatted as
   * a parenthesis comment, following the RB element.
   */
  public static final String RUBY_POSITION_INLINE = "Inline";
  /**
   * GlyphOrientationVertical: Auto
   */
  public static final String GLYPH_ORIENTATION_VERTICAL_AUTO = "Auto";
  /**
   * GlyphOrientationVertical: -180°
   */
  public static final String GLYPH_ORIENTATION_VERTICAL_MINUS_180_DEGREES = "-180";
  /**
   * GlyphOrientationVertical: -90°
   */
  public static final String GLYPH_ORIENTATION_VERTICAL_MINUS_90_DEGREES = "-90";
  /**
   * GlyphOrientationVertical: 0°
   */
  public static final String GLYPH_ORIENTATION_VERTICAL_ZERO_DEGREES = "0";
  /**
   * GlyphOrientationVertical: 90°
   */
  public static final String GLYPH_ORIENTATION_VERTICAL_90_DEGREES = "90";
  /**
   * GlyphOrientationVertical: 180°
   */
  public static final String GLYPH_ORIENTATION_VERTICAL_180_DEGREES = "180";
  /**
   * GlyphOrientationVertical: 270°
   */
  public static final String GLYPH_ORIENTATION_VERTICAL_270_DEGREES = "270";
  /**
   * GlyphOrientationVertical: 360°
   */
  public static final String GLYPH_ORIENTATION_VERTICAL_360_DEGREES = "360";

  /**
   * Default constructor.
   */
  public PDLayoutAttributeObject() {
    setOwner(PDLayoutAttributeObject.OWNER_LAYOUT);
  }

  /**
   * Creates a new Layout attribute object with a given dictionary.
   *
   * @param dictionary the dictionary
   */
  public PDLayoutAttributeObject(final COSDictionary dictionary) {
    super(dictionary);
  }

  /**
   * Gets the positioning of the element with respect to the enclosing reference
   * area and other content (Placement). The default value is
   * {@link #PLACEMENT_INLINE}.
   *
   * @return the placement
   */
  public String getPlacement() {
    return this.getName(PDLayoutAttributeObject.PLACEMENT, PDLayoutAttributeObject.PLACEMENT_INLINE);
  }

  /**
   * Sets the positioning of the element with respect to the enclosing reference
   * area and other content (Placement). The value should be one of:
   * <ul>
   * <li>{@link #PLACEMENT_BLOCK},</li>
   * <li>{@link #PLACEMENT_INLINE},</li>
   * <li>{@link #PLACEMENT_BEFORE},</li>
   * <li>{@link #PLACEMENT_START},</li>
   * <li>{@link #PLACEMENT_END}.</li>
   * </ul>
   *
   * @param placement the placement
   */
  public void setPlacement(final String placement) {
    setName(PDLayoutAttributeObject.PLACEMENT, placement);
  }

  /**
   * Gets the writing mode (WritingMode). The default value is
   * {@link #WRITING_MODE_LRTB}.
   *
   * @return the writing mode
   */
  public String getWritingMode() {
    return this.getName(PDLayoutAttributeObject.WRITING_MODE, PDLayoutAttributeObject.WRITING_MODE_LRTB);
  }

  /**
   * Sets the writing mode (WritingMode). The value should be one of:
   * <ul>
   * <li>{@link #WRITING_MODE_LRTB},</li>
   * <li>{@link #WRITING_MODE_RLTB},</li>
   * <li>{@link #WRITING_MODE_TBRL}.</li>
   * </ul>
   *
   * @param writingMode the writing mode
   */
  public void setWritingMode(final String writingMode) {
    setName(PDLayoutAttributeObject.WRITING_MODE, writingMode);
  }

  /**
   * Gets the background colour (BackgroundColor).
   *
   * @return the background colour
   */
  public PDGamma getBackgroundColor() {
    return this.getColor(PDLayoutAttributeObject.BACKGROUND_COLOR);
  }

  /**
   * Sets the background colour (BackgroundColor).
   *
   * @param backgroundColor the background colour
   */
  public void setBackgroundColor(final PDGamma backgroundColor) {
    this.setColor(PDLayoutAttributeObject.BACKGROUND_COLOR, backgroundColor);
  }

  /**
   * Gets the border colour (BorderColor).
   *
   * @return a single border colour ({@link PDGamma}) or four border colours
   *         ({@link PDFourColours})
   */
  public Object getBorderColors() {
    return getColorOrFourColors(PDLayoutAttributeObject.BORDER_COLOR);
  }

  /**
   * Sets the same border colour for all four sides (BorderColor).
   *
   * @param borderColor the border colour
   */
  public void setAllBorderColors(final PDGamma borderColor) {
    this.setColor(PDLayoutAttributeObject.BORDER_COLOR, borderColor);
  }

  /**
   * Sets the border colours for four sides separately (BorderColor).
   *
   * @param borderColors the border colours
   */
  public void setBorderColors(final PDFourColours borderColors) {
    setFourColors(PDLayoutAttributeObject.BORDER_COLOR, borderColors);
  }

  /**
   * Gets the border style (BorderStyle). The default value is
   * {@link #BORDER_STYLE_NONE}.
   *
   * @return the border styles (a String or an array of four Strings)
   */
  public Object getBorderStyle() {
    return getNameOrArrayOfName(PDLayoutAttributeObject.BORDER_STYLE, PDLayoutAttributeObject.BORDER_STYLE_NONE);
  }

  /**
   * Sets the same border style for all four sides (BorderStyle). The value should
   * be one of:
   * <ul>
   * <li>{@link #BORDER_STYLE_NONE},</li>
   * <li>{@link #BORDER_STYLE_HIDDEN},</li>
   * <li>{@link #BORDER_STYLE_DOTTED},</li>
   * <li>{@link #BORDER_STYLE_DASHED},</li>
   * <li>{@link #BORDER_STYLE_SOLID},</li>
   * <li>{@link #BORDER_STYLE_DOUBLE},</li>
   * <li>{@link #BORDER_STYLE_GROOVE},</li>
   * <li>{@link #BORDER_STYLE_RIDGE},</li>
   * <li>{@link #BORDER_STYLE_INSET},</li>
   * <li>{@link #BORDER_STYLE_OUTSET}.</li>
   * </ul>
   *
   * @param borderStyle the border style
   */
  public void setAllBorderStyles(final String borderStyle) {
    setName(PDLayoutAttributeObject.BORDER_STYLE, borderStyle);
  }

  /**
   * Sets the border styles for four sides separately (BorderStyle). The values
   * should be of:
   * <ul>
   * <li>{@link #BORDER_STYLE_NONE},</li>
   * <li>{@link #BORDER_STYLE_HIDDEN},</li>
   * <li>{@link #BORDER_STYLE_DOTTED},</li>
   * <li>{@link #BORDER_STYLE_DASHED},</li>
   * <li>{@link #BORDER_STYLE_SOLID},</li>
   * <li>{@link #BORDER_STYLE_DOUBLE},</li>
   * <li>{@link #BORDER_STYLE_GROOVE},</li>
   * <li>{@link #BORDER_STYLE_RIDGE},</li>
   * <li>{@link #BORDER_STYLE_INSET},</li>
   * <li>{@link #BORDER_STYLE_OUTSET}.</li>
   * </ul>
   *
   * @param borderStyles the border styles (an array of four Strings)
   */
  public void setBorderStyles(final String[] borderStyles) {
    setArrayOfName(PDLayoutAttributeObject.BORDER_STYLE, borderStyles);
  }

  /**
   * Gets the border thickness (BorderThickness).
   *
   * @return the border thickness (a Float or an array of four floats)
   */
  public Object getBorderThickness() {
    return getNumberOrArrayOfNumber(PDLayoutAttributeObject.BORDER_THICKNESS, PDStandardAttributeObject.UNSPECIFIED);
  }

  /**
   * Sets the same border thickness for all four sides (BorderThickness).
   *
   * @param borderThickness the border thickness
   */
  public void setAllBorderThicknesses(final float borderThickness) {
    this.setNumber(PDLayoutAttributeObject.BORDER_THICKNESS, borderThickness);
  }

  /**
   * Sets the same border thickness for all four sides (BorderThickness).
   *
   * @param borderThickness the border thickness
   */
  public void setAllBorderThicknesses(final int borderThickness) {
    this.setNumber(PDLayoutAttributeObject.BORDER_THICKNESS, borderThickness);
  }

  /**
   * Sets the border thicknesses for four sides separately (BorderThickness).
   *
   * @param borderThicknesses the border thickness (an array of four floats)
   */
  public void setBorderThicknesses(final float[] borderThicknesses) {
    setArrayOfNumber(PDLayoutAttributeObject.BORDER_THICKNESS, borderThicknesses);
  }

  /**
   * Gets the padding (Padding). The default value is 0.
   *
   * @return the padding (a Float or an array of float)
   */
  public Object getPadding() {
    return getNumberOrArrayOfNumber(PDLayoutAttributeObject.PADDING, 0.f);
  }

  /**
   * Sets the same padding for all four sides (Padding).
   *
   * @param padding the padding
   */
  public void setAllPaddings(final float padding) {
    this.setNumber(PDLayoutAttributeObject.PADDING, padding);
  }

  /**
   * Sets the same padding for all four sides (Padding).
   *
   * @param padding the padding
   */
  public void setAllPaddings(final int padding) {
    this.setNumber(PDLayoutAttributeObject.PADDING, padding);
  }

  /**
   * Sets the paddings for four sides separately (Padding).
   *
   * @param paddings the paddings (an array of four floats)
   */
  public void setPaddings(final float[] paddings) {
    setArrayOfNumber(PDLayoutAttributeObject.PADDING, paddings);
  }

  /**
   * Gets the color to be used for drawing text and the default value for the
   * colour of table borders and text decorations (Color).
   *
   * @return the colour
   */
  public PDGamma getColor() {
    return this.getColor(PDLayoutAttributeObject.COLOR);
  }

  /**
   * Sets the color to be used for drawing text and the default value for the
   * colour of table borders and text decorations (Color).
   *
   * @param color the colour
   */
  public void setColor(final PDGamma color) {
    this.setColor(PDLayoutAttributeObject.COLOR, color);
  }

  /**
   * Gets the amount of extra space preceding the before edge of the BLSE in the
   * block-progression direction (SpaceBefore). The default value is 0.
   *
   * @return the space before
   */
  public float getSpaceBefore() {
    return this.getNumber(PDLayoutAttributeObject.SPACE_BEFORE, 0.f);
  }

  /**
   * Sets the amount of extra space preceding the before edge of the BLSE in the
   * block-progression direction (SpaceBefore).
   *
   * @param spaceBefore the space before
   */
  public void setSpaceBefore(final float spaceBefore) {
    this.setNumber(PDLayoutAttributeObject.SPACE_BEFORE, spaceBefore);
  }

  /**
   * Sets the amount of extra space preceding the before edge of the BLSE in the
   * block-progression direction (SpaceBefore).
   *
   * @param spaceBefore the space before
   */
  public void setSpaceBefore(final int spaceBefore) {
    this.setNumber(PDLayoutAttributeObject.SPACE_BEFORE, spaceBefore);
  }

  /**
   * Gets the amount of extra space following the after edge of the BLSE in the
   * block-progression direction (SpaceAfter). The default value is 0.
   *
   * @return the space after
   */
  public float getSpaceAfter() {
    return this.getNumber(PDLayoutAttributeObject.SPACE_AFTER, 0.f);
  }

  /**
   * Sets the amount of extra space following the after edge of the BLSE in the
   * block-progression direction (SpaceAfter).
   *
   * @param spaceAfter the space after
   */
  public void setSpaceAfter(final float spaceAfter) {
    this.setNumber(PDLayoutAttributeObject.SPACE_AFTER, spaceAfter);
  }

  /**
   * Sets the amount of extra space following the after edge of the BLSE in the
   * block-progression direction (SpaceAfter).
   *
   * @param spaceAfter the space after
   */
  public void setSpaceAfter(final int spaceAfter) {
    this.setNumber(PDLayoutAttributeObject.SPACE_AFTER, spaceAfter);
  }

  /**
   * Gets the distance from the start edge of the reference area to that of the
   * BLSE in the inline-progression direction (StartIndent). The default value is
   * 0.
   *
   * @return the start indent
   */
  public float getStartIndent() {
    return this.getNumber(PDLayoutAttributeObject.START_INDENT, 0.f);
  }

  /**
   * Sets the distance from the start edge of the reference area to that of the
   * BLSE in the inline-progression direction (StartIndent).
   *
   * @param startIndent the start indent
   */
  public void setStartIndent(final float startIndent) {
    this.setNumber(PDLayoutAttributeObject.START_INDENT, startIndent);
  }

  /**
   * Sets the distance from the start edge of the reference area to that of the
   * BLSE in the inline-progression direction (StartIndent).
   *
   * @param startIndent the start indent
   */
  public void setStartIndent(final int startIndent) {
    this.setNumber(PDLayoutAttributeObject.START_INDENT, startIndent);
  }

  /**
   * Gets the distance from the end edge of the BLSE to that of the reference area
   * in the inline-progression direction (EndIndent). The default value is 0.
   *
   * @return the end indent
   */
  public float getEndIndent() {
    return this.getNumber(PDLayoutAttributeObject.END_INDENT, 0.f);
  }

  /**
   * Sets the distance from the end edge of the BLSE to that of the reference area
   * in the inline-progression direction (EndIndent).
   *
   * @param endIndent the end indent
   */
  public void setEndIndent(final float endIndent) {
    this.setNumber(PDLayoutAttributeObject.END_INDENT, endIndent);
  }

  /**
   * Sets the distance from the end edge of the BLSE to that of the reference area
   * in the inline-progression direction (EndIndent).
   *
   * @param endIndent the end indent
   */
  public void setEndIndent(final int endIndent) {
    this.setNumber(PDLayoutAttributeObject.END_INDENT, endIndent);
  }

  /**
   * Gets the additional distance in the inline-progression direction from the
   * start edge of the BLSE, as specified by StartIndent, to that of the first
   * line of text (TextIndent). The default value is 0.
   *
   * @return the text indent
   */
  public float getTextIndent() {
    return this.getNumber(PDLayoutAttributeObject.TEXT_INDENT, 0.f);
  }

  /**
   * Sets the additional distance in the inline-progression direction from the
   * start edge of the BLSE, as specified by StartIndent, to that of the first
   * line of text (TextIndent).
   *
   * @param textIndent the text indent
   */
  public void setTextIndent(final float textIndent) {
    this.setNumber(PDLayoutAttributeObject.TEXT_INDENT, textIndent);
  }

  /**
   * Sets the additional distance in the inline-progression direction from the
   * start edge of the BLSE, as specified by StartIndent, to that of the first
   * line of text (TextIndent).
   *
   * @param textIndent the text indent
   */
  public void setTextIndent(final int textIndent) {
    this.setNumber(PDLayoutAttributeObject.TEXT_INDENT, textIndent);
  }

  /**
   * Gets the alignment, in the inline-progression direction, of text and other
   * content within lines of the BLSE (TextAlign). The default value is
   * {@link #TEXT_ALIGN_START}.
   *
   * @return the text alignment
   */
  public String getTextAlign() {
    return this.getName(PDLayoutAttributeObject.TEXT_ALIGN, PDLayoutAttributeObject.TEXT_ALIGN_START);
  }

  /**
   * Sets the alignment, in the inline-progression direction, of text and other
   * content within lines of the BLSE (TextAlign). The value should be one of:
   * <ul>
   * <li>{@link #TEXT_ALIGN_START},</li>
   * <li>{@link #TEXT_ALIGN_CENTER},</li>
   * <li>{@link #TEXT_ALIGN_END},</li>
   * <li>{@link #TEXT_ALIGN_JUSTIFY}.</li>
   * </ul>
   *
   * @param textIndent the text alignment
   */
  public void setTextAlign(final String textIndent) {
    setName(PDLayoutAttributeObject.TEXT_ALIGN, textIndent);
  }

  /**
   * Gets the bounding box.
   *
   * @return the bounding box.
   */
  public PDRectangle getBBox() {
    final COSArray array = (COSArray) getCOSObject().getDictionaryObject(PDLayoutAttributeObject.BBOX);
    if (array != null)
      return new PDRectangle(array);
    return null;
  }

  /**
   * Sets the bounding box.
   *
   * @param bbox the bounding box
   */
  public void setBBox(final PDRectangle bbox) {
    final String name = PDLayoutAttributeObject.BBOX;
    final COSBase oldValue = getCOSObject().getDictionaryObject(name);
    getCOSObject().setItem(name, bbox);
    final COSBase newValue = bbox == null ? null : bbox.getCOSObject();
    potentiallyNotifyChanged(oldValue, newValue);
  }

  /**
   * Gets the width of the element’s content rectangle in the inline-progression
   * direction (Width). The default value is {@link #WIDTH_AUTO}.
   *
   * @return the width (a Float or a String)
   */
  public Object getWidth() {
    return getNumberOrName(PDLayoutAttributeObject.WIDTH, PDLayoutAttributeObject.WIDTH_AUTO);
  }

  /**
   * Sets the width of the element’s content rectangle in the inline-progression
   * direction (Width) to {@link #WIDTH_AUTO}.
   */
  public void setWidthAuto() {
    setName(PDLayoutAttributeObject.WIDTH, PDLayoutAttributeObject.WIDTH_AUTO);
  }

  /**
   * Sets the width of the element’s content rectangle in the inline-progression
   * direction (Width).
   *
   * @param width the width
   */
  public void setWidth(final float width) {
    this.setNumber(PDLayoutAttributeObject.WIDTH, width);
  }

  /**
   * Sets the width of the element’s content rectangle in the inline-progression
   * direction (Width).
   *
   * @param width the width
   */
  public void setWidth(final int width) {
    this.setNumber(PDLayoutAttributeObject.WIDTH, width);
  }

  /**
   * Gets the height of the element’s content rectangle in the block-progression
   * direction (Height). The default value is {@link #HEIGHT_AUTO}.
   *
   * @return the height (a Float or a String)
   */
  public Object getHeight() {
    return getNumberOrName(PDLayoutAttributeObject.HEIGHT, PDLayoutAttributeObject.HEIGHT_AUTO);
  }

  /**
   * Sets the height of the element’s content rectangle in the block-progression
   * direction (Height) to {@link #HEIGHT_AUTO}.
   */
  public void setHeightAuto() {
    setName(PDLayoutAttributeObject.HEIGHT, PDLayoutAttributeObject.HEIGHT_AUTO);
  }

  /**
   * Sets the height of the element’s content rectangle in the block-progression
   * direction (Height).
   *
   * @param height the height
   */
  public void setHeight(final float height) {
    this.setNumber(PDLayoutAttributeObject.HEIGHT, height);
  }

  /**
   * Sets the height of the element’s content rectangle in the block-progression
   * direction (Height).
   *
   * @param height the height
   */
  public void setHeight(final int height) {
    this.setNumber(PDLayoutAttributeObject.HEIGHT, height);
  }

  /**
   * Gets the alignment, in the block-progression direction, of content within the
   * table cell (BlockAlign). The default value is {@link #BLOCK_ALIGN_BEFORE}.
   *
   * @return the block alignment
   */
  public String getBlockAlign() {
    return this.getName(PDLayoutAttributeObject.BLOCK_ALIGN, PDLayoutAttributeObject.BLOCK_ALIGN_BEFORE);
  }

  /**
   * Sets the alignment, in the block-progression direction, of content within the
   * table cell (BlockAlign). The value should be one of:
   * <ul>
   * <li>{@link #BLOCK_ALIGN_BEFORE},</li>
   * <li>{@link #BLOCK_ALIGN_MIDDLE},</li>
   * <li>{@link #BLOCK_ALIGN_AFTER},</li>
   * <li>{@link #BLOCK_ALIGN_JUSTIFY}.</li>
   * </ul>
   *
   * @param blockAlign the block alignment
   */
  public void setBlockAlign(final String blockAlign) {
    setName(PDLayoutAttributeObject.BLOCK_ALIGN, blockAlign);
  }

  /**
   * Gets the alignment, in the inline-progression direction, of content within
   * the table cell (InlineAlign). The default value is
   * {@link #INLINE_ALIGN_START}.
   *
   * @return the inline alignment
   */
  public String getInlineAlign() {
    return this.getName(PDLayoutAttributeObject.INLINE_ALIGN, PDLayoutAttributeObject.INLINE_ALIGN_START);
  }

  /**
   * Sets the alignment, in the inline-progression direction, of content within
   * the table cell (InlineAlign). The value should be one of
   * <ul>
   * <li>{@link #INLINE_ALIGN_START},</li>
   * <li>{@link #INLINE_ALIGN_CENTER},</li>
   * <li>{@link #INLINE_ALIGN_END}.</li>
   * </ul>
   *
   * @param inlineAlign the inline alignment
   */
  public void setInlineAlign(final String inlineAlign) {
    setName(PDLayoutAttributeObject.INLINE_ALIGN, inlineAlign);
  }

  /**
   * Gets the style of the border drawn on each edge of a table cell
   * (TBorderStyle).
   *
   * @return the border style.
   */
  public Object getTBorderStyle() {
    return getNameOrArrayOfName(PDLayoutAttributeObject.T_BORDER_STYLE, PDLayoutAttributeObject.BORDER_STYLE_NONE);
  }

  /**
   * Sets the same table border style for all four sides (TBorderStyle). The value
   * should be one of:
   * <ul>
   * <li>{@link #BORDER_STYLE_NONE},</li>
   * <li>{@link #BORDER_STYLE_HIDDEN},</li>
   * <li>{@link #BORDER_STYLE_DOTTED},</li>
   * <li>{@link #BORDER_STYLE_DASHED},</li>
   * <li>{@link #BORDER_STYLE_SOLID},</li>
   * <li>{@link #BORDER_STYLE_DOUBLE},</li>
   * <li>{@link #BORDER_STYLE_GROOVE},</li>
   * <li>{@link #BORDER_STYLE_RIDGE},</li>
   * <li>{@link #BORDER_STYLE_INSET},</li>
   * <li>{@link #BORDER_STYLE_OUTSET}.</li>
   * </ul>
   *
   * @param tBorderStyle the table border style
   */
  public void setAllTBorderStyles(final String tBorderStyle) {
    setName(PDLayoutAttributeObject.T_BORDER_STYLE, tBorderStyle);
  }

  /**
   * Sets the style of the border drawn on each edge of a table cell
   * (TBorderStyle). The values should be of:
   * <ul>
   * <li>{@link #BORDER_STYLE_NONE},</li>
   * <li>{@link #BORDER_STYLE_HIDDEN},</li>
   * <li>{@link #BORDER_STYLE_DOTTED},</li>
   * <li>{@link #BORDER_STYLE_DASHED},</li>
   * <li>{@link #BORDER_STYLE_SOLID},</li>
   * <li>{@link #BORDER_STYLE_DOUBLE},</li>
   * <li>{@link #BORDER_STYLE_GROOVE},</li>
   * <li>{@link #BORDER_STYLE_RIDGE},</li>
   * <li>{@link #BORDER_STYLE_INSET},</li>
   * <li>{@link #BORDER_STYLE_OUTSET}.</li>
   * </ul>
   *
   * @param tBorderStyles
   */
  public void setTBorderStyles(final String[] tBorderStyles) {
    setArrayOfName(PDLayoutAttributeObject.T_BORDER_STYLE, tBorderStyles);
  }

  /**
   * Gets the offset to account for the separation between the table cell’s
   * content rectangle and the surrounding border (TPadding). The default value is
   * 0.
   *
   * @return the table padding (a Float or an array of float)
   */
  public Object getTPadding() {
    return getNumberOrArrayOfNumber(PDLayoutAttributeObject.T_PADDING, 0.f);
  }

  /**
   * Sets the same table padding for all four sides (TPadding).
   *
   * @param tPadding the table padding
   */
  public void setAllTPaddings(final float tPadding) {
    this.setNumber(PDLayoutAttributeObject.T_PADDING, tPadding);
  }

  /**
   * Sets the same table padding for all four sides (TPadding).
   *
   * @param tPadding the table padding
   */
  public void setAllTPaddings(final int tPadding) {
    this.setNumber(PDLayoutAttributeObject.T_PADDING, tPadding);
  }

  /**
   * Sets the table paddings for four sides separately (TPadding).
   *
   * @param tPaddings the table paddings (an array of four floats)
   */
  public void setTPaddings(final float[] tPaddings) {
    setArrayOfNumber(PDLayoutAttributeObject.T_PADDING, tPaddings);
  }

  /**
   * Gets the distance by which the element’s baseline shall be shifted relative
   * to that of its parent element (BaselineShift). The default value is 0.
   *
   * @return the baseline shift
   */
  public float getBaselineShift() {
    return this.getNumber(PDLayoutAttributeObject.BASELINE_SHIFT, 0.f);
  }

  /**
   * Sets the distance by which the element’s baseline shall be shifted relative
   * to that of its parent element (BaselineShift).
   *
   * @param baselineShift the baseline shift
   */
  public void setBaselineShift(final float baselineShift) {
    this.setNumber(PDLayoutAttributeObject.BASELINE_SHIFT, baselineShift);
  }

  /**
   * Sets the distance by which the element’s baseline shall be shifted relative
   * to that of its parent element (BaselineShift).
   *
   * @param baselineShift the baseline shift
   */
  public void setBaselineShift(final int baselineShift) {
    this.setNumber(PDLayoutAttributeObject.BASELINE_SHIFT, baselineShift);
  }

  /**
   * Gets the element’s preferred height in the block-progression direction
   * (LineHeight). The default value is {@link #LINE_HEIGHT_NORMAL}.
   *
   * @return the line height (a Float or a String)
   */
  public Object getLineHeight() {
    return getNumberOrName(PDLayoutAttributeObject.LINE_HEIGHT, PDLayoutAttributeObject.LINE_HEIGHT_NORMAL);
  }

  /**
   * Sets the element’s preferred height in the block-progression direction
   * (LineHeight) to {@link #LINE_HEIGHT_NORMAL}.
   */
  public void setLineHeightNormal() {
    setName(PDLayoutAttributeObject.LINE_HEIGHT, PDLayoutAttributeObject.LINE_HEIGHT_NORMAL);
  }

  /**
   * Sets the element’s preferred height in the block-progression direction
   * (LineHeight) to {@link #LINE_HEIGHT_AUTO}.
   */
  public void setLineHeightAuto() {
    setName(PDLayoutAttributeObject.LINE_HEIGHT, PDLayoutAttributeObject.LINE_HEIGHT_AUTO);
  }

  /**
   * Sets the element’s preferred height in the block-progression direction
   * (LineHeight).
   *
   * @param lineHeight the line height
   */
  public void setLineHeight(final float lineHeight) {
    this.setNumber(PDLayoutAttributeObject.LINE_HEIGHT, lineHeight);
  }

  /**
   * Sets the element’s preferred height in the block-progression direction
   * (LineHeight).
   *
   * @param lineHeight the line height
   */
  public void setLineHeight(final int lineHeight) {
    this.setNumber(PDLayoutAttributeObject.LINE_HEIGHT, lineHeight);
  }

  /**
   * Gets the colour to be used for drawing text decorations
   * (TextDecorationColor).
   *
   * @return the text decoration colour
   */
  public PDGamma getTextDecorationColor() {
    return this.getColor(PDLayoutAttributeObject.TEXT_DECORATION_COLOR);
  }

  /**
   * Sets the colour to be used for drawing text decorations
   * (TextDecorationColor).
   *
   * @param textDecorationColor the text decoration colour
   */
  public void setTextDecorationColor(final PDGamma textDecorationColor) {
    this.setColor(PDLayoutAttributeObject.TEXT_DECORATION_COLOR, textDecorationColor);
  }

  /**
   * Gets the thickness of each line drawn as part of the text decoration
   * (TextDecorationThickness).
   *
   * @return the text decoration thickness
   */
  public float getTextDecorationThickness() {
    return this.getNumber(PDLayoutAttributeObject.TEXT_DECORATION_THICKNESS);
  }

  /**
   * Sets the thickness of each line drawn as part of the text decoration
   * (TextDecorationThickness).
   *
   * @param textDecorationThickness the text decoration thickness
   */
  public void setTextDecorationThickness(final float textDecorationThickness) {
    this.setNumber(PDLayoutAttributeObject.TEXT_DECORATION_THICKNESS, textDecorationThickness);
  }

  /**
   * Sets the thickness of each line drawn as part of the text decoration
   * (TextDecorationThickness).
   *
   * @param textDecorationThickness the text decoration thickness
   */
  public void setTextDecorationThickness(final int textDecorationThickness) {
    this.setNumber(PDLayoutAttributeObject.TEXT_DECORATION_THICKNESS, textDecorationThickness);
  }

  /**
   * Gets the type of text decoration (TextDecorationType). The default value is
   * {@link #TEXT_DECORATION_TYPE_NONE}.
   *
   * @return the type of text decoration
   */
  public String getTextDecorationType() {
    return this.getName(PDLayoutAttributeObject.TEXT_DECORATION_TYPE,
        PDLayoutAttributeObject.TEXT_DECORATION_TYPE_NONE);
  }

  /**
   * Sets the type of text decoration (TextDecorationType). The value should be
   * one of:
   * <ul>
   * <li>{@link #TEXT_DECORATION_TYPE_NONE},</li>
   * <li>{@link #TEXT_DECORATION_TYPE_UNDERLINE},</li>
   * <li>{@link #TEXT_DECORATION_TYPE_OVERLINE},</li>
   * <li>{@link #TEXT_DECORATION_TYPE_LINE_THROUGH}.</li>
   * </ul>
   *
   * @param textDecorationType the type of text decoration
   */
  public void setTextDecorationType(final String textDecorationType) {
    setName(PDLayoutAttributeObject.TEXT_DECORATION_TYPE, textDecorationType);
  }

  /**
   * Gets the justification of the lines within a ruby assembly (RubyAlign). The
   * default value is {@link #RUBY_ALIGN_DISTRIBUTE}.
   *
   * @return the ruby alignment
   */
  public String getRubyAlign() {
    return this.getName(PDLayoutAttributeObject.RUBY_ALIGN, PDLayoutAttributeObject.RUBY_ALIGN_DISTRIBUTE);
  }

  /**
   * Sets the justification of the lines within a ruby assembly (RubyAlign). The
   * value should be one of:
   * <ul>
   * <li>{@link #RUBY_ALIGN_START},</li>
   * <li>{@link #RUBY_ALIGN_CENTER},</li>
   * <li>{@link #RUBY_ALIGN_END},</li>
   * <li>{@link #RUBY_ALIGN_JUSTIFY},</li>
   * <li>{@link #RUBY_ALIGN_DISTRIBUTE},</li>
   * </ul>
   *
   * @param rubyAlign the ruby alignment
   */
  public void setRubyAlign(final String rubyAlign) {
    setName(PDLayoutAttributeObject.RUBY_ALIGN, rubyAlign);
  }

  /**
   * Gets the placement of the RT structure element relative to the RB element in
   * a ruby assembly (RubyPosition). The default value is
   * {@link #RUBY_POSITION_BEFORE}.
   *
   * @return the ruby position
   */
  public String getRubyPosition() {
    return this.getName(PDLayoutAttributeObject.RUBY_POSITION, PDLayoutAttributeObject.RUBY_POSITION_BEFORE);
  }

  /**
   * Sets the placement of the RT structure element relative to the RB element in
   * a ruby assembly (RubyPosition). The value should be one of:
   * <ul>
   * <li>{@link #RUBY_POSITION_BEFORE},</li>
   * <li>{@link #RUBY_POSITION_AFTER},</li>
   * <li>{@link #RUBY_POSITION_WARICHU},</li>
   * <li>{@link #RUBY_POSITION_INLINE}.</li>
   * </ul>
   *
   * @param rubyPosition the ruby position
   */
  public void setRubyPosition(final String rubyPosition) {
    setName(PDLayoutAttributeObject.RUBY_POSITION, rubyPosition);
  }

  /**
   * Gets the orientation of glyphs when the inline-progression direction is top
   * to bottom or bottom to top (GlyphOrientationVertical). The default value is
   * {@link #GLYPH_ORIENTATION_VERTICAL_AUTO}.
   *
   * @return the vertical glyph orientation
   */
  public String getGlyphOrientationVertical() {
    return this.getName(PDLayoutAttributeObject.GLYPH_ORIENTATION_VERTICAL,
        PDLayoutAttributeObject.GLYPH_ORIENTATION_VERTICAL_AUTO);
  }

  /**
   * Sets the orientation of glyphs when the inline-progression direction is top
   * to bottom or bottom to top (GlyphOrientationVertical). The value should be
   * one of:
   * <ul>
   * <li>{@link #GLYPH_ORIENTATION_VERTICAL_AUTO},</li>
   * <li>{@link #GLYPH_ORIENTATION_VERTICAL_MINUS_180_DEGREES},</li>
   * <li>{@link #GLYPH_ORIENTATION_VERTICAL_MINUS_90_DEGREES},</li>
   * <li>{@link #GLYPH_ORIENTATION_VERTICAL_ZERO_DEGREES},</li>
   * <li>{@link #GLYPH_ORIENTATION_VERTICAL_90_DEGREES},</li>
   * <li>{@link #GLYPH_ORIENTATION_VERTICAL_180_DEGREES},</li>
   * <li>{@link #GLYPH_ORIENTATION_VERTICAL_270_DEGREES},</li>
   * <li>{@link #GLYPH_ORIENTATION_VERTICAL_360_DEGREES}.</li>
   * </ul>
   *
   * @param glyphOrientationVertical the vertical glyph orientation
   */
  public void setGlyphOrientationVertical(final String glyphOrientationVertical) {
    setName(PDLayoutAttributeObject.GLYPH_ORIENTATION_VERTICAL, glyphOrientationVertical);
  }

  /**
   * Gets the number of columns in the content of the grouping element
   * (ColumnCount). The default value is 1.
   *
   * @return the column count
   */
  public int getColumnCount() {
    return getInteger(PDLayoutAttributeObject.COLUMN_COUNT, 1);
  }

  /**
   * Sets the number of columns in the content of the grouping element
   * (ColumnCount).
   *
   * @param columnCount the column count
   */
  public void setColumnCount(final int columnCount) {
    setInteger(PDLayoutAttributeObject.COLUMN_COUNT, columnCount);
  }

  /**
   * Gets the desired space between adjacent columns in the inline-progression
   * direction (ColumnGap).
   *
   * @return the column gap (FLoat or array of floats)
   */
  public Object getColumnGap() {
    return getNumberOrArrayOfNumber(PDLayoutAttributeObject.COLUMN_GAP, PDStandardAttributeObject.UNSPECIFIED);
  }

  /**
   * Sets the desired space between all columns in the inline-progression
   * direction (ColumnGap).
   *
   * @param columnGap the column gap
   */
  public void setColumnGap(final float columnGap) {
    this.setNumber(PDLayoutAttributeObject.COLUMN_GAP, columnGap);
  }

  /**
   * Sets the desired space between all columns in the inline-progression
   * direction (ColumnGap).
   *
   * @param columnGap the column gap
   */
  public void setColumnGap(final int columnGap) {
    this.setNumber(PDLayoutAttributeObject.COLUMN_GAP, columnGap);
  }

  /**
   * Sets the desired space between adjacent columns in the inline-progression
   * direction (ColumnGap), the first element specifying the space between the
   * first and second columns, the second specifying the space between the second
   * and third columns, and so on.
   *
   * @param columnGaps the column gaps
   */
  public void setColumnGaps(final float[] columnGaps) {
    setArrayOfNumber(PDLayoutAttributeObject.COLUMN_GAP, columnGaps);
  }

  /**
   * Gets the desired width of the columns, measured in default user space units
   * in the inline-progression direction (ColumnWidths).
   *
   * @return the column widths (Float or array of floats)
   */
  public Object getColumnWidths() {
    return getNumberOrArrayOfNumber(PDLayoutAttributeObject.COLUMN_WIDTHS, PDStandardAttributeObject.UNSPECIFIED);
  }

  /**
   * Sets the same column width for all columns (ColumnWidths).
   *
   * @param columnWidth the column width
   */
  public void setAllColumnWidths(final float columnWidth) {
    this.setNumber(PDLayoutAttributeObject.COLUMN_WIDTHS, columnWidth);
  }

  /**
   * Sets the same column width for all columns (ColumnWidths).
   *
   * @param columnWidth the column width
   */
  public void setAllColumnWidths(final int columnWidth) {
    this.setNumber(PDLayoutAttributeObject.COLUMN_WIDTHS, columnWidth);
  }

  /**
   * Sets the column widths for the columns separately (ColumnWidths).
   *
   * @param columnWidths the column widths
   */
  public void setColumnWidths(final float[] columnWidths) {
    setArrayOfNumber(PDLayoutAttributeObject.COLUMN_WIDTHS, columnWidths);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder().append(super.toString());
    if (isSpecified(PDLayoutAttributeObject.PLACEMENT)) {
      sb.append(", Placement=").append(getPlacement());
    }
    if (isSpecified(PDLayoutAttributeObject.WRITING_MODE)) {
      sb.append(", WritingMode=").append(getWritingMode());
    }
    if (isSpecified(PDLayoutAttributeObject.BACKGROUND_COLOR)) {
      sb.append(", BackgroundColor=").append(getBackgroundColor());
    }
    if (isSpecified(PDLayoutAttributeObject.BORDER_COLOR)) {
      sb.append(", BorderColor=").append(getBorderColors());
    }
    if (isSpecified(PDLayoutAttributeObject.BORDER_STYLE)) {
      final Object borderStyle = getBorderStyle();
      sb.append(", BorderStyle=");
      if (borderStyle instanceof String[]) {
        sb.append(PDAttributeObject.arrayToString((String[]) borderStyle));
      } else {
        sb.append(borderStyle);
      }
    }
    if (isSpecified(PDLayoutAttributeObject.BORDER_THICKNESS)) {
      final Object borderThickness = getBorderThickness();
      sb.append(", BorderThickness=");
      if (borderThickness instanceof float[]) {
        sb.append(PDAttributeObject.arrayToString((float[]) borderThickness));
      } else {
        sb.append(String.valueOf(borderThickness));
      }
    }
    if (isSpecified(PDLayoutAttributeObject.PADDING)) {
      final Object padding = getPadding();
      sb.append(", Padding=");
      if (padding instanceof float[]) {
        sb.append(PDAttributeObject.arrayToString((float[]) padding));
      } else {
        sb.append(String.valueOf(padding));
      }
    }
    if (isSpecified(PDLayoutAttributeObject.COLOR)) {
      sb.append(", Color=").append(this.getColor());
    }
    if (isSpecified(PDLayoutAttributeObject.SPACE_BEFORE)) {
      sb.append(", SpaceBefore=").append(String.valueOf(getSpaceBefore()));
    }
    if (isSpecified(PDLayoutAttributeObject.SPACE_AFTER)) {
      sb.append(", SpaceAfter=").append(String.valueOf(getSpaceAfter()));
    }
    if (isSpecified(PDLayoutAttributeObject.START_INDENT)) {
      sb.append(", StartIndent=").append(String.valueOf(getStartIndent()));
    }
    if (isSpecified(PDLayoutAttributeObject.END_INDENT)) {
      sb.append(", EndIndent=").append(String.valueOf(getEndIndent()));
    }
    if (isSpecified(PDLayoutAttributeObject.TEXT_INDENT)) {
      sb.append(", TextIndent=").append(String.valueOf(getTextIndent()));
    }
    if (isSpecified(PDLayoutAttributeObject.TEXT_ALIGN)) {
      sb.append(", TextAlign=").append(getTextAlign());
    }
    if (isSpecified(PDLayoutAttributeObject.BBOX)) {
      sb.append(", BBox=").append(getBBox());
    }
    if (isSpecified(PDLayoutAttributeObject.WIDTH)) {
      final Object width = getWidth();
      sb.append(", Width=");
      if (width instanceof Float) {
        sb.append(String.valueOf(width));
      } else {
        sb.append(width);
      }
    }
    if (isSpecified(PDLayoutAttributeObject.HEIGHT)) {
      final Object height = getHeight();
      sb.append(", Height=");
      if (height instanceof Float) {
        sb.append(String.valueOf(height));
      } else {
        sb.append(height);
      }
    }
    if (isSpecified(PDLayoutAttributeObject.BLOCK_ALIGN)) {
      sb.append(", BlockAlign=").append(getBlockAlign());
    }
    if (isSpecified(PDLayoutAttributeObject.INLINE_ALIGN)) {
      sb.append(", InlineAlign=").append(getInlineAlign());
    }
    if (isSpecified(PDLayoutAttributeObject.T_BORDER_STYLE)) {
      final Object tBorderStyle = getTBorderStyle();
      sb.append(", TBorderStyle=");
      if (tBorderStyle instanceof String[]) {
        sb.append(PDAttributeObject.arrayToString((String[]) tBorderStyle));
      } else {
        sb.append(tBorderStyle);
      }
    }
    if (isSpecified(PDLayoutAttributeObject.T_PADDING)) {
      final Object tPadding = getTPadding();
      sb.append(", TPadding=");
      if (tPadding instanceof float[]) {
        sb.append(PDAttributeObject.arrayToString((float[]) tPadding));
      } else {
        sb.append(String.valueOf(tPadding));
      }
    }
    if (isSpecified(PDLayoutAttributeObject.BASELINE_SHIFT)) {
      sb.append(", BaselineShift=").append(String.valueOf(getBaselineShift()));
    }
    if (isSpecified(PDLayoutAttributeObject.LINE_HEIGHT)) {
      final Object lineHeight = getLineHeight();
      sb.append(", LineHeight=");
      if (lineHeight instanceof Float) {
        sb.append(String.valueOf(lineHeight));
      } else {
        sb.append(lineHeight);
      }
    }
    if (isSpecified(PDLayoutAttributeObject.TEXT_DECORATION_COLOR)) {
      sb.append(", TextDecorationColor=").append(getTextDecorationColor());
    }
    if (isSpecified(PDLayoutAttributeObject.TEXT_DECORATION_THICKNESS)) {
      sb.append(", TextDecorationThickness=").append(String.valueOf(getTextDecorationThickness()));
    }
    if (isSpecified(PDLayoutAttributeObject.TEXT_DECORATION_TYPE)) {
      sb.append(", TextDecorationType=").append(getTextDecorationType());
    }
    if (isSpecified(PDLayoutAttributeObject.RUBY_ALIGN)) {
      sb.append(", RubyAlign=").append(getRubyAlign());
    }
    if (isSpecified(PDLayoutAttributeObject.RUBY_POSITION)) {
      sb.append(", RubyPosition=").append(getRubyPosition());
    }
    if (isSpecified(PDLayoutAttributeObject.GLYPH_ORIENTATION_VERTICAL)) {
      sb.append(", GlyphOrientationVertical=").append(getGlyphOrientationVertical());
    }
    if (isSpecified(PDLayoutAttributeObject.COLUMN_COUNT)) {
      sb.append(", ColumnCount=").append(String.valueOf(getColumnCount()));
    }
    if (isSpecified(PDLayoutAttributeObject.COLUMN_GAP)) {
      final Object columnGap = getColumnGap();
      sb.append(", ColumnGap=");
      if (columnGap instanceof float[]) {
        sb.append(PDAttributeObject.arrayToString((float[]) columnGap));
      } else {
        sb.append(String.valueOf(columnGap));
      }
    }
    if (isSpecified(PDLayoutAttributeObject.COLUMN_WIDTHS)) {
      final Object columnWidth = getColumnWidths();
      sb.append(", ColumnWidths=");
      if (columnWidth instanceof float[]) {
        sb.append(PDAttributeObject.arrayToString((float[]) columnWidth));
      } else {
        sb.append(String.valueOf(columnWidth));
      }
    }
    return sb.toString();
  }

}
