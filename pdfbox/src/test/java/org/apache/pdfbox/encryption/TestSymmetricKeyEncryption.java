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
package org.apache.pdfbox.encryption;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.PDEncryption;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.encryption.StandardSecurityHandler;
import org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Charsets;
import org.junit.Assert;

import junit.framework.TestCase;

/**
 * Tests for symmetric key encryption.
 *
 * IMPORTANT! When making changes in the encryption / decryption methods, do
 * also check whether the six generated encrypted files (to be found in
 * pdfbox/target/test-output/crypto and named *encrypted.pdf) can be opened with
 * Adobe Reader by providing the owner password and the user password.
 *
 * @author Ralf Hauser
 * @author Tilman Hausherr
 *
 */
public class TestSymmetricKeyEncryption extends TestCase {
  /**
   * Logger instance.
   */
  private static final Log LOG = LogFactory.getLog(TestSymmetricKeyEncryption.class);

  private final File testResultsDir = new File("target/test-output/crypto");

  private AccessPermission permission;

  static final String USERPASSWORD = "1234567890abcdefghijk1234567890abcdefghijk";
  static final String OWNERPASSWORD = "abcdefghijk1234567890abcdefghijk1234567890";

  /**
   * {@inheritDoc}
   */
  @Override
  protected void setUp() throws Exception {
    testResultsDir.mkdirs();

    if (Cipher.getMaxAllowedKeyLength("AES") != Integer.MAX_VALUE) {
      // we need strong encryption for these tests
      TestCase.fail("JCE unlimited strength jurisdiction policy files are not installed");
    }

    permission = new AccessPermission();
    permission.setCanAssembleDocument(false);
    permission.setCanExtractContent(false);
    permission.setCanExtractForAccessibility(true);
    permission.setCanFillInForm(false);
    permission.setCanModify(false);
    permission.setCanModifyAnnotations(false);
    permission.setCanPrint(true);
    permission.setCanPrintDegraded(false);
    permission.setReadOnly();
  }

  /**
   * Test that permissions work as intended: the user psw ("user") is enough to
   * open the PDF with possibly restricted rights, the owner psw ("owner") gives
   * full permissions. The 3 files of this test were created by Maruan Sahyoun,
   * NOT with PDFBox, but with Adobe Acrobat to ensure "the gold standard". The
   * restricted permissions prevent printing and text extraction. In the 128 and
   * 256 bit encrypted files, AssembleDocument, ExtractForAccessibility and
   * PrintDegraded are also disabled.
   */
  public void testPermissions() throws Exception {
    final AccessPermission fullAP = new AccessPermission();
    final AccessPermission restrAP = new AccessPermission();
    restrAP.setCanPrint(false);
    restrAP.setCanExtractContent(false);
    restrAP.setCanModify(false);

    byte[] inputFileAsByteArray = getFileResourceAsByteArray("PasswordSample-40bit.pdf");
    checkPerms(inputFileAsByteArray, "owner", fullAP);
    checkPerms(inputFileAsByteArray, "user", restrAP);
    try {
      checkPerms(inputFileAsByteArray, "", null);
      TestCase.fail("wrong password not detected");
    } catch (final IOException ex) {
      TestCase.assertEquals("Cannot decrypt PDF, the password is incorrect", ex.getMessage());
    }

    restrAP.setCanAssembleDocument(false);
    restrAP.setCanExtractForAccessibility(false);
    restrAP.setCanPrintDegraded(false);

    inputFileAsByteArray = getFileResourceAsByteArray("PasswordSample-128bit.pdf");
    checkPerms(inputFileAsByteArray, "owner", fullAP);
    checkPerms(inputFileAsByteArray, "user", restrAP);
    try {
      checkPerms(inputFileAsByteArray, "", null);
      TestCase.fail("wrong password not detected");
    } catch (final IOException ex) {
      TestCase.assertEquals("Cannot decrypt PDF, the password is incorrect", ex.getMessage());
    }

    inputFileAsByteArray = getFileResourceAsByteArray("PasswordSample-256bit.pdf");
    checkPerms(inputFileAsByteArray, "owner", fullAP);
    checkPerms(inputFileAsByteArray, "user", restrAP);
    try {
      checkPerms(inputFileAsByteArray, "", null);
      TestCase.fail("wrong password not detected");
    } catch (final IOException ex) {
      TestCase.assertEquals("Cannot decrypt PDF, the password is incorrect", ex.getMessage());
    }
  }

