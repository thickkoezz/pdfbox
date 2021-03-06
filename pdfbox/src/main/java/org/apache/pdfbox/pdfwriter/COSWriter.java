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
package org.apache.pdfbox.pdfwriter;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.cos.COSUpdateInfo;
import org.apache.pdfbox.cos.ICOSVisitor;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessInputStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFXRefStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.SecurityHandler;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.COSFilterInputStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.util.Charsets;
import org.apache.pdfbox.util.Hex;

/**
 * This class acts on a in-memory representation of a PDF document.
 *
 * @author Michael Traut
 * @author Ben Litchfield
 */
public class COSWriter implements ICOSVisitor, Closeable {
  /**
   * The dictionary open token.
   */
  public static final byte[] DICT_OPEN = "<<".getBytes(Charsets.US_ASCII);
  /**
   * The dictionary close token.
   */
  public static final byte[] DICT_CLOSE = ">>".getBytes(Charsets.US_ASCII);
  /**
   * space character.
   */
  public static final byte[] SPACE = { ' ' };
  /**
   * The start to a PDF comment.
   */
  public static final byte[] COMMENT = { '%' };

  /**
   * The output version of the PDF.
   */
  public static final byte[] VERSION = "PDF-1.4".getBytes(Charsets.US_ASCII);
  /**
   * Garbage bytes used to create the PDF header.
   */
  public static final byte[] GARBAGE = new byte[] { (byte) 0xf6, (byte) 0xe4, (byte) 0xfc, (byte) 0xdf };
  /**
   * The EOF constant.
   */
  public static final byte[] EOF = "%%EOF".getBytes(Charsets.US_ASCII);
  // pdf tokens

  /**
   * The reference token.
   */
  public static final byte[] REFERENCE = "R".getBytes(Charsets.US_ASCII);
  /**
   * The XREF token.
   */
  public static final byte[] XREF = "xref".getBytes(Charsets.US_ASCII);
  /**
   * The xref free token.
   */
  public static final byte[] XREF_FREE = "f".getBytes(Charsets.US_ASCII);
  /**
   * The xref used token.
   */
  public static final byte[] XREF_USED = "n".getBytes(Charsets.US_ASCII);
  /**
   * The trailer token.
   */
  public static final byte[] TRAILER = "trailer".getBytes(Charsets.US_ASCII);
  /**
   * The start xref token.
   */
  public static final byte[] STARTXREF = "startxref".getBytes(Charsets.US_ASCII);
  /**
   * The starting object token.
   */
  public static final byte[] OBJ = "obj".getBytes(Charsets.US_ASCII);
  /**
   * The end object token.
   */
  public static final byte[] ENDOBJ = "endobj".getBytes(Charsets.US_ASCII);
  /**
   * The array open token.
   */
  public static final byte[] ARRAY_OPEN = "[".getBytes(Charsets.US_ASCII);
  /**
   * The array close token.
   */
  public static final byte[] ARRAY_CLOSE = "]".getBytes(Charsets.US_ASCII);
  /**
   * The open stream token.
   */
  public static final byte[] STREAM = "stream".getBytes(Charsets.US_ASCII);
  /**
   * The close stream token.
   */
  public static final byte[] ENDSTREAM = "endstream".getBytes(Charsets.US_ASCII);

  private final NumberFormat formatXrefOffset = new DecimalFormat("0000000000",
      DecimalFormatSymbols.getInstance(Locale.US));

  // the decimal format for the xref object generation number data
  private final NumberFormat formatXrefGeneration = new DecimalFormat("00000",
      DecimalFormatSymbols.getInstance(Locale.US));

  // the stream where we create the pdf output
  private OutputStream output;

  // the stream used to write standard cos data
  private COSStandardOutputStream standardOutput;

  // the start position of the x ref section
  private long startxref = 0;

  // the current object number
  private long number = 0;

  // maps the object to the keys generated in the writer
  // these are used for indirect references in other objects
  // A hashtable is used on purpose over a hashmap
  // so that null entries will not get added.
  private final Map<COSBase, COSObjectKey> objectKeys = new Hashtable<>();
  private final Map<COSObjectKey, COSBase> keyObject = new Hashtable<>();

  // the list of x ref entries to be made so far
  private final List<COSWriterXRefEntry> xRefEntries = new ArrayList<>();
  private final Set<COSBase> objectsToWriteSet = new HashSet<>();

  // A list of objects to write.
  private final Deque<COSBase> objectsToWrite = new LinkedList<>();

  // a list of objects already written
  private final Set<COSBase> writtenObjects = new HashSet<>();

  // An 'actual' is any COSBase that is not a COSObject.
  // need to keep a list of the actuals that are added
  // as well as the objects because there is a problem
  // when adding a COSObject and then later adding
  // the actual for that object, so we will track
  // actuals separately.
  private final Set<COSBase> actualsAdded = new HashSet<>();

  private COSObjectKey currentObjectKey = null;
  private PDDocument pdDocument = null;
  private FDFDocument fdfDocument = null;
  private boolean willEncrypt = false;

