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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.contentstream.operator.MissingOperandException;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.contentstream.operator.OperatorProcessor;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

/**
 * gs: Set parameters from graphics state parameter dictionary.
 *
 * @author Ben Litchfield
 */
public class SetGraphicsStateParameters extends OperatorProcessor {
  private static final Log LOG = LogFactory.getLog(SetGraphicsStateParameters.class);

  @Override
  public void process(final Operator operator, final List<COSBase> arguments) throws IOException {
    if (arguments.isEmpty())
      throw new MissingOperandException(operator, arguments);
    final COSBase base0 = arguments.get(0);
    if (!(base0 instanceof COSName))
      return;

    // set parameters from graphics state parameter dictionary
    final COSName graphicsName = (COSName) base0;
    final PDExtendedGraphicsState gs = context.getResources().getExtGState(graphicsName);
    if (gs == null) {
      SetGraphicsStateParameters.LOG.error("name for 'gs' operator not found in resources: /" + graphicsName.getName());
      return;
    }
    gs.copyIntoGraphicsState(context.getGraphicsState());
  }

  @Override
  public String getName() {
    return OperatorName.SET_GRAPHICS_STATE_PARAMS;
  }
}