  private void checkPerms(final byte[] inputFileAsByteArray, final String password,
      final AccessPermission expectedPermissions) throws IOException {
    final PDDocument doc = PDDocument.load(inputFileAsByteArray, password);

    final AccessPermission currentAccessPermission = doc.getCurrentAccessPermission();

    // check permissions
    TestCase.assertEquals(expectedPermissions.isOwnerPermission(), currentAccessPermission.isOwnerPermission());
    if (!expectedPermissions.isOwnerPermission()) {
      TestCase.assertEquals(true, currentAccessPermission.isReadOnly());
    }
    TestCase.assertEquals(expectedPermissions.canAssembleDocument(), currentAccessPermission.canAssembleDocument());
    TestCase.assertEquals(expectedPermissions.canExtractContent(), currentAccessPermission.canExtractContent());
    TestCase.assertEquals(expectedPermissions.canExtractForAccessibility(),
        currentAccessPermission.canExtractForAccessibility());
    TestCase.assertEquals(expectedPermissions.canFillInForm(), currentAccessPermission.canFillInForm());
    TestCase.assertEquals(expectedPermissions.canModify(), currentAccessPermission.canModify());
    TestCase.assertEquals(expectedPermissions.canModifyAnnotations(), currentAccessPermission.canModifyAnnotations());
    TestCase.assertEquals(expectedPermissions.canPrint(), currentAccessPermission.canPrint());
    TestCase.assertEquals(expectedPermissions.canPrintDegraded(), currentAccessPermission.canPrintDegraded());

    new PDFRenderer(doc).renderImage(0);

    doc.close();
  }

  /**
   * Protect a document with a key and try to reopen it with that key and compare.
   *
   * @throws Exception If there is an unexpected error during the test.
   */
  public void testProtection() throws Exception {
    final byte[] inputFileAsByteArray = getFileResourceAsByteArray("Acroform-PDFBOX-2333.pdf");
    final int sizePriorToEncryption = inputFileAsByteArray.length;

    testSymmEncrForKeySize(40, false, sizePriorToEncryption, inputFileAsByteArray,
        TestSymmetricKeyEncryption.USERPASSWORD, TestSymmetricKeyEncryption.OWNERPASSWORD, permission);

    testSymmEncrForKeySize(128, false, sizePriorToEncryption, inputFileAsByteArray,
        TestSymmetricKeyEncryption.USERPASSWORD, TestSymmetricKeyEncryption.OWNERPASSWORD, permission);

    testSymmEncrForKeySize(128, true, sizePriorToEncryption, inputFileAsByteArray,
        TestSymmetricKeyEncryption.USERPASSWORD, TestSymmetricKeyEncryption.OWNERPASSWORD, permission);

    testSymmEncrForKeySize(256, true, sizePriorToEncryption, inputFileAsByteArray,
        TestSymmetricKeyEncryption.USERPASSWORD, TestSymmetricKeyEncryption.OWNERPASSWORD, permission);
  }

  /**
   * PDFBOX-4308: test that index colorspace table string doesn't get corrupted
   * when encrypting. This happened because the colorspace was referenced twice,
   * once in the resources dictionary and once in an image in the resources
   * dictionary, and when saving the PDF the string was saved twice, once as a
   * direct object and once as an indirect object (both from the same java
   * object). Encryption used the wrong object number and/or the object was
   * encrypted twice.
   *
   * @throws IOException
   */
  public void testPDFBox4308() throws IOException {
    byte[] inputFileAsByteArray;
    try (InputStream is = new FileInputStream("target/pdfs/PDFBOX-4308.pdf")) {
      inputFileAsByteArray = IOUtils.toByteArray(is);
    }
    final int sizePriorToEncryption = inputFileAsByteArray.length;

    testSymmEncrForKeySize(40, false, sizePriorToEncryption, inputFileAsByteArray,
        TestSymmetricKeyEncryption.USERPASSWORD, TestSymmetricKeyEncryption.OWNERPASSWORD, permission);
  }