  // signing
  private boolean incrementalUpdate = false;
  private boolean reachedSignature = false;
  private long signatureOffset, signatureLength;
  private long byteRangeOffset, byteRangeLength;
  private RandomAccessRead incrementalInput;
  private OutputStream incrementalOutput;
  private SignatureInterface signatureInterface;
  private byte[] incrementPart;
  private COSArray byteRangeArray;

  /**
   * COSWriter constructor.
   *
   * @param outputStream The output stream to write the PDF. It will be closed
   *                     when this object is closed.
   */
  public COSWriter(final OutputStream outputStream) {
    setOutput(outputStream);
    setStandardOutput(new COSStandardOutputStream(output));
  }

  /**
   * COSWriter constructor for incremental updates. There must be a path of
   * objects that have {@link COSUpdateInfo#isNeedToBeUpdated()} set, starting
   * from the document catalog. For signatures this is taken care by PDFBox
   * itself.
   *
   * @param outputStream output stream where the new PDF data will be written. It
   *                     will be closed when this object is closed.
   * @param inputData    random access read containing source PDF data
   *
   * @throws IOException if something went wrong
   */
  public COSWriter(final OutputStream outputStream, final RandomAccessRead inputData) throws IOException {
    // write to buffer instead of output
    setOutput(new ByteArrayOutputStream());
    setStandardOutput(new COSStandardOutputStream(output, inputData.length()));

    incrementalInput = inputData;
    incrementalOutput = outputStream;
    incrementalUpdate = true;
  }

  private void prepareIncrement(final PDDocument doc) {
    if (doc != null) {
      final COSDocument cosDoc = doc.getDocument();

      final Map<COSObjectKey, Long> xrefTable = cosDoc.getXrefTable();
      final Set<COSObjectKey> keySet = xrefTable.keySet();
      long highestNumber = doc.getDocument().getHighestXRefObjectNumber();
      for (final COSObjectKey cosObjectKey : keySet) {
        final COSBase object = cosDoc.getObjectFromPool(cosObjectKey).getObject();
        if (object != null && cosObjectKey != null && !(object instanceof COSNumber)) {
          objectKeys.put(object, cosObjectKey);
          keyObject.put(cosObjectKey, object);
        }

        if (cosObjectKey != null) {
          final long num = cosObjectKey.getNumber();
          if (num > highestNumber) {
            highestNumber = num;
          }
        }
      }
      setNumber(highestNumber);
    }
  }

  /**
   * add an entry in the x ref table for later dump.
   *
   * @param entry The new entry to add.
   */
  protected void addXRefEntry(final COSWriterXRefEntry entry) {
    getXRefEntries().add(entry);
  }

  /**
   * This will close the stream.
   *
   * @throws IOException If the underlying stream throws an exception.
   */
  @Override
  public void close() throws IOException {
    if (getStandardOutput() != null) {
      getStandardOutput().close();
    }
    if (incrementalOutput != null) {
      incrementalOutput.close();
    }
  }

  /**
   * This will get the current object number.
   *
   * @return The current object number.
   */
  protected long getNumber() {
    return number;
  }

  /**
   * This will get all available object keys.
   *
   * @return A map of all object keys.
   */
  public Map<COSBase, COSObjectKey> getObjectKeys() {
    return objectKeys;
  }

  /**
   * This will get the output stream.
   *
   * @return The output stream.
   */
  protected java.io.OutputStream getOutput() {
    return output;
  }

  /**
   * This will get the standard output stream.
   *
   * @return The standard output stream.
   */
  protected COSStandardOutputStream getStandardOutput() {
    return standardOutput;
  }

  /**
   * This will get the current start xref.
   *
   * @return The current start xref.
   */
  protected long getStartxref() {
    return startxref;
  }

  /**
   * This will get the xref entries.
   *
   * @return All available xref entries.
   */
  protected List<COSWriterXRefEntry> getXRefEntries() {
    return xRefEntries;
  }

  /**
   * This will set the current object number.
   *
   * @param newNumber The new object number.
   */
  protected void setNumber(final long newNumber) {
    number = newNumber;
  }

  /**
   * This will set the output stream.
   *
   * @param newOutput The new output stream.
   */
  private void setOutput(final OutputStream newOutput) {
    output = newOutput;
  }

  /**
   * This will set the standard output stream.
   *
   * @param newStandardOutput The new standard output stream.
   */
  private void setStandardOutput(final COSStandardOutputStream newStandardOutput) {
    standardOutput = newStandardOutput;
  }

  /**
   * This will set the start xref.
   *
   * @param newStartxref The new start xref attribute.
   */
  protected void setStartxref(final long newStartxref) {
    startxref = newStartxref;
  }

  /**
   * This will write the body of the document.
   *
   * @param doc The document to write the body for.
   *
   * @throws IOException If there is an error writing the data.
   */
  protected void doWriteBody(final COSDocument doc) throws IOException {
    final COSDictionary trailer = doc.getTrailer();
    final COSDictionary root = trailer.getCOSDictionary(COSName.ROOT);
    final COSDictionary info = trailer.getCOSDictionary(COSName.INFO);
    final COSDictionary encrypt = trailer.getCOSDictionary(COSName.ENCRYPT);
    if (root != null) {
      addObjectToWrite(root);
    }
    if (info != null) {
      addObjectToWrite(info);
    }

    doWriteObjects();
    willEncrypt = false;
    if (encrypt != null) {
      addObjectToWrite(encrypt);
    }

    doWriteObjects();
  }

