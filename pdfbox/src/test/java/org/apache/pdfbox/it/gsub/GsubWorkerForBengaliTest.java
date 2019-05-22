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

package org.apache.pdfbox.it.gsub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.gsub.GsubWorker;
import org.apache.fontbox.ttf.gsub.GsubWorkerFactory;
import org.apache.fontbox.ttf.gsub.GsubWorkerForBengali;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Integration test for {@link GsubWorkerForBengali}. Has various combinations
 * of glyphs to test proper working of the GSUB system.
 *
 * @author Palash Ray
 *
 */
public class GsubWorkerForBengaliTest {

  private static final String LOHIT_BENGALI_TTF = "/org/apache/pdfbox/ttf/Lohit-Bengali.ttf";

  private CmapLookup cmapLookup;
  private GsubWorker gsubWorkerForBengali;

  @Before
  public void init() throws IOException {
    try (PDDocument doc = new PDDocument()) {
      final PDType0Font font = PDType0Font.load(doc,
          GsubWorkerForBengaliTest.class.getResourceAsStream(GsubWorkerForBengaliTest.LOHIT_BENGALI_TTF), true);

      cmapLookup = font.getCmapLookup();
      gsubWorkerForBengali = new GsubWorkerFactory().getGsubWorker(cmapLookup, font.getGsubData());
    }
  }

  @Test
  public void testApplyTransforms_simple_hosshoi_kar() {
    // given
    final List<Integer> glyphsAfterGsub = Arrays.asList(56, 102, 91);

    // when
    final List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("আমি"));

    // then
    Assert.assertEquals(glyphsAfterGsub, result);
  }

  @Test
  public void testApplyTransforms_ja_phala() {
    // given
    final List<Integer> glyphsAfterGsub = Arrays.asList(89, 156, 101, 97);

    // when
    final List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("ব্যাস"));

    // then
    Assert.assertEquals(glyphsAfterGsub, result);
  }

  @Test
  public void testApplyTransforms_e_kar() {
    // given
    final List<Integer> glyphsAfterGsub = Arrays.asList(438, 89, 94, 101);

    // when
    final List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("বেলা"));

    // then
    Assert.assertEquals(glyphsAfterGsub, result);
  }

  @Test
  public void testApplyTransforms_o_kar() {
    // given
    final List<Integer> glyphsAfterGsub = Arrays.asList(108, 89, 101, 97);

    // when
    final List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("বোস"));

    // then
    Assert.assertEquals(glyphsAfterGsub, result);
  }

  @Ignore
  public void testApplyTransforms_o_kar_repeated_1_not_working_yet() {
    // given
    final List<Integer> glyphsAfterGsub = Arrays.asList(108, 96, 101, 108, 94, 101);

    // when
    final List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("ষোলো"));

    // then
    Assert.assertEquals(glyphsAfterGsub, result);
  }

  @Ignore
  public void testApplyTransforms_o_kar_repeated_2_not_working_yet() {
    // given
    final List<Integer> glyphsAfterGsub = Arrays.asList(108, 73, 101, 108, 77, 101);

    // when
    final List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("ছোটো"));

    // then
    Assert.assertEquals(glyphsAfterGsub, result);
  }

  @Test
  public void testApplyTransforms_ou_kar() {
    // given
    final List<Integer> glyphsAfterGsub = Arrays.asList(108, 91, 114, 94);

    // when
    final List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("মৌল"));

    // then
    Assert.assertEquals(glyphsAfterGsub, result);
  }

  @Test
  public void testApplyTransforms_oi_kar() {
    // given
    final List<Integer> glyphsAfterGsub = Arrays.asList(439, 89, 93);

    // when
    final List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("বৈর"));

    // then
    Assert.assertEquals(glyphsAfterGsub, result);
  }

  @Test
  public void testApplyTransforms_kha_e_murddhana_swa_e_khiwa() {
    // given
    final List<Integer> glyphsAfterGsub = Arrays.asList(167, 103, 438, 93, 93);

    // when
    final List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("ক্ষীরের"));

    // then
    Assert.assertEquals(glyphsAfterGsub, result);
  }

  @Test
  public void testApplyTransforms_ra_phala() {
    // given
    final List<Integer> glyphsAfterGsub = Arrays.asList(274, 82);

    // when
    final List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("দ্রুত"));

    // then
    Assert.assertEquals(glyphsAfterGsub, result);
  }

  @Test
  public void testApplyTransforms_ref() {
    // given
    final List<Integer> glyphsAfterGsub = Arrays.asList(85, 104, 440, 82);

    // when
    final List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("ধুর্ত"));

    // then
    Assert.assertEquals(glyphsAfterGsub, result);
  }

  @Test
  public void testApplyTransforms_ra_e_hosshu() {
    // given
    final List<Integer> glyphsAfterGsub = Arrays.asList(352, 108, 87, 101);

    // when
    final List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("রুপো"));

    // then
    Assert.assertEquals(glyphsAfterGsub, result);
  }

  @Test
  public void testApplyTransforms_la_e_la_e() {
    // given
    final List<Integer> glyphsAfterGsub = Arrays.asList(67, 108, 369, 101, 94);

    // when
    final List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("কল্লোল"));

    // then
    Assert.assertEquals(glyphsAfterGsub, result);
  }

  @Test
  public void testApplyTransforms_khanda_ta() {
    // given
    final List<Integer> glyphsAfterGsub = Arrays.asList(98, 78, 101, 113);

    // when
    final List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("হঠাৎ"));

    // then
    Assert.assertEquals(glyphsAfterGsub, result);
  }

  private List<Integer> getGlyphIds(final String word) {
    final List<Integer> originalGlyphIds = new ArrayList<>();

    for (final char unicodeChar : word.toCharArray()) {
      final int glyphId = cmapLookup.getGlyphId(unicodeChar);
      Assert.assertTrue(glyphId > 0);
      originalGlyphIds.add(glyphId);
    }

    return originalGlyphIds;
  }

}