  /**
   * Protect a document with an embedded PDF with a key and try to reopen it with
   * that key and compare.
   *
   * @throws Exception If there is an unexpected error during the test.
   */
  public void testProtectionInnerAttachment() throws Exception {
    final String testFileName = "preEnc_20141025_105451.pdf";
    final byte[] inputFileWithEmbeddedFileAsByteArray = getFileResourceAsByteArray(testFileName);

    final int sizeOfFileWithEmbeddedFile = inputFileWithEmbeddedFileAsByteArray.length;

    final File extractedEmbeddedFile = extractEmbeddedFile(
        new ByteArrayInputStream(inputFileWithEmbeddedFileAsByteArray), "innerFile.pdf");

    testSymmEncrForKeySizeInner(40, false, sizeOfFileWithEmbeddedFile, inputFileWithEmbeddedFileAsByteArray,
        extractedEmbeddedFile, TestSymmetricKeyEncryption.USERPASSWORD, TestSymmetricKeyEncryption.OWNERPASSWORD);

    testSymmEncrForKeySizeInner(128, false, sizeOfFileWithEmbeddedFile, inputFileWithEmbeddedFileAsByteArray,
        extractedEmbeddedFile, TestSymmetricKeyEncryption.USERPASSWORD, TestSymmetricKeyEncryption.OWNERPASSWORD);

    testSymmEncrForKeySizeInner(128, true, sizeOfFileWithEmbeddedFile, inputFileWithEmbeddedFileAsByteArray,
        extractedEmbeddedFile, TestSymmetricKeyEncryption.USERPASSWORD, TestSymmetricKeyEncryption.OWNERPASSWORD);

    testSymmEncrForKeySizeInner(256, true, sizeOfFileWithEmbeddedFile, inputFileWithEmbeddedFileAsByteArray,
        extractedEmbeddedFile, TestSymmetricKeyEncryption.USERPASSWORD, TestSymmetricKeyEncryption.OWNERPASSWORD);
  }

  /**
   * PDFBOX-4453: verify that identical encrypted strings are really decrypted
   * each.
   * 
   * @throws IOException
   */
  public void testPDFBox4453() throws IOException {
    final int TESTCOUNT = 1000;
    final File file = new File(testResultsDir, "PDFBOX-4453.pdf");
    try (PDDocument doc = new PDDocument()) {
      doc.addPage(new PDPage());
      for (int i = 0; i < TESTCOUNT; ++i) {
        // strings must be in different dictionaries so that the actual
        // encryption key changes
        final COSDictionary dict = new COSDictionary();
        doc.getPage(0).getCOSObject().setItem(COSName.getPDFName("_Test-" + i), dict);
        // need two different keys so that there are both encrypted and decrypted
        // COSStrings
        // with value "0"
        dict.setString("key1", "3");
        dict.setString("key2", "0");
      }

      // RC4-40
      final StandardProtectionPolicy spp = new StandardProtectionPolicy("12345", "", new AccessPermission());
      spp.setEncryptionKeyLength(40);
      spp.setPreferAES(false);
      doc.protect(spp);
      doc.save(file);
    }

    try (PDDocument doc = PDDocument.load(file)) {
      Assert.assertTrue(doc.isEncrypted());
      for (int i = 0; i < TESTCOUNT; ++i) {
        final COSDictionary dict = doc.getPage(0).getCOSObject().getCOSDictionary(COSName.getPDFName("_Test-" + i));
        Assert.assertEquals("3", dict.getString("key1"));
        Assert.assertEquals("0", dict.getString("key2"));
      }
    }
  }