  private void doWriteObjects() throws IOException {
    while (objectsToWrite.size() > 0) {
      final COSBase nextObject = objectsToWrite.removeFirst();
      objectsToWriteSet.remove(nextObject);
      doWriteObject(nextObject);
    }
  }

  private void addObjectToWrite(final COSBase object) {
    COSBase actual = object;
    if (actual instanceof COSObject) {
      actual = ((COSObject) actual).getObject();
    }

    if (!writtenObjects.contains(object) && !objectsToWriteSet.contains(object) && !actualsAdded.contains(actual)) {
      COSBase cosBase = null;
      COSObjectKey cosObjectKey = null;
      if (actual != null) {
        cosObjectKey = objectKeys.get(actual);
      }
      if (cosObjectKey != null) {
        cosBase = keyObject.get(cosObjectKey);
      }
      if (actual != null && objectKeys.containsKey(actual) && object instanceof COSUpdateInfo
          && !((COSUpdateInfo) object).isNeedToBeUpdated() && cosBase instanceof COSUpdateInfo
          && !((COSUpdateInfo) cosBase).isNeedToBeUpdated())
        return;
      objectsToWrite.add(object);
      objectsToWriteSet.add(object);
      if (actual != null) {
        actualsAdded.add(actual);
      }
    }
  }

  /**
   * This will write a COS object.
   *
   * @param obj The object to write.
   *
   * @throws IOException if the output cannot be written
   */
  public void doWriteObject(final COSBase obj) throws IOException {
    writtenObjects.add(obj);
    // find the physical reference
    currentObjectKey = getObjectKey(obj);
    // add a x ref entry
    addXRefEntry(new COSWriterXRefEntry(getStandardOutput().getPos(), obj, currentObjectKey));
    // write the object
    getStandardOutput().write(String.valueOf(currentObjectKey.getNumber()).getBytes(Charsets.ISO_8859_1));
    getStandardOutput().write(COSWriter.SPACE);
    getStandardOutput().write(String.valueOf(currentObjectKey.getGeneration()).getBytes(Charsets.ISO_8859_1));
    getStandardOutput().write(COSWriter.SPACE);
    getStandardOutput().write(COSWriter.OBJ);
    getStandardOutput().writeEOL();
    // null test added to please Sonar
    // TODO: shouldn't all public methods be guarded against passing null. Passing
    // null to most methods will
    // fail with an NPE
    if (obj != null) {
      obj.accept(this);
    }
    getStandardOutput().writeEOL();
    getStandardOutput().write(COSWriter.ENDOBJ);
    getStandardOutput().writeEOL();
  }

  /**
   * This will write the header to the PDF document.
   *
   * @param doc The document to get the data from.
   *
   * @throws IOException If there is an error writing to the stream.
   */
  protected void doWriteHeader(final COSDocument doc) throws IOException {
    String headerString;
    if (fdfDocument != null) {
      headerString = "%FDF-" + Float.toString(doc.getVersion());
    } else {
      headerString = "%PDF-" + Float.toString(doc.getVersion());
    }
    getStandardOutput().write(headerString.getBytes(Charsets.ISO_8859_1));

    getStandardOutput().writeEOL();
    getStandardOutput().write(COSWriter.COMMENT);
    getStandardOutput().write(COSWriter.GARBAGE);
    getStandardOutput().writeEOL();
  }

  /**
   * This will write the trailer to the PDF document.
   *
   * @param doc The document to create the trailer for.
   *
   * @throws IOException If there is an IOError while writing the document.
   */
  protected void doWriteTrailer(final COSDocument doc) throws IOException {
    getStandardOutput().write(COSWriter.TRAILER);
    getStandardOutput().writeEOL();

    final COSDictionary trailer = doc.getTrailer();
    // sort xref, needed only if object keys not regenerated
    Collections.sort(getXRefEntries());
    final COSWriterXRefEntry lastEntry = getXRefEntries().get(getXRefEntries().size() - 1);
    trailer.setLong(COSName.SIZE, lastEntry.getKey().getNumber() + 1);
    // Only need to stay, if an incremental update will be performed
    if (!incrementalUpdate) {
      trailer.removeItem(COSName.PREV);
    }
    if (!doc.isXRefStream()) {
      trailer.removeItem(COSName.XREF_STM);
    }
    // Remove a checksum if present
    trailer.removeItem(COSName.DOC_CHECKSUM);

    final COSArray idArray = trailer.getCOSArray(COSName.ID);
    if (idArray != null) {
      idArray.setDirect(true);
    }

    trailer.accept(this);
  }

