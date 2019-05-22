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
package org.apache.pdfbox.io;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * This is a unit test for RandomAccessOutputStream.
 *
 * @author Fredrik Kjellberg
 */
public class TestRandomAccessOutputStream extends TestCase {
  private final File testResultsDir = new File("target/test-output");

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    testResultsDir.mkdirs();
  }

  public void testWrite() throws IOException {
    RandomAccessOutputStream out;
    byte[] buffer;

    final File file = new File(testResultsDir, "raf-outputstream.bin");

    file.delete();

    final RandomAccessFile raFile = new RandomAccessFile(file, "rw");

    // Test single byte writes
    buffer = createDataSequence(16, 10);
    out = new RandomAccessOutputStream(raFile);
    for (final byte b : buffer) {
      out.write(b);
    }
    TestCase.assertEquals(16, raFile.length());
    TestCase.assertEquals(16, raFile.getPosition());
    out.close();

    // Test no write
    out = new RandomAccessOutputStream(raFile);
    TestCase.assertEquals(16, raFile.length());
    TestCase.assertEquals(16, raFile.getPosition());
    out.close();

    // Test buffer writes
    buffer = createDataSequence(8, 30);
    out = new RandomAccessOutputStream(raFile);
    out.write(buffer);
    TestCase.assertEquals(24, raFile.length());
    TestCase.assertEquals(24, raFile.getPosition());
    out.close();

    // Test partial buffer writes
    buffer = createDataSequence(16, 50);
    out = new RandomAccessOutputStream(raFile);
    out.write(buffer, 8, 4);
    out.write(buffer, 4, 2);
    TestCase.assertEquals(30, raFile.length());
    TestCase.assertEquals(30, raFile.getPosition());
    out.close();

    // Verify written data
    buffer = new byte[(int) raFile.length()];
    raFile.seek(0);
    TestCase.assertEquals(buffer.length, raFile.read(buffer, 0, buffer.length));
    TestCase.assertEquals(10, buffer[0]);
    TestCase.assertEquals(11, buffer[1]);
    TestCase.assertEquals(25, buffer[15]);

    TestCase.assertEquals(30, buffer[16]);
    TestCase.assertEquals(31, buffer[17]);
    TestCase.assertEquals(37, buffer[23]);

    TestCase.assertEquals(58, buffer[24]);
    TestCase.assertEquals(59, buffer[25]);
    TestCase.assertEquals(60, buffer[26]);
    TestCase.assertEquals(61, buffer[27]);
    TestCase.assertEquals(54, buffer[28]);
    TestCase.assertEquals(55, buffer[29]);

    // Cleanup
    raFile.close();
    file.delete();
  }

  protected byte[] createDataSequence(final int length, final int firstByteValue) {
    final byte[] buffer = new byte[length];
    for (int i = 0; i < buffer.length; i++) {
      buffer[i] = (byte) (firstByteValue + i);
    }

    return buffer;
  }
}
