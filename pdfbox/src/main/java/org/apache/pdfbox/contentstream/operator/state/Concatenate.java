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
package org.apache.pdfbox.contentstream.operator.state;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.contentstream.operator.MissingOperandException;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.contentstream.operator.OperatorProcessor;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.util.Matrix;

/**
 * cm: Concatenate matrix to current transformation matrix.
 *
 * @author Laurent Huault
 */
public class Concatenate extends OperatorProcessor {
  @Override
  public void process(final Operator operator, final List<COSBase> arguments) throws IOException {
    if (arguments.size() < 6)
      throw new MissingOperandException(operator, arguments);
    if (!checkArrayTypesClass(arguments, COSNumber.class))
      return;

    // concatenate matrix to current transformation matrix
    final COSNumber a = (COSNumber) arguments.get(0);
    final COSNumber b = (COSNumber) arguments.get(1);
    final COSNumber c = (COSNumber) arguments.get(2);
    final COSNumber d = (COSNumber) arguments.get(3);
    final COSNumber e = (COSNumber) arguments.get(4);
    final COSNumber f = (COSNumber) arguments.get(5);

    final Matrix matrix = new Matrix(a.floatValue(), b.floatValue(), c.floatValue(), d.floatValue(), e.floatValue(),
        f.floatValue());

    context.getGraphicsState().getCurrentTransformationMatrix().concatenate(matrix);
  }

  @Override
  public String getName() {
    return OperatorName.CONCAT;
  }
}