  private void doWriteXRefInc(final COSDocument doc, final long hybridPrev) throws IOException {
    if (doc.isXRefStream() || hybridPrev != -1) {
      // the file uses XrefStreams, so we need to update
      // it with an xref stream. We create a new one and fill it
      // with data available here

      // create a new XRefStrema object
      final PDFXRefStream pdfxRefStream = new PDFXRefStream(doc);

      // add all entries from the incremental update.
      final List<COSWriterXRefEntry> xRefEntries2 = getXRefEntries();
      for (final COSWriterXRefEntry cosWriterXRefEntry : xRefEntries2) {
        pdfxRefStream.addEntry(cosWriterXRefEntry);
      }

      final COSDictionary trailer = doc.getTrailer();
      if (incrementalUpdate) {
        // use previous startXref value as new PREV value
        trailer.setLong(COSName.PREV, doc.getStartXref());
      } else {
        trailer.removeItem(COSName.PREV);
      }
      pdfxRefStream.addTrailerInfo(trailer);
      // the size is the highest object number+1. we add one more
      // for the xref stream object we are going to write
      pdfxRefStream.setSize(getNumber() + 2);

      setStartxref(getStandardOutput().getPos());
      final COSStream stream2 = pdfxRefStream.getStream();
      doWriteObject(stream2);
    }

    if (!doc.isXRefStream() || hybridPrev != -1) {
      final COSDictionary trailer = doc.getTrailer();
      trailer.setLong(COSName.PREV, doc.getStartXref());
      if (hybridPrev != -1) {
        final COSName xrefStm = COSName.XREF_STM;
        trailer.removeItem(xrefStm);
        trailer.setLong(xrefStm, getStartxref());
      }
      doWriteXRefTable();
      doWriteTrailer(doc);
    }
  }

  // writes the "xref" table
  private void doWriteXRefTable() throws IOException {
    addXRefEntry(COSWriterXRefEntry.getNullEntry());

    // sort xref, needed only if object keys not regenerated
    Collections.sort(getXRefEntries());

    // remember the position where x ref was written
    setStartxref(getStandardOutput().getPos());

    getStandardOutput().write(COSWriter.XREF);
    getStandardOutput().writeEOL();
    // write start object number and object count for this x ref section
    // we assume starting from scratch

    final Long[] xRefRanges = getXRefRanges(getXRefEntries());
    final int xRefLength = xRefRanges.length;
    int x = 0;
    int j = 0;
    while (x < xRefLength && xRefLength % 2 == 0) {
      writeXrefRange(xRefRanges[x], xRefRanges[x + 1]);

      for (int i = 0; i < xRefRanges[x + 1]; ++i) {
        writeXrefEntry(xRefEntries.get(j++));
      }
      x += 2;
    }
  }

  /**
   * Write an incremental update for a non signature case. This can be used for
   * e.g. augmenting signatures.
   *
   * @throws IOException
   */
  private void doWriteIncrement() throws IOException {
    // write existing PDF
    IOUtils.copy(new RandomAccessInputStream(incrementalInput), incrementalOutput);
    // write the actual incremental update
    incrementalOutput.write(((ByteArrayOutputStream) output).toByteArray());
  }

  private void doWriteSignature() throws IOException {
    // calculate the ByteRange values
    final long inLength = incrementalInput.length();
    final long beforeLength = signatureOffset;
    final long afterOffset = signatureOffset + signatureLength;
    final long afterLength = getStandardOutput().getPos() - (inLength + signatureLength) - (signatureOffset - inLength);

    final String byteRange = "0 " + beforeLength + " " + afterOffset + " " + afterLength + "]";

    // Assign the values to the actual COSArray, so that the user can access it
    // before closing
    byteRangeArray.set(0, COSInteger.ZERO);
    byteRangeArray.set(1, COSInteger.get(beforeLength));
    byteRangeArray.set(2, COSInteger.get(afterOffset));
    byteRangeArray.set(3, COSInteger.get(afterLength));

    if (byteRange.length() > byteRangeLength)
      throw new IOException("Can't write new byteRange '" + byteRange + "' not enough space: byteRange.length(): "
          + byteRange.length() + ", byteRangeLength: " + byteRangeLength);

    // copy the new incremental data into a buffer (e.g. signature dict, trailer)
    final ByteArrayOutputStream byteOut = (ByteArrayOutputStream) output;
    byteOut.flush();
    incrementPart = byteOut.toByteArray();

    // overwrite the ByteRange in the buffer
    final byte[] byteRangeBytes = byteRange.getBytes(Charsets.ISO_8859_1);
    for (int i = 0; i < byteRangeLength; i++) {
      if (i >= byteRangeBytes.length) {
        incrementPart[(int) (byteRangeOffset + i - inLength)] = 0x20; // SPACE
      } else {
        incrementPart[(int) (byteRangeOffset + i - inLength)] = byteRangeBytes[i];
      }
    }

    if (signatureInterface != null) {
      // data to be signed
      final InputStream dataToSign = getDataToSign();

      // sign the bytes
      final byte[] signatureBytes = signatureInterface.sign(dataToSign);
      writeExternalSignature(signatureBytes);
    }
    // else signature should created externally and set via writeSignature()
  }

