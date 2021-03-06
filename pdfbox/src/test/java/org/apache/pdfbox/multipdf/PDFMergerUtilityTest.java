/*
 * Copyright 2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.multipdf;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;
import org.apache.pdfbox.pdmodel.common.PDNumberTreeNode;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.rendering.PDFRenderer;

import junit.framework.TestCase;

/**
 * Test suite for PDFMergerUtility.
 *
 * @author Maruan Sahyoun (PDF files)
 * @author Tilman Hausherr (code)
 */
public class PDFMergerUtilityTest extends TestCase {
  final String SRCDIR = "src/test/resources/input/merge/";
  final String TARGETTESTDIR = "target/test-output/merge/";
  private static final File TARGETPDFDIR = new File("target/pdfs");
  final int DPI = 96;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    new File(TARGETTESTDIR).mkdirs();
    if (!new File(TARGETTESTDIR).exists())
      throw new IOException("could not create output directory");
  }

  /**
   * Tests whether the merge of two PDF files with identically named but different
   * global resources works. The two PDF files have two fonts each named /TT1 and
   * /TT0 that are Arial and Courier and vice versa in the second file. Revisions
   * before 1613017 fail this test because global resources were merged which made
   * trouble when resources of the same kind had the same name.
   *
   * @throws IOException if something goes wrong.
   */
  public void testPDFMergerUtility() throws IOException {
    checkMergeIdentical("PDFBox.GlobalResourceMergeTest.Doc01.decoded.pdf",
        "PDFBox.GlobalResourceMergeTest.Doc02.decoded.pdf", "GlobalResourceMergeTestResult.pdf",
        MemoryUsageSetting.setupMainMemoryOnly());

    // once again, with scratch file
    checkMergeIdentical("PDFBox.GlobalResourceMergeTest.Doc01.decoded.pdf",
        "PDFBox.GlobalResourceMergeTest.Doc02.decoded.pdf", "GlobalResourceMergeTestResult2.pdf",
        MemoryUsageSetting.setupTempFileOnly());
  }

  /**
   * Tests whether the merge of two PDF files with JPEG and CCITT works. A few
   * revisions before 1704911 this test failed because the clone utility attempted
   * to decode and re-encode the streams, see PDFBOX-2893 on 23.9.2015.
   *
   * @throws IOException if something goes wrong.
   */
  public void testJpegCcitt() throws IOException {
    checkMergeIdentical("jpegrgb.pdf", "multitiff.pdf", "JpegMultiMergeTestResult.pdf",
        MemoryUsageSetting.setupMainMemoryOnly());

    // once again, with scratch file
    checkMergeIdentical("jpegrgb.pdf", "multitiff.pdf", "JpegMultiMergeTestResult.pdf",
        MemoryUsageSetting.setupTempFileOnly());
  }

  // see PDFBOX-2893
  public void testPDFMergerUtility2() throws IOException {
    checkMergeIdentical("PDFBox.GlobalResourceMergeTest.Doc01.pdf", "PDFBox.GlobalResourceMergeTest.Doc02.pdf",
        "GlobalResourceMergeTestResult.pdf", MemoryUsageSetting.setupMainMemoryOnly());

    // once again, with scratch file
    checkMergeIdentical("PDFBox.GlobalResourceMergeTest.Doc01.pdf", "PDFBox.GlobalResourceMergeTest.Doc02.pdf",
        "GlobalResourceMergeTestResult2.pdf", MemoryUsageSetting.setupTempFileOnly());
  }

  /**
   * PDFBOX-3972: Test that OpenAction page destination isn't lost after merge.
   * 
   * @throws IOException
   */
  public void testPDFMergerOpenAction() throws IOException {
    try (PDDocument doc1 = new PDDocument()) {
      doc1.addPage(new PDPage());
      doc1.addPage(new PDPage());
      doc1.addPage(new PDPage());
      doc1.save(new File(TARGETTESTDIR, "MergerOpenActionTest1.pdf"));
    }

    PDPageDestination dest;
    try (PDDocument doc2 = new PDDocument()) {
      doc2.addPage(new PDPage());
      doc2.addPage(new PDPage());
      doc2.addPage(new PDPage());
      dest = new PDPageFitDestination();
      dest.setPage(doc2.getPage(1));
      doc2.getDocumentCatalog().setOpenAction(dest);
      doc2.save(new File(TARGETTESTDIR, "MergerOpenActionTest2.pdf"));
    }

    final PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
    pdfMergerUtility.addSource(new File(TARGETTESTDIR, "MergerOpenActionTest1.pdf"));
    pdfMergerUtility.addSource(new File(TARGETTESTDIR, "MergerOpenActionTest2.pdf"));
    pdfMergerUtility.setDestinationFileName(TARGETTESTDIR + "MergerOpenActionTestResult.pdf");
    pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

    try (PDDocument mergedDoc = PDDocument.load(new File(TARGETTESTDIR, "MergerOpenActionTestResult.pdf"))) {
      final PDDocumentCatalog documentCatalog = mergedDoc.getDocumentCatalog();
      dest = (PDPageDestination) documentCatalog.getOpenAction();
      TestCase.assertEquals(4, documentCatalog.getPages().indexOf(dest.getPage()));
    }
  }

  /**
   * PDFBOX-3999: check that page entries in the structure tree only reference
   * pages from the page tree, i.e. that no orphan pages exist.
   * 
   * @throws IOException
   */
  public void testStructureTreeMerge() throws IOException {
    final PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
    final PDDocument src = PDDocument
        .load(new File(PDFMergerUtilityTest.TARGETPDFDIR, "PDFBOX-3999-GeneralForbearance.pdf"));

    ElementCounter elementCounter = new ElementCounter();
    elementCounter.walk(src.getDocumentCatalog().getStructureTreeRoot().getK());
    final int singleCnt = elementCounter.cnt;
    final int singleSetSize = elementCounter.set.size();
    TestCase.assertEquals(134, singleCnt);
    TestCase.assertEquals(134, singleSetSize);

    final PDDocument dst = PDDocument
        .load(new File(PDFMergerUtilityTest.TARGETPDFDIR, "PDFBOX-3999-GeneralForbearance.pdf"));
    pdfMergerUtility.appendDocument(dst, src);
    src.close();
    dst.save(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-merged.pdf"));
    dst.close();

    final PDDocument doc = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-merged.pdf"));

    // Assume that the merged tree has double element count
    elementCounter = new ElementCounter();
    elementCounter.walk(doc.getDocumentCatalog().getStructureTreeRoot().getK());
    TestCase.assertEquals(singleCnt * 2, elementCounter.cnt);
    TestCase.assertEquals(singleSetSize * 2, elementCounter.set.size());
    checkForPageOrphans(doc);

    doc.close();
  }

  /**
   * PDFBOX-3999: check that no streams are kept from the source document by the
   * destination document, despite orphan annotations remaining in the structure
   * tree.
   *
   * @throws IOException
   */
  public void testStructureTreeMerge2() throws IOException {
    final PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
    PDDocument doc = PDDocument.load(new File(PDFMergerUtilityTest.TARGETPDFDIR, "PDFBOX-3999-GeneralForbearance.pdf"));
    doc.getDocumentCatalog().getAcroForm().flatten();
    doc.save(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-flattened.pdf"));

    ElementCounter elementCounter = new ElementCounter();
    elementCounter.walk(doc.getDocumentCatalog().getStructureTreeRoot().getK());
    final int singleCnt = elementCounter.cnt;
    final int singleSetSize = elementCounter.set.size();
    TestCase.assertEquals(134, singleCnt);
    TestCase.assertEquals(134, singleSetSize);

    doc.close();

    final PDDocument src = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-flattened.pdf"));
    final PDDocument dst = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-flattened.pdf"));
    pdfMergerUtility.appendDocument(dst, src);
    // before solving PDFBOX-3999, the close() below brought
    // IOException: COSStream has been closed and cannot be read.
    src.close();
    dst.save(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-flattened-merged.pdf"));
    dst.close();

    doc = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-3999-GeneralForbearance-flattened-merged.pdf"));

    checkForPageOrphans(doc);

    // Assume that the merged tree has double element count
    elementCounter = new ElementCounter();
    elementCounter.walk(doc.getDocumentCatalog().getStructureTreeRoot().getK());
    TestCase.assertEquals(singleCnt * 2, elementCounter.cnt);
    TestCase.assertEquals(singleSetSize * 2, elementCounter.set.size());

    doc.close();
  }

  /**
   * PDFBOX-4408: Check that /StructParents values from pages and /StructParent
   * values from annotations are found in the /ParentTree.
   *
   * @throws IOException
   */
  public void testStructureTreeMerge3() throws IOException {
    final PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
    final PDDocument src = PDDocument.load(new File(PDFMergerUtilityTest.TARGETPDFDIR, "PDFBOX-4408.pdf"));

    ElementCounter elementCounter = new ElementCounter();
    elementCounter.walk(src.getDocumentCatalog().getStructureTreeRoot().getK());
    final int singleCnt = elementCounter.cnt;
    final int singleSetSize = elementCounter.set.size();
    TestCase.assertEquals(25, singleCnt);
    TestCase.assertEquals(25, singleSetSize);

    PDDocument dst = PDDocument.load(new File(PDFMergerUtilityTest.TARGETPDFDIR, "PDFBOX-4408.pdf"));
    pdfMergerUtility.appendDocument(dst, src);
    src.close();
    dst.save(new File(TARGETTESTDIR, "PDFBOX-4408-merged.pdf"));
    dst.close();

    dst = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-4408-merged.pdf"));

    // Assume that the merged tree has double element count
    elementCounter = new ElementCounter();
    elementCounter.walk(dst.getDocumentCatalog().getStructureTreeRoot().getK());
    TestCase.assertEquals(singleCnt * 2, elementCounter.cnt);
    TestCase.assertEquals(singleSetSize * 2, elementCounter.set.size());

    checkWithNumberTree(dst);
    checkForPageOrphans(dst);
    dst.close();
    checkStructTreeRootCount(new File(TARGETTESTDIR, "PDFBOX-4408-merged.pdf"));
  }

  /**
   * PDFBOX-4417: Same as the previous tests, but this one failed when the
   * previous tests succeeded because of more bugs with cloning.
   *
   * @throws IOException
   */
  public void testStructureTreeMerge4() throws IOException {
    final PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
    final PDDocument src = PDDocument.load(new File(SRCDIR, "PDFBOX-4417-001031.pdf"));

    ElementCounter elementCounter = new ElementCounter();
    elementCounter.walk(src.getDocumentCatalog().getStructureTreeRoot().getK());
    final int singleCnt = elementCounter.cnt;
    final int singleSetSize = elementCounter.set.size();
    TestCase.assertEquals(104, singleCnt);
    TestCase.assertEquals(104, singleSetSize);

    PDDocument dst = PDDocument.load(new File(SRCDIR, "PDFBOX-4417-001031.pdf"));
    pdfMergerUtility.appendDocument(dst, src);
    src.close();
    dst.save(new File(TARGETTESTDIR, "PDFBOX-4417-001031-merged.pdf"));
    dst.close();
    dst = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-4417-001031-merged.pdf"));

    // Assume that the merged tree has double element count
    elementCounter = new ElementCounter();
    elementCounter.walk(dst.getDocumentCatalog().getStructureTreeRoot().getK());
    TestCase.assertEquals(singleCnt * 2, elementCounter.cnt);
    TestCase.assertEquals(singleSetSize * 2, elementCounter.set.size());

    checkWithNumberTree(dst);
    checkForPageOrphans(dst);
    dst.close();
    checkStructTreeRootCount(new File(TARGETTESTDIR, "PDFBOX-4417-001031-merged.pdf"));
  }

  /**
   * PDFBOX-4417: Same as the previous tests, but this one failed when the
   * previous tests succeeded because the /K tree started with two dictionaries
   * and not with an array.
   *
   * @throws IOException
   */
  public void testStructureTreeMerge5() throws IOException {
    final PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
    final PDDocument src = PDDocument.load(new File(SRCDIR, "PDFBOX-4417-054080.pdf"));

    ElementCounter elementCounter = new ElementCounter();
    elementCounter.walk(src.getDocumentCatalog().getStructureTreeRoot().getK());
    final int singleCnt = elementCounter.cnt;
    final int singleSetSize = elementCounter.set.size();

    PDDocument dst = PDDocument.load(new File(SRCDIR, "PDFBOX-4417-054080.pdf"));
    pdfMergerUtility.appendDocument(dst, src);
    src.close();
    dst.save(new File(TARGETTESTDIR, "PDFBOX-4417-054080-merged.pdf"));
    dst.close();
    dst = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-4417-054080-merged.pdf"));
    checkWithNumberTree(dst);
    checkForPageOrphans(dst);

    // Assume that the merged tree has double element count
    elementCounter = new ElementCounter();
    elementCounter.walk(dst.getDocumentCatalog().getStructureTreeRoot().getK());
    TestCase.assertEquals(singleCnt * 2, elementCounter.cnt);
    TestCase.assertEquals(singleSetSize * 2, elementCounter.set.size());

    dst.close();

    checkStructTreeRootCount(new File(TARGETTESTDIR, "PDFBOX-4417-054080-merged.pdf"));
  }

  /**
   * PDFBOX-4418: test merging PDFs where ParentTree have a hierarchy.
   * 
   * @throws IOException
   */
  public void testStructureTreeMerge6() throws IOException {
    final PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
    final PDDocument src = PDDocument.load(new File(PDFMergerUtilityTest.TARGETPDFDIR, "PDFBOX-4418-000671.pdf"));

    PDStructureTreeRoot structureTreeRoot = src.getDocumentCatalog().getStructureTreeRoot();
    PDNumberTreeNode parentTree = structureTreeRoot.getParentTree();
    Map<Integer, COSObjectable> numberTreeAsMap = PDFMergerUtility.getNumberTreeAsMap(parentTree);
    TestCase.assertEquals(381, numberTreeAsMap.size());
    TestCase.assertEquals(743, Collections.max(numberTreeAsMap.keySet()) + 1);
    TestCase.assertEquals(0, (int) Collections.min(numberTreeAsMap.keySet()));
    TestCase.assertEquals(743, structureTreeRoot.getParentTreeNextKey());

    PDDocument dst = PDDocument.load(new File(PDFMergerUtilityTest.TARGETPDFDIR, "PDFBOX-4418-000314.pdf"));

    structureTreeRoot = dst.getDocumentCatalog().getStructureTreeRoot();
    parentTree = structureTreeRoot.getParentTree();
    numberTreeAsMap = PDFMergerUtility.getNumberTreeAsMap(parentTree);
    TestCase.assertEquals(7, numberTreeAsMap.size());
    TestCase.assertEquals(328, Collections.max(numberTreeAsMap.keySet()) + 1);
    TestCase.assertEquals(321, (int) Collections.min(numberTreeAsMap.keySet()));
    // ParentTreeNextKey should be 321 but PDF has a higher value
    TestCase.assertEquals(408, structureTreeRoot.getParentTreeNextKey());

    pdfMergerUtility.appendDocument(dst, src);
    src.close();
    dst.save(new File(TARGETTESTDIR, "PDFBOX-4418-merged.pdf"));
    dst.close();

    dst = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-4418-merged.pdf"));
    checkWithNumberTree(dst);
    checkForPageOrphans(dst);

    structureTreeRoot = dst.getDocumentCatalog().getStructureTreeRoot();
    parentTree = structureTreeRoot.getParentTree();
    numberTreeAsMap = PDFMergerUtility.getNumberTreeAsMap(parentTree);
    TestCase.assertEquals(381 + 7, numberTreeAsMap.size());
    TestCase.assertEquals(408 + 743, Collections.max(numberTreeAsMap.keySet()) + 1);
    TestCase.assertEquals(321, (int) Collections.min(numberTreeAsMap.keySet()));
    TestCase.assertEquals(408 + 743, structureTreeRoot.getParentTreeNextKey());
    dst.close();

    checkStructTreeRootCount(new File(TARGETTESTDIR, "PDFBOX-4418-merged.pdf"));
  }

  /**
   * PDFBOX-4423: test merging a PDF where a widget has no StructParent.
   * 
   * @throws IOException
   */
  public void testStructureTreeMerge7() throws IOException {
    final PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
    final PDDocument src = PDDocument.load(new File(PDFMergerUtilityTest.TARGETPDFDIR, "PDFBOX-4423-000746.pdf"));

    PDStructureTreeRoot structureTreeRoot = src.getDocumentCatalog().getStructureTreeRoot();
    PDNumberTreeNode parentTree = structureTreeRoot.getParentTree();
    Map<Integer, COSObjectable> numberTreeAsMap = PDFMergerUtility.getNumberTreeAsMap(parentTree);
    TestCase.assertEquals(33, numberTreeAsMap.size());
    TestCase.assertEquals(64, Collections.max(numberTreeAsMap.keySet()) + 1);
    TestCase.assertEquals(31, (int) Collections.min(numberTreeAsMap.keySet()));
    TestCase.assertEquals(126, structureTreeRoot.getParentTreeNextKey());

    PDDocument dst = new PDDocument();

    pdfMergerUtility.appendDocument(dst, src);
    src.close();
    dst.save(new File(TARGETTESTDIR, "PDFBOX-4423-merged.pdf"));
    dst.close();

    dst = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-4423-merged.pdf"));
    checkWithNumberTree(dst);
    checkForPageOrphans(dst);

    structureTreeRoot = dst.getDocumentCatalog().getStructureTreeRoot();
    parentTree = structureTreeRoot.getParentTree();
    numberTreeAsMap = PDFMergerUtility.getNumberTreeAsMap(parentTree);
    TestCase.assertEquals(33, numberTreeAsMap.size());
    TestCase.assertEquals(64, Collections.max(numberTreeAsMap.keySet()) + 1);
    TestCase.assertEquals(31, (int) Collections.min(numberTreeAsMap.keySet()));
    TestCase.assertEquals(64, structureTreeRoot.getParentTreeNextKey());
    dst.close();

    checkStructTreeRootCount(new File(TARGETTESTDIR, "PDFBOX-4423-merged.pdf"));
  }

  /**
   * PDFBOX-4009: Test that ParentTreeNextKey is recalculated correctly.
   */
  public void testMissingParentTreeNextKey() throws IOException {
    final PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
    final PDDocument src = PDDocument.load(new File(PDFMergerUtilityTest.TARGETPDFDIR, "PDFBOX-4418-000314.pdf"));
    PDDocument dst = PDDocument.load(new File(PDFMergerUtilityTest.TARGETPDFDIR, "PDFBOX-4418-000314.pdf"));
    // existing numbers are 321..327; ParentTreeNextKey is 408.
    // After deletion, it is recalculated in the merge 328.
    // That value is added to all numbers of the destination,
    // so the new numbers should be 321+328..327+328, i.e. 649..655,
    // and this ParentTreeNextKey is 656 at the end.
    dst.getDocumentCatalog().getStructureTreeRoot().getCOSObject().removeItem(COSName.PARENT_TREE_NEXT_KEY);
    pdfMergerUtility.appendDocument(dst, src);
    src.close();
    dst.save(new File(TARGETTESTDIR, "PDFBOX-4418-000314-merged.pdf"));
    dst.close();
    dst = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-4418-000314-merged.pdf"));
    TestCase.assertEquals(656, dst.getDocumentCatalog().getStructureTreeRoot().getParentTreeNextKey());
    dst.close();
  }

  /**
   * PDFBOX-4416: Test merging of /IDTree <br>
   * PDFBOX-4009: test merging to empty destination
   *
   * @throws IOException
   */
  public void testStructureTreeMergeIDTree() throws IOException {
    final PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
    PDDocument src = PDDocument.load(new File(SRCDIR, "PDFBOX-4417-001031.pdf"));
    PDDocument dst = PDDocument.load(new File(SRCDIR, "PDFBOX-4417-054080.pdf"));

    final PDNameTreeNode<PDStructureElement> srcIDTree = src.getDocumentCatalog().getStructureTreeRoot().getIDTree();
    final Map<String, PDStructureElement> srcIDTreeMap = PDFMergerUtility.getIDTreeAsMap(srcIDTree);
    PDNameTreeNode<PDStructureElement> dstIDTree = dst.getDocumentCatalog().getStructureTreeRoot().getIDTree();
    Map<String, PDStructureElement> dstIDTreeMap = PDFMergerUtility.getIDTreeAsMap(dstIDTree);
    final int expectedTotal = srcIDTreeMap.size() + dstIDTreeMap.size();
    TestCase.assertEquals(192, expectedTotal);

    // PDFBOX-4009, test that empty dest doc still merges structure tree
    // (empty dest doc is used in command line app)
    final PDDocument emptyDest = new PDDocument();
    pdfMergerUtility.appendDocument(emptyDest, src);
    src.close();
    src = emptyDest;
    TestCase.assertEquals(4, src.getDocumentCatalog().getStructureTreeRoot().getParentTreeNextKey());

    pdfMergerUtility.appendDocument(dst, src);
    src.close();
    dst.save(new File(TARGETTESTDIR, "PDFBOX-4416-IDTree-merged.pdf"));
    dst.close();
    dst = PDDocument.load(new File(TARGETTESTDIR, "PDFBOX-4416-IDTree-merged.pdf"));
    checkWithNumberTree(dst);
    checkForPageOrphans(dst);

    dstIDTree = dst.getDocumentCatalog().getStructureTreeRoot().getIDTree();
    dstIDTreeMap = PDFMergerUtility.getIDTreeAsMap(dstIDTree);
    TestCase.assertEquals(expectedTotal, dstIDTreeMap.size());

    dst.close();
    checkStructTreeRootCount(new File(TARGETTESTDIR, "PDFBOX-4416-IDTree-merged.pdf"));
  }

  /**
   * PDFBOX-4429: merge into destination that has /StructParent(s) entries in the
   * destination file but no structure tree.
   *
   * @throws IOException
   */
  public void testMergeBogusStructParents1() throws IOException {
    final PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
    final PDDocument src = PDDocument.load(new File(PDFMergerUtilityTest.TARGETPDFDIR, "PDFBOX-4408.pdf"));
    final PDDocument dst = PDDocument.load(new File(PDFMergerUtilityTest.TARGETPDFDIR, "PDFBOX-4408.pdf"));
    dst.getDocumentCatalog().setStructureTreeRoot(null);
    dst.getPage(0).setStructParents(9999);
    dst.getPage(0).getAnnotations().get(0).setStructParent(9998);
    pdfMergerUtility.appendDocument(dst, src);
    checkWithNumberTree(dst);
    checkForPageOrphans(dst);
    src.close();
    dst.close();
  }

  /**
   * PDFBOX-4429: merge into destination that has /StructParent(s) entries in the
   * source file but no structure tree.
   *
   * @throws IOException
   */
  public void testMergeBogusStructParents2() throws IOException {
    final PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
    final PDDocument src = PDDocument.load(new File(PDFMergerUtilityTest.TARGETPDFDIR, "PDFBOX-4408.pdf"));
    final PDDocument dst = PDDocument.load(new File(PDFMergerUtilityTest.TARGETPDFDIR, "PDFBOX-4408.pdf"));
    src.getDocumentCatalog().setStructureTreeRoot(null);
    src.getPage(0).setStructParents(9999);
    src.getPage(0).getAnnotations().get(0).setStructParent(9998);
    pdfMergerUtility.appendDocument(dst, src);
    checkWithNumberTree(dst);
    checkForPageOrphans(dst);
    src.close();
    dst.close();
  }

  /**
   * Test of the parent tree. Didn't work before PDFBOX-4003 because of
   * incompatible class for PDNumberTreeNode.
   *
   * @throws IOException
   */
  public void testParentTree() throws IOException {
    final PDDocument doc = PDDocument
        .load(new File(PDFMergerUtilityTest.TARGETPDFDIR, "PDFBOX-3999-GeneralForbearance.pdf"));
    final PDStructureTreeRoot structureTreeRoot = doc.getDocumentCatalog().getStructureTreeRoot();
    final PDNumberTreeNode parentTree = structureTreeRoot.getParentTree();
    parentTree.getValue(0);
    final Map<Integer, COSObjectable> numberTreeAsMap = PDFMergerUtility.getNumberTreeAsMap(parentTree);
    TestCase.assertEquals(31, numberTreeAsMap.size());
    TestCase.assertEquals(31, Collections.max(numberTreeAsMap.keySet()) + 1);
    TestCase.assertEquals(0, (int) Collections.min(numberTreeAsMap.keySet()));
    TestCase.assertEquals(31, structureTreeRoot.getParentTreeNextKey());
    doc.close();
  }

  // PDFBOX-4417: check for multiple /StructTreeRoot entries that was due to
  // incorrect merging of /K entries
  private void checkStructTreeRootCount(final File file) throws IOException {
    int count = 0;
    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.equals("/Type /StructTreeRoot")) {
          ++count;
        }
      }
    }
    TestCase.assertEquals(1, count);
  }

  /**
   * PDFBOX-4408: Check that /StructParents values from pages and /StructParent
   * values from annotations are found in the /ParentTree.
   *
   * @param document
   */
  void checkWithNumberTree(final PDDocument document) throws IOException {
    final PDDocumentCatalog documentCatalog = document.getDocumentCatalog();
    final PDNumberTreeNode parentTree = documentCatalog.getStructureTreeRoot().getParentTree();
    final Map<Integer, COSObjectable> numberTreeAsMap = PDFMergerUtility.getNumberTreeAsMap(parentTree);
    final Set<Integer> keySet = numberTreeAsMap.keySet();
    final PDAcroForm acroForm = documentCatalog.getAcroForm();
    if (acroForm != null) {
      for (final PDField field : acroForm.getFieldTree()) {
        for (final PDAnnotationWidget widget : field.getWidgets()) {
          if (widget.getStructParent() >= 0) {
            TestCase.assertTrue("field '" + field.getFullyQualifiedName() + "' /StructParent "
                + widget.getStructParent() + " missing in /ParentTree", keySet.contains(widget.getStructParent()));
          }
        }
      }
    }
    for (final PDPage page : document.getPages()) {
      if (page.getStructParents() >= 0) {
        TestCase.assertTrue(keySet.contains(page.getStructParents()));
      }
      for (final PDAnnotation ann : page.getAnnotations()) {
        if (ann.getStructParent() >= 0) {
          TestCase.assertTrue("/StructParent " + ann.getStructParent() + " missing in /ParentTree",
              keySet.contains(ann.getStructParent()));
        }
      }
    }

    // might also test image and form dictionaries...
  }

  /**
   * PDFBOX-4383: Test that file can be deleted after merge.
   *
   * @throws IOException
   */
  public void testFileDeletion() throws IOException {
    final File outFile = new File(TARGETTESTDIR, "PDFBOX-4383-result.pdf");

    final File inFile1 = new File(TARGETTESTDIR, "PDFBOX-4383-src1.pdf");
    final File inFile2 = new File(TARGETTESTDIR, "PDFBOX-4383-src2.pdf");

    createSimpleFile(inFile1);
    createSimpleFile(inFile2);

    try (OutputStream out = new FileOutputStream(outFile)) {
      final PDFMergerUtility merger = new PDFMergerUtility();
      merger.setDestinationStream(out);

      merger.addSource(inFile1);
      merger.addSource(inFile2);

      merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
    }

    Files.delete(inFile1.toPath());
    Files.delete(inFile2.toPath());
    Files.delete(outFile.toPath());
  }

  private void checkForPageOrphans(final PDDocument doc) throws IOException {
    // check for orphan pages in the StructTreeRoot/K, StructTreeRoot/ParentTree and
    // StructTreeRoot/IDTree trees.
    final PDPageTree pageTree = doc.getPages();
    final PDStructureTreeRoot structureTreeRoot = doc.getDocumentCatalog().getStructureTreeRoot();
    checkElement(pageTree, structureTreeRoot.getParentTree().getCOSObject());
    checkElement(pageTree, structureTreeRoot.getK());
    checkForIDTreeOrphans(pageTree, structureTreeRoot);
  }

  private void checkForIDTreeOrphans(final PDPageTree pageTree, final PDStructureTreeRoot structureTreeRoot)
      throws IOException {
    final PDNameTreeNode<PDStructureElement> idTree = structureTreeRoot.getIDTree();
    if (idTree == null)
      return;
    final Map<String, PDStructureElement> map = PDFMergerUtility.getIDTreeAsMap(idTree);
    for (final PDStructureElement element : map.values()) {
      if (element.getPage() != null) {
        checkForPage(pageTree, element);
      }
      if (!element.getKids().isEmpty()) {
        checkElement(pageTree, element.getCOSObject().getDictionaryObject(COSName.K));
      }
    }
  }

  private void createSimpleFile(final File file) throws IOException {
    try (PDDocument doc = new PDDocument()) {
      doc.addPage(new PDPage());
      doc.save(file);
    }
  }

  private class ElementCounter {
    int cnt = 0;
    Set<COSBase> set = new HashSet<>();

    void walk(final COSBase base) {
      if (base instanceof COSArray) {
        for (COSBase base2 : (COSArray) base) {
          if (base2 instanceof COSObject) {
            base2 = ((COSObject) base2).getObject();
          }
          walk(base2);
        }
      } else if (base instanceof COSDictionary) {
        final COSDictionary kdict = (COSDictionary) base;
        if (kdict.containsKey(COSName.PG)) {
          ++cnt;
          set.add(kdict);
        }
        if (kdict.containsKey(COSName.K)) {
          walk(kdict.getDictionaryObject(COSName.K));
        }
      }
    }
  }

  // Each element can be an array, a dictionary or a number.
  // See PDF specification Table 37 - Entries in a number tree node dictionary
  // See PDF specification Table 322 - Entries in the structure tree root
  // See PDF specification Table 323 - Entries in a structure element dictionary
  // See PDF specification Table 325 – Entries in an object reference dictionary
  // example of file with /Kids: 000153.pdf 000208.pdf 000314.pdf 000359.pdf
  // 000671.pdf
  // from digitalcorpora site
  private void checkElement(final PDPageTree pageTree, final COSBase base) throws IOException {
    if (base instanceof COSArray) {
      for (COSBase base2 : (COSArray) base) {
        if (base2 instanceof COSObject) {
          base2 = ((COSObject) base2).getObject();
        }
        checkElement(pageTree, base2);
      }
    } else if (base instanceof COSDictionary) {
      final COSDictionary kdict = (COSDictionary) base;
      if (kdict.containsKey(COSName.PG)) {
        final PDStructureElement structureElement = new PDStructureElement(kdict);
        checkForPage(pageTree, structureElement);
      }
      if (kdict.containsKey(COSName.K)) {
        checkElement(pageTree, kdict.getDictionaryObject(COSName.K));
        return;
      }

      // if we're in a number tree, check /Nums and /Kids
      if (kdict.containsKey(COSName.KIDS)) {
        checkElement(pageTree, kdict.getDictionaryObject(COSName.KIDS));
      } else if (kdict.containsKey(COSName.NUMS)) {
        checkElement(pageTree, kdict.getDictionaryObject(COSName.NUMS));
      }

      // if we're an object reference dictionary (/OBJR), check the obj
      if (kdict.containsKey(COSName.OBJ)) {
        final COSDictionary obj = (COSDictionary) kdict.getDictionaryObject(COSName.OBJ);
        final COSBase type = obj.getDictionaryObject(COSName.TYPE);
        if (COSName.ANNOT.equals(type)) {
          final PDAnnotation annotation = PDAnnotation.createAnnotation(obj);
          final PDPage page = annotation.getPage();
          if (page != null) {
            if (pageTree.indexOf(page) == -1) {
              final COSBase item = kdict.getItem(COSName.OBJ);
              if (item instanceof COSObject) {
                TestCase.assertTrue("Annotation page is not in the page tree: " + item, pageTree.indexOf(page) != -1);
              } else {
                // don't display because of stack overflow
                TestCase.assertTrue("Annotation page is not in the page tree", pageTree.indexOf(page) != -1);
              }
            }
          }
        } else {
          // TODO needs to be investigated. Specification mentions
          // "such as an XObject or an annotation"
          TestCase.fail("Other type: " + type);
        }
      }
    }
  }

  // checks that the result file of a merge has the same rendering as the two
  // source files
  private void checkMergeIdentical(final String filename1, final String filename2, final String mergeFilename,
      final MemoryUsageSetting memUsageSetting) throws IOException {
    int src1PageCount;
    BufferedImage[] src1ImageTab;
    try (PDDocument srcDoc1 = PDDocument.load(new File(SRCDIR, filename1), (String) null)) {
      src1PageCount = srcDoc1.getNumberOfPages();
      final PDFRenderer src1PdfRenderer = new PDFRenderer(srcDoc1);
      src1ImageTab = new BufferedImage[src1PageCount];
      for (int page = 0; page < src1PageCount; ++page) {
        src1ImageTab[page] = src1PdfRenderer.renderImageWithDPI(page, DPI);
      }
    }

    int src2PageCount;
    BufferedImage[] src2ImageTab;
    try (PDDocument srcDoc2 = PDDocument.load(new File(SRCDIR, filename2), (String) null)) {
      src2PageCount = srcDoc2.getNumberOfPages();
      final PDFRenderer src2PdfRenderer = new PDFRenderer(srcDoc2);
      src2ImageTab = new BufferedImage[src2PageCount];
      for (int page = 0; page < src2PageCount; ++page) {
        src2ImageTab[page] = src2PdfRenderer.renderImageWithDPI(page, DPI);
      }
    }

    final PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
    pdfMergerUtility.addSource(new File(SRCDIR, filename1));
    pdfMergerUtility.addSource(new File(SRCDIR, filename2));
    pdfMergerUtility.setDestinationFileName(TARGETTESTDIR + mergeFilename);
    pdfMergerUtility.mergeDocuments(memUsageSetting);

    try (PDDocument mergedDoc = PDDocument.load(new File(TARGETTESTDIR, mergeFilename), (String) null)) {
      final PDFRenderer mergePdfRenderer = new PDFRenderer(mergedDoc);
      final int mergePageCount = mergedDoc.getNumberOfPages();
      TestCase.assertEquals(src1PageCount + src2PageCount, mergePageCount);
      for (int page = 0; page < src1PageCount; ++page) {
        final BufferedImage bim = mergePdfRenderer.renderImageWithDPI(page, DPI);
        checkImagesIdentical(bim, src1ImageTab[page]);
      }
      for (int page = 0; page < src2PageCount; ++page) {
        final int mergePage = page + src1PageCount;
        final BufferedImage bim = mergePdfRenderer.renderImageWithDPI(mergePage, DPI);
        checkImagesIdentical(bim, src2ImageTab[page]);
      }
    }
  }

  private void checkImagesIdentical(final BufferedImage bim1, final BufferedImage bim2) {
    TestCase.assertEquals(bim1.getHeight(), bim2.getHeight());
    TestCase.assertEquals(bim1.getWidth(), bim2.getWidth());
    final int w = bim1.getWidth();
    final int h = bim1.getHeight();
    for (int i = 0; i < w; ++i) {
      for (int j = 0; j < h; ++j) {
        TestCase.assertEquals(bim1.getRGB(i, j), bim2.getRGB(i, j));
      }
    }
  }

  private void checkForPage(final PDPageTree pageTree, final PDStructureElement structureElement) {
    final PDPage page = structureElement.getPage();
    if (page != null) {
      TestCase.assertTrue("Page is not in the page tree", pageTree.indexOf(page) != -1);
    }
  }
}
