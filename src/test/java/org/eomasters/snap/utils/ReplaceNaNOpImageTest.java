/*-
 * ========================LICENSE_START=================================
 * EOM Commons SNAP - Library of common utilities for ESA SNAP
 * -> https://www.eomasters.org/
 * ======================================================================
 * Copyright (C) 2023 - 2025 Marco Peters
 * ======================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * -> http://www.gnu.org/licenses/gpl-3.0.html
 * =========================LICENSE_END==================================
 */

package org.eomasters.snap.utils;

import static java.lang.Float.NaN;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.ImageUtils;
import org.junit.jupiter.api.Test;

class ReplaceNaNOpImageTest {

  private static final int W = 2;
  private static final int H = 3;

  @Test
  void testFloatReplacement() {
    float[] data = new float[]{
        1, 2,
        NaN, 4.0123456f,
        5, NaN
    };
    RenderedImage image = ImageUtils.createRenderedImage(W, H, ProductData.createInstance(data));
    ReplaceNaNOpImage replaceNaNOpImage = new ReplaceNaNOpImage(image, 42);
    assertEquals(DataBuffer.TYPE_FLOAT, replaceNaNOpImage.getData().getDataBuffer().getDataType());
    assertEquals(1, replaceNaNOpImage.getData().getSampleFloat(0, 0, 0));
    assertEquals(2, replaceNaNOpImage.getData().getSampleFloat(1, 0, 0));
    assertEquals(42, replaceNaNOpImage.getData().getSampleFloat(0, 1, 0));
    assertEquals(4.012345f, replaceNaNOpImage.getData().getSampleFloat(1, 1, 0), 1.0e-6);
    assertEquals(5, replaceNaNOpImage.getData().getSampleFloat(0, 2, 0));
    assertEquals(42, replaceNaNOpImage.getData().getSampleFloat(1, 2, 0));
  }

  @Test
  void testDoubleReplacement() {
    double[] data = new double[]{
        1, 2,
        NaN, 4.012345678,
        5, NaN
    };
    RenderedImage image = ImageUtils.createRenderedImage(W, H, ProductData.createInstance(data));
    ReplaceNaNOpImage replaceNaNOpImage = new ReplaceNaNOpImage(image, 42);
    assertEquals(DataBuffer.TYPE_DOUBLE, replaceNaNOpImage.getData().getDataBuffer().getDataType());
    assertEquals(1, replaceNaNOpImage.getData().getSampleDouble(0, 0, 0));
    assertEquals(2, replaceNaNOpImage.getData().getSampleDouble(1, 0, 0));
    assertEquals(42, replaceNaNOpImage.getData().getSampleDouble(0, 1, 0));
    assertEquals(4.01234567, replaceNaNOpImage.getData().getSampleDouble(1, 1, 0), 1.0e-8);
    assertEquals(5, replaceNaNOpImage.getData().getSampleDouble(0, 2, 0));
    assertEquals(42, replaceNaNOpImage.getData().getSampleDouble(1, 2, 0));
  }
}