  /**
   * Return the stream of PDF data to be signed. Clients should use this method
   * only to create signatures externally. {@link #write(PDDocument)} method
   * should have been called prior. The created signature should be set using
   * {@link #writeExternalSignature(byte[])}.
   * <p>
   * When {@link SignatureInterface} instance is used, COSWriter obtains and
   * writes the signature itself.
   * </p>
   *
   * @return data stream to be signed
   * @throws IllegalStateException if PDF is not prepared for external signing
   * @throws IOException           if input data is closed
   */
  public InputStream getDataToSign() throws IOException {
    if (incrementPart == null || incrementalInput == null)
      throw new IllegalStateException("PDF not prepared for signing");
    // range of incremental bytes to be signed (includes /ByteRange but not
    // /Contents)
    final int incPartSigOffset = (int) (signatureOffset - incrementalInput.length());
    final int afterSigOffset = incPartSigOffset + (int) signatureLength;
    final int[] range = { 0, incPartSigOffset, afterSigOffset, incrementPart.length - afterSigOffset };

    return new SequenceInputStream(new RandomAccessInputStream(incrementalInput),
        new COSFilterInputStream(incrementPart, range));
  }

  /**
   * Write externally created signature of PDF data obtained via
   * {@link #getDataToSign()} method.
   *
   * @param cmsSignature CMS signature byte array
   * @throws IllegalStateException if PDF is not prepared for external signing
   * @throws IOException           if source data stream is closed
   */
  public void writeExternalSignature(final byte[] cmsSignature) throws IOException {

    if (incrementPart == null || incrementalInput == null)
      throw new IllegalStateException("PDF not prepared for setting signature");
    final byte[] signatureBytes = Hex.getBytes(cmsSignature);

    // substract 2 bytes because of the enclosing "<>"
    if (signatureBytes.length > signatureLength - 2)
      throw new IOException("Can't write signature, not enough space");

    // overwrite the signature Contents in the buffer
    final int incPartSigOffset = (int) (signatureOffset - incrementalInput.length());
    System.arraycopy(signatureBytes, 0, incrementPart, incPartSigOffset + 1, signatureBytes.length);

    // write the data to the incremental output stream
    IOUtils.copy(new RandomAccessInputStream(incrementalInput), incrementalOutput);
    incrementalOutput.write(incrementPart);

    // prevent further use
    incrementPart = null;
  }

  private void writeXrefRange(final long x, final long y) throws IOException {
    getStandardOutput().write(String.valueOf(x).getBytes(Charsets.ISO_8859_1));
    getStandardOutput().write(COSWriter.SPACE);
    getStandardOutput().write(String.valueOf(y).getBytes(Charsets.ISO_8859_1));
    getStandardOutput().writeEOL();
  }

  private void writeXrefEntry(final COSWriterXRefEntry entry) throws IOException {
    final String offset = formatXrefOffset.format(entry.getOffset());
    final String generation = formatXrefGeneration.format(entry.getKey().getGeneration());
    getStandardOutput().write(offset.getBytes(Charsets.ISO_8859_1));
    getStandardOutput().write(COSWriter.SPACE);
    getStandardOutput().write(generation.getBytes(Charsets.ISO_8859_1));
    getStandardOutput().write(COSWriter.SPACE);
    getStandardOutput().write(entry.isFree() ? COSWriter.XREF_FREE : COSWriter.XREF_USED);
    getStandardOutput().writeCRLF();
  }

  /**
   * check the xref entries and write out the ranges. The format of the returned
   * array is exactly the same as the pdf specification. See section 7.5.4 of
   * ISO32000-1:2008, example 1 (page 40) for reference.
   * <p>
   * example: 0 1 2 5 6 7 8 10
   * <p>
   * will create a array with follow ranges
   * <p>
   * 0 3 5 4 10 1
   * <p>
   * this mean that the element 0 is followed by two other related numbers that
   * represent a cluster of the size 3. 5 is follow by three other related numbers
   * and create a cluster of size 4. etc.
   *
   * @param xRefEntriesList list with the xRef entries that was written
   * @return a integer array with the ranges
   */
  protected Long[] getXRefRanges(final List<COSWriterXRefEntry> xRefEntriesList) {
    long last = -2;
    long count = 1;

    final List<Long> list = new ArrayList<>();
    for (final Object object : xRefEntriesList) {
      final long nr = (int) ((COSWriterXRefEntry) object).getKey().getNumber();
      if (nr == last + 1) {
        ++count;
        last = nr;
      } else if (last == -2) {
        last = nr;
      } else {
        list.add(last - count + 1);
        list.add(count);
        last = nr;
        count = 1;
      }
    }
    // If no new entry is found, we need to write out the last result
    if (xRefEntriesList.size() > 0) {
      list.add(last - count + 1);
      list.add(count);
    }
    return list.toArray(new Long[list.size()]);
  }

  /**
   * This will get the object key for the object.
   *
   * @param obj The object to get the key for.
   *
   * @return The object key for the object.
   */
  private COSObjectKey getObjectKey(final COSBase obj) {
    COSBase actual = obj;
    if (actual instanceof COSObject) {
      actual = ((COSObject) obj).getObject();
    }
    // PDFBOX-4540: because objectKeys is accessible from outside, it is possible
    // that a COSObject obj is already in the objectKeys map.
    COSObjectKey key = objectKeys.get(obj);
    if (key == null && actual != null) {
      key = objectKeys.get(actual);
    }
    if (key == null) {
      setNumber(getNumber() + 1);
      key = new COSObjectKey(getNumber(), 0);
      objectKeys.put(obj, key);
      if (actual != null) {
        objectKeys.put(actual, key);
      }
    }
    return key;
  }