  private void testSymmEncrForKeySize(final int keyLength, final boolean preferAES, final int sizePriorToEncr,
      final byte[] inputFileAsByteArray, final String userpassword, final String ownerpassword,
      final AccessPermission permission) throws IOException {
    final PDDocument document = PDDocument.load(inputFileAsByteArray);
    final String prefix = "Simple-";
    final int numSrcPages = document.getNumberOfPages();
    PDFRenderer pdfRenderer = new PDFRenderer(document);
    final List<BufferedImage> srcImgTab = new ArrayList<>();
    final List<byte[]> srcContentStreamTab = new ArrayList<>();
    for (int i = 0; i < numSrcPages; ++i) {
      srcImgTab.add(pdfRenderer.renderImage(i));
      final InputStream unfilteredStream = document.getPage(i).getContents();
      final byte[] bytes = IOUtils.toByteArray(unfilteredStream);
      unfilteredStream.close();
      srcContentStreamTab.add(bytes);
    }

    final PDDocument encryptedDoc = encrypt(keyLength, preferAES, sizePriorToEncr, document, prefix, permission,
        userpassword, ownerpassword);

    Assert.assertEquals(numSrcPages, encryptedDoc.getNumberOfPages());
    pdfRenderer = new PDFRenderer(encryptedDoc);
    for (int i = 0; i < encryptedDoc.getNumberOfPages(); ++i) {
      // compare rendering
      final BufferedImage bim = pdfRenderer.renderImage(i);
      ValidateXImage.checkIdent(bim, srcImgTab.get(i));

      // compare content streams
      final InputStream unfilteredStream = encryptedDoc.getPage(i).getContents();
      final byte[] bytes = IOUtils.toByteArray(unfilteredStream);
      unfilteredStream.close();
      Assert.assertArrayEquals("content stream of page " + i + " not identical", srcContentStreamTab.get(i), bytes);
    }

    final File pdfFile = new File(testResultsDir,
        prefix + keyLength + "-bit-" + (preferAES ? "AES" : "RC4") + "-decrypted.pdf");
    encryptedDoc.setAllSecurityToBeRemoved(true);
    encryptedDoc.save(pdfFile);
    encryptedDoc.close();
  }

  // encrypt with keylength and permission, save, check sizes before and after
  // encryption
  // reopen, decrypt and return document
  private PDDocument encrypt(final int keyLength, final boolean preferAES, final int sizePriorToEncr,
      final PDDocument doc, final String prefix, final AccessPermission permission, final String userpassword,
      final String ownerpassword) throws IOException {
    final StandardProtectionPolicy spp = new StandardProtectionPolicy(ownerpassword, userpassword, permission);
    spp.setEncryptionKeyLength(keyLength);
    spp.setPreferAES(preferAES);

    // This must have no effect and should only log a warning.
    doc.setAllSecurityToBeRemoved(true);

    doc.protect(spp);

    final File pdfFile = new File(testResultsDir,
        prefix + keyLength + "-bit-" + (preferAES ? "AES" : "RC4") + "-encrypted.pdf");

    doc.save(pdfFile);
    doc.close();
    final long sizeEncrypted = pdfFile.length();
    Assert.assertTrue(
        keyLength + "-bit " + (preferAES ? "AES" : "RC4") + " encrypted pdf should not have same size as plain one",
        sizeEncrypted != sizePriorToEncr);

    // test with owner password => full permissions
    PDDocument encryptedDoc = PDDocument.load(pdfFile, ownerpassword);
    Assert.assertTrue(encryptedDoc.isEncrypted());
    Assert.assertTrue(encryptedDoc.getCurrentAccessPermission().isOwnerPermission());

    // Older encryption allows to get the user password when the owner password is
    // known
    final PDEncryption encryption = encryptedDoc.getEncryption();
    final int revision = encryption.getRevision();
    if (revision < 5) {
      final StandardSecurityHandler standardSecurityHandler = new StandardSecurityHandler();
      final int keyLengthInBytes = encryption.getVersion() == 1 ? 5 : encryption.getLength() / 8;
      final byte[] computedUserPassword = standardSecurityHandler.getUserPassword(
          ownerpassword.getBytes(Charsets.ISO_8859_1), encryption.getOwnerKey(), revision, keyLengthInBytes);
      Assert.assertEquals(userpassword.substring(0, 32), new String(computedUserPassword, Charsets.ISO_8859_1));
    }

    encryptedDoc.close();

    // test with user password => restricted permissions
    encryptedDoc = PDDocument.load(pdfFile, userpassword);
    Assert.assertTrue(encryptedDoc.isEncrypted());
    Assert.assertFalse(encryptedDoc.getCurrentAccessPermission().isOwnerPermission());

    TestCase.assertEquals(permission.getPermissionBytes(),
        encryptedDoc.getCurrentAccessPermission().getPermissionBytes());

    return encryptedDoc;
  }

