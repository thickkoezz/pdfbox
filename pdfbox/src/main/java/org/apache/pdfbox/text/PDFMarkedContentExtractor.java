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
package org.apache.pdfbox.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.pdfbox.contentstream.operator.markedcontent.BeginMarkedContentSequence;
import org.apache.pdfbox.contentstream.operator.markedcontent.BeginMarkedContentSequenceWithProperties;
import org.apache.pdfbox.contentstream.operator.markedcontent.DrawObject;
import org.apache.pdfbox.contentstream.operator.markedcontent.EndMarkedContentSequence;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;

/**
 * This is an stream engine to extract the marked content of a pdf.
 *
 * @author Johannes Koch
 */
public class PDFMarkedContentExtractor extends LegacyPDFStreamEngine {
  private final boolean suppressDuplicateOverlappingText = true;
  private final List<PDMarkedContent> markedContents = new ArrayList<>();
  private final Stack<PDMarkedContent> currentMarkedContents = new Stack<>();
  private final Map<String, List<TextPosition>> characterListMapping = new HashMap<>();

  /**
   * Instantiate a new PDFTextStripper object.
   */
  public PDFMarkedContentExtractor() throws IOException {
    this(null);
  }

  /**
   * Constructor. Will apply encoding-specific conversions to the output text.
   *
   * @param encoding The encoding that the output will be written in.
   */
  public PDFMarkedContentExtractor(final String encoding) throws IOException {
    addOperator(new BeginMarkedContentSequenceWithProperties());
    addOperator(new BeginMarkedContentSequence());
    addOperator(new EndMarkedContentSequence());
    addOperator(new DrawObject());
    // todo: DP - Marked Content Point
    // todo: MP - Marked Content Point with Properties
  }

  /**
   * This will determine of two floating point numbers are within a specified
   * variance.
   *
   * @param first    The first number to compare to.
   * @param second   The second number to compare to.
   * @param variance The allowed variance.
   */
  private boolean within(final float first, final float second, final float variance) {
    return second > first - variance && second < first + variance;
  }

  @Override
  public void beginMarkedContentSequence(final COSName tag, final COSDictionary properties) {
    final PDMarkedContent markedContent = PDMarkedContent.create(tag, properties);
    if (currentMarkedContents.isEmpty()) {
      markedContents.add(markedContent);
    } else {
      final PDMarkedContent currentMarkedContent = currentMarkedContents.peek();
      if (currentMarkedContent != null) {
        currentMarkedContent.addMarkedContent(markedContent);
      }
    }
    currentMarkedContents.push(markedContent);
  }

  @Override
  public void endMarkedContentSequence() {
    if (!currentMarkedContents.isEmpty()) {
      currentMarkedContents.pop();
    }
  }

  public void xobject(final PDXObject xobject) {
    if (!currentMarkedContents.isEmpty()) {
      currentMarkedContents.peek().addXObject(xobject);
    }
  }

  /**
   * This will process a TextPosition object and add the text to the list of
   * characters on a page. It takes care of overlapping text.
   *
   * @param text The text to process.
   */
  @Override
  protected void processTextPosition(final TextPosition text) {
    boolean showCharacter = true;
    if (suppressDuplicateOverlappingText) {
      showCharacter = false;
      final String textCharacter = text.getUnicode();
      final float textX = text.getX();
      final float textY = text.getY();
      List<TextPosition> sameTextCharacters = characterListMapping.get(textCharacter);
      if (sameTextCharacters == null) {
        sameTextCharacters = new ArrayList<>();
        characterListMapping.put(textCharacter, sameTextCharacters);
      }

      // RDD - Here we compute the value that represents the end of the rendered
      // text. This value is used to determine whether subsequent text rendered
      // on the same line overwrites the current text.
      //
      // We subtract any positive padding to handle cases where extreme amounts
      // of padding are applied, then backed off (not sure why this is done, but there
      // are cases where the padding is on the order of 10x the character width, and
      // the TJ just backs up to compensate after each character). Also, we subtract
      // an amount to allow for kerning (a percentage of the width of the last
      // character).
      //
      boolean suppressCharacter = false;
      final float tolerance = text.getWidth() / textCharacter.length() / 3.0f;
      for (final TextPosition sameTextCharacter : sameTextCharacters) {
        final TextPosition character = sameTextCharacter;
        final String charCharacter = character.getUnicode();
        final float charX = character.getX();
        final float charY = character.getY();
        // only want to suppress
        if (charCharacter != null &&
        // charCharacter.equals( textCharacter ) &&
            within(charX, textX, tolerance) && within(charY, textY, tolerance)) {
          suppressCharacter = true;
          break;
        }
      }
      if (!suppressCharacter) {
        sameTextCharacters.add(text);
        showCharacter = true;
      }
    }

    if (showCharacter) {
      final List<TextPosition> textList = new ArrayList<>();

      /*
       * In the wild, some PDF encoded documents put diacritics (accents on top of
       * characters) into a separate Tj element. When displaying them graphically, the
       * two chunks get overlayed. With text output though, we need to do the overlay.
       * This code recombines the diacritic with its associated character if the two
       * are consecutive.
       */
      if (textList.isEmpty()) {
        textList.add(text);
      } else {
        /*
         * test if we overlap the previous entry. Note that we are making an assumption
         * that we need to only look back one TextPosition to find what we are
         * overlapping. This may not always be true.
         */
        final TextPosition previousTextPosition = textList.get(textList.size() - 1);
        if (text.isDiacritic() && previousTextPosition.contains(text)) {
          previousTextPosition.mergeDiacritic(text);
        }
        /*
         * If the previous TextPosition was the diacritic, merge it into this one and
         * remove it from the list.
         */
        else if (previousTextPosition.isDiacritic() && text.contains(previousTextPosition)) {
          text.mergeDiacritic(previousTextPosition);
          textList.remove(textList.size() - 1);
          textList.add(text);
        } else {
          textList.add(text);
        }
      }
      if (!currentMarkedContents.isEmpty()) {
        currentMarkedContents.peek().addText(text);
      }
    }
  }

  public List<PDMarkedContent> getMarkedContents() {
    return markedContents;
  }
}