  @Override
  public Object visitFromArray(final COSArray obj) throws IOException {
    int count = 0;
    getStandardOutput().write(COSWriter.ARRAY_OPEN);
    for (final Iterator<COSBase> i = obj.iterator(); i.hasNext();) {
      final COSBase current = i.next();
      if (current instanceof COSDictionary) {
        if (current.isDirect()) {
          visitFromDictionary((COSDictionary) current);
        } else {
          addObjectToWrite(current);
          writeReference(current);
        }
      } else if (current instanceof COSObject) {
        final COSBase subValue = ((COSObject) current).getObject();
        if (willEncrypt || incrementalUpdate || subValue instanceof COSDictionary || subValue == null) {
          // PDFBOX-4308: added willEncrypt to prevent an object
          // that is referenced several times from being written
          // direct and indirect, thus getting encrypted
          // with wrong object number or getting encrypted twice
          addObjectToWrite(current);
          writeReference(current);
        } else {
          subValue.accept(this);
        }
      } else if (current == null) {
        COSNull.NULL.accept(this);
      } else {
        current.accept(this);
      }
      count++;
      if (i.hasNext()) {
        if (count % 10 == 0) {
          getStandardOutput().writeEOL();
        } else {
          getStandardOutput().write(COSWriter.SPACE);
        }
      }
    }
    getStandardOutput().write(COSWriter.ARRAY_CLOSE);
    getStandardOutput().writeEOL();
    return null;
  }

  @Override
  public Object visitFromBoolean(final COSBoolean obj) throws IOException {
    obj.writePDF(getStandardOutput());
    return null;
  }

  @Override
  public Object visitFromDictionary(final COSDictionary obj) throws IOException {
    if (!reachedSignature) {
      final COSBase itemType = obj.getItem(COSName.TYPE);
      if (COSName.SIG.equals(itemType) || COSName.DOC_TIME_STAMP.equals(itemType)) {
        reachedSignature = true;
      }
    }
    getStandardOutput().write(COSWriter.DICT_OPEN);
    getStandardOutput().writeEOL();
    for (final Map.Entry<COSName, COSBase> entry : obj.entrySet()) {
      final COSBase value = entry.getValue();
      if (value != null) {
        entry.getKey().accept(this);
        getStandardOutput().write(COSWriter.SPACE);
        if (value instanceof COSDictionary) {
          final COSDictionary dict = (COSDictionary) value;

          if (!incrementalUpdate) {
            // write all XObjects as direct objects, this will save some size
            // PDFBOX-3684: but avoid dictionary that references itself
            COSBase item = dict.getItem(COSName.XOBJECT);
            if (item != null && !COSName.XOBJECT.equals(entry.getKey())) {
              item.setDirect(true);
            }
            item = dict.getItem(COSName.RESOURCES);
            if (item != null && !COSName.RESOURCES.equals(entry.getKey())) {
              item.setDirect(true);
            }
          }

          if (dict.isDirect()) {
            // If the object should be written direct, we need
            // to pass the dictionary to the visitor again.
            visitFromDictionary(dict);
          } else {
            addObjectToWrite(dict);
            writeReference(dict);
          }
        } else if (value instanceof COSObject) {
          final COSBase subValue = ((COSObject) value).getObject();
          if (willEncrypt || incrementalUpdate || subValue instanceof COSDictionary || subValue == null) {
            // PDFBOX-4308: added willEncrypt to prevent an object
            // that is referenced several times from being written
            // direct and indirect, thus getting encrypted
            // with wrong object number or getting encrypted twice
            addObjectToWrite(value);
            writeReference(value);
          } else {
            subValue.accept(this);
          }
        } else {
          // If we reach the pdf signature, we need to determinate the position of the
          // content and byterange
          if (reachedSignature && COSName.CONTENTS.equals(entry.getKey())) {
            signatureOffset = getStandardOutput().getPos();
            value.accept(this);
            signatureLength = getStandardOutput().getPos() - signatureOffset;
          } else if (reachedSignature && COSName.BYTERANGE.equals(entry.getKey())) {
            byteRangeArray = (COSArray) entry.getValue();
            byteRangeOffset = getStandardOutput().getPos() + 1;
            value.accept(this);
            byteRangeLength = getStandardOutput().getPos() - 1 - byteRangeOffset;
            reachedSignature = false;
          } else {
            value.accept(this);
          }
        }
        getStandardOutput().writeEOL();

      } else {
        // then we won't write anything, there are a couple cases
        // were the value of an entry in the COSDictionary will
        // be a dangling reference that points to nothing
        // so we will just not write out the entry if that is the case
      }
    }
    getStandardOutput().write(COSWriter.DICT_CLOSE);
    getStandardOutput().writeEOL();
    return null;
  }