  // extract the embedded file, saves it, and return the extracted saved file
  private File extractEmbeddedFile(final InputStream pdfInputStream, final String name) throws IOException {
    PDDocument docWithEmbeddedFile;
    docWithEmbeddedFile = PDDocument.load(pdfInputStream);
    final PDDocumentCatalog catalog = docWithEmbeddedFile.getDocumentCatalog();
    final PDDocumentNameDictionary names = catalog.getNames();
    final PDEmbeddedFilesNameTreeNode embeddedFiles = names.getEmbeddedFiles();
    final Map<String, PDComplexFileSpecification> embeddedFileNames = embeddedFiles.getNames();
    Assert.assertEquals(1, embeddedFileNames.size());
    final Map.Entry<String, PDComplexFileSpecification> entry = embeddedFileNames.entrySet().iterator().next();
    TestSymmetricKeyEncryption.LOG.info("Processing embedded file " + entry.getKey() + ":");
    final PDComplexFileSpecification complexFileSpec = entry.getValue();
    final PDEmbeddedFile embeddedFile = complexFileSpec.getEmbeddedFile();

    final File resultFile = new File(testResultsDir, name);
    final FileOutputStream fos = new FileOutputStream(resultFile);
    final InputStream is = embeddedFile.createInputStream();
    IOUtils.copy(is, fos);
    fos.close();
    is.close();

    TestSymmetricKeyEncryption.LOG.info("  size: " + embeddedFile.getSize());
    TestCase.assertEquals(embeddedFile.getSize(), resultFile.length());

    return resultFile;
  }

  private void testSymmEncrForKeySizeInner(final int keyLength, final boolean preferAES, final int sizePriorToEncr,
      final byte[] inputFileWithEmbeddedFileAsByteArray, final File embeddedFilePriorToEncryption,
      final String userpassword, final String ownerpassword) throws IOException {
    final PDDocument document = PDDocument.load(inputFileWithEmbeddedFileAsByteArray);
    final PDDocument encryptedDoc = encrypt(keyLength, preferAES, sizePriorToEncr, document, "ContainsEmbedded-",
        permission, userpassword, ownerpassword);

    final File decryptedFile = new File(testResultsDir,
        "DecryptedContainsEmbedded-" + keyLength + "-bit-" + (preferAES ? "AES" : "RC4") + ".pdf");
    encryptedDoc.setAllSecurityToBeRemoved(true);
    encryptedDoc.save(decryptedFile);

    final File extractedEmbeddedFile = extractEmbeddedFile(new FileInputStream(decryptedFile),
        "decryptedInnerFile-" + keyLength + "-bit-" + (preferAES ? "AES" : "RC4") + ".pdf");

    Assert.assertEquals(
        keyLength + "-bit " + (preferAES ? "AES" : "RC4")
            + " decrypted inner attachment pdf should have same size as plain one",
        embeddedFilePriorToEncryption.length(), extractedEmbeddedFile.length());

    // compare the two embedded files
    Assert.assertArrayEquals(getFileAsByteArray(embeddedFilePriorToEncryption),
        getFileAsByteArray(extractedEmbeddedFile));
    encryptedDoc.close();
  }

  private byte[] getFileResourceAsByteArray(final String testFileName) throws IOException {
    return IOUtils.toByteArray(TestSymmetricKeyEncryption.class.getResourceAsStream(testFileName));
  }

  private byte[] getFileAsByteArray(final File f) throws IOException {
    return Files.readAllBytes(f.toPath());
  }
}