  @Override
  public Object visitFromDocument(final COSDocument doc) throws IOException {
    if (!incrementalUpdate) {
      doWriteHeader(doc);
    } else {
      // Sometimes the original file will be missing a newline at the end
      // In order to avoid having %%EOF the first object on the same line
      // as the %%EOF, we put a newline here. If there's already one at
      // the end of the file, an extra one won't hurt. PDFBOX-1051
      getStandardOutput().writeCRLF();
    }

    doWriteBody(doc);

    // get the previous trailer
    final COSDictionary trailer = doc.getTrailer();
    long hybridPrev = -1;

    if (trailer != null) {
      hybridPrev = trailer.getLong(COSName.XREF_STM);
    }

    if (incrementalUpdate || doc.isXRefStream()) {
      doWriteXRefInc(doc, hybridPrev);
    } else {
      doWriteXRefTable();
      doWriteTrailer(doc);
    }

    // write endof
    getStandardOutput().write(COSWriter.STARTXREF);
    getStandardOutput().writeEOL();
    getStandardOutput().write(String.valueOf(getStartxref()).getBytes(Charsets.ISO_8859_1));
    getStandardOutput().writeEOL();
    getStandardOutput().write(COSWriter.EOF);
    getStandardOutput().writeEOL();

    if (incrementalUpdate) {
      if (signatureOffset == 0 || byteRangeOffset == 0) {
        doWriteIncrement();
      } else {
        doWriteSignature();
      }
    }

    return null;
  }

  @Override
  public Object visitFromFloat(final COSFloat obj) throws IOException {
    obj.writePDF(getStandardOutput());
    return null;
  }

  @Override
  public Object visitFromInt(final COSInteger obj) throws IOException {
    obj.writePDF(getStandardOutput());
    return null;
  }

  @Override
  public Object visitFromName(final COSName obj) throws IOException {
    obj.writePDF(getStandardOutput());
    return null;
  }

  @Override
  public Object visitFromNull(final COSNull obj) throws IOException {
    obj.writePDF(getStandardOutput());
    return null;
  }

  /**
   * visitFromObjRef method comment.
   *
   * @param obj The object that is being visited.
   *
   * @throws IOException If there is an exception while visiting this object.
   */
  public void writeReference(final COSBase obj) throws IOException {
    final COSObjectKey key = getObjectKey(obj);
    getStandardOutput().write(String.valueOf(key.getNumber()).getBytes(Charsets.ISO_8859_1));
    getStandardOutput().write(COSWriter.SPACE);
    getStandardOutput().write(String.valueOf(key.getGeneration()).getBytes(Charsets.ISO_8859_1));
    getStandardOutput().write(COSWriter.SPACE);
    getStandardOutput().write(COSWriter.REFERENCE);
  }

  @Override
  public Object visitFromStream(final COSStream obj) throws IOException {
    if (willEncrypt) {
      pdDocument.getEncryption().getSecurityHandler().encryptStream(obj, currentObjectKey.getNumber(),
          currentObjectKey.getGeneration());
    }

    InputStream input = null;
    try {
      // write the stream content
      visitFromDictionary(obj);
      getStandardOutput().write(COSWriter.STREAM);
      getStandardOutput().writeCRLF();

      input = obj.createRawInputStream();
      IOUtils.copy(input, getStandardOutput());

      getStandardOutput().writeCRLF();
      getStandardOutput().write(COSWriter.ENDSTREAM);
      getStandardOutput().writeEOL();
      return null;
    } finally {
      if (input != null) {
        input.close();
      }
    }
  }

  @Override
  public Object visitFromString(final COSString obj) throws IOException {
    if (willEncrypt) {
      pdDocument.getEncryption().getSecurityHandler().encryptString(obj, currentObjectKey.getNumber(),
          currentObjectKey.getGeneration());
    }
    COSWriter.writeString(obj, getStandardOutput());
    return null;
  }

  /**
   * This will write the pdf document.
   *
   * @throws IOException If an error occurs while generating the data.
   * @param doc The document to write.
   */
  public void write(final COSDocument doc) throws IOException {
    final PDDocument pdDoc = new PDDocument(doc);
    write(pdDoc);
  }

  /**
   * This will write the pdf document. If signature should be created externally,
   * {@link #writeExternalSignature(byte[])} should be invoked to set signature
   * after calling this method.
   *
   * @param doc The document to write.
   *
   * @throws IOException If an error occurs while generating the data.
   */
  public void write(final PDDocument doc) throws IOException {
    write(doc, null);
  }

  /**
   * This will write the pdf document. If signature should be created externally,
   * {@link #writeExternalSignature(byte[])} should be invoked to set signature
   * after calling this method.
   *
   * @param doc           The document to write.
   * @param signInterface class to be used for signing; {@code null} if external
   *                      signing would be performed or there will be no signing
   *                      at all
   *
   * @throws IOException           If an error occurs while generating the data.
   * @throws IllegalStateException If the document has an encryption dictionary
   *                               but no protection policy.
   */
  public void write(final PDDocument doc, final SignatureInterface signInterface) throws IOException {
    final Long idTime = doc.getDocumentId() == null ? System.currentTimeMillis() : doc.getDocumentId();

    pdDocument = doc;
    signatureInterface = signInterface;

    if (incrementalUpdate) {
      prepareIncrement(doc);
    }

    // if the document says we should remove encryption, then we shouldn't encrypt
    if (doc.isAllSecurityToBeRemoved()) {
      willEncrypt = false;
      // also need to get rid of the "Encrypt" in the trailer so readers
      // don't try to decrypt a document which is not encrypted
      final COSDocument cosDoc = doc.getDocument();
      final COSDictionary trailer = cosDoc.getTrailer();
      trailer.removeItem(COSName.ENCRYPT);
    } else {
      if (pdDocument.getEncryption() != null) {
        if (!incrementalUpdate) {
          final SecurityHandler securityHandler = pdDocument.getEncryption().getSecurityHandler();
          if (!securityHandler.hasProtectionPolicy())
            throw new IllegalStateException("PDF contains an encryption dictionary, please remove it with "
                + "setAllSecurityToBeRemoved() or set a protection policy with protect()");
          securityHandler.prepareDocumentForEncryption(pdDocument);
        }
        willEncrypt = true;
      } else {
        willEncrypt = false;
      }
    }

    final COSDocument cosDoc = pdDocument.getDocument();
    final COSDictionary trailer = cosDoc.getTrailer();
    COSArray idArray;
    boolean missingID = true;
    final COSBase base = trailer.getDictionaryObject(COSName.ID);
    if (base instanceof COSArray) {
      idArray = (COSArray) base;
      if (idArray.size() == 2) {
        missingID = false;
      }
    } else {
      idArray = new COSArray();
    }
    if (missingID || incrementalUpdate) {
      MessageDigest md5;
      try {
        md5 = MessageDigest.getInstance("MD5");
      } catch (final NoSuchAlgorithmException e) {
        // should never happen
        throw new RuntimeException(e);
      }

      // algorithm says to use time/path/size/values in doc to generate the id.
      // we don't have path or size, so do the best we can
      md5.update(Long.toString(idTime).getBytes(Charsets.ISO_8859_1));

      final COSDictionary info = trailer.getCOSDictionary(COSName.INFO);
      if (info != null) {
        for (final COSBase cosBase : info.getValues()) {
          md5.update(cosBase.toString().getBytes(Charsets.ISO_8859_1));
        }
      }
      // reuse origin documentID if available as first value
      final COSString firstID = missingID ? new COSString(md5.digest()) : (COSString) idArray.get(0);
      // it's ok to use the same ID for the second part if the ID is created for the
      // first time
      final COSString secondID = missingID ? firstID : new COSString(md5.digest());
      idArray = new COSArray();
      idArray.add(firstID);
      idArray.add(secondID);
      trailer.setItem(COSName.ID, idArray);
    }
    cosDoc.accept(this);
  }

  /**
   * This will write the fdf document.
   *
   * @param doc The document to write.
   *
   * @throws IOException If an error occurs while generating the data.
   */
  public void write(final FDFDocument doc) throws IOException {
    fdfDocument = doc;
    willEncrypt = false;
    final COSDocument cosDoc = fdfDocument.getDocument();
    cosDoc.accept(this);
  }

  /**
   * This will output the given byte getString as a PDF object.
   *
   * @param string COSString to be written
   * @param output The stream to write to.
   * @throws IOException If there is an error writing to the stream.
   */
  public static void writeString(final COSString string, final OutputStream output) throws IOException {
    COSWriter.writeString(string.getBytes(), string.getForceHexForm(), output);
  }

  /**
   * This will output the given text/byte getString as a PDF object.
   *
   * @param bytes  byte array representation of a string to be written
   * @param output The stream to write to.
   * @throws IOException If there is an error writing to the stream.
   */
  public static void writeString(final byte[] bytes, final OutputStream output) throws IOException {
    COSWriter.writeString(bytes, false, output);
  }

  /**
   * This will output the given text/byte string as a PDF object.
   *
   * @param output The stream to write to.
   * @throws IOException If there is an error writing to the stream.
   */
  private static void writeString(final byte[] bytes, final boolean forceHex, final OutputStream output)
      throws IOException {
    // check for non-ASCII characters
    boolean isASCII = true;
    if (!forceHex) {
      for (final byte b : bytes) {
        // if the byte is negative then it is an eight bit byte and is outside the ASCII
        // range
        if (b < 0) {
          isASCII = false;
          break;
        }
        // PDFBOX-3107 EOL markers within a string are troublesome
        if (b == 0x0d || b == 0x0a) {
          isASCII = false;
          break;
        }
      }
    }

    if (isASCII && !forceHex) {
      // write ASCII string
      output.write('(');
      for (final byte b : bytes) {
        switch (b) {
        case '(':
        case ')':
        case '\\':
          output.write('\\');
          output.write(b);
          break;
        default:
          output.write(b);
          break;
        }
      }
      output.write(')');
    } else {
      // write hex string
      output.write('<');
      Hex.writeHexBytes(bytes, output);
      output.write('>');
    }
  }
}
