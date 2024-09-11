/*-
 * ========================LICENSE_START=================================
 * EOM Commons SNAP - Library of common utilities for ESA SNAP
 * -> https://www.eomasters.org/
 * ======================================================================
 * Copyright (C) 2023 - 2024 Marco Peters
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import org.junit.jupiter.api.Test;

public class MaskedOpImageTest {

  @Test
  public void testComputeRect() {
    Rectangle rect = new Rectangle(0, 0, 5, 5);
    BufferedImage sourceImage = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_BYTE_GRAY);
    BufferedImage maskImage = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_BYTE_GRAY);

    // Create artificial raster
    for (int y = 0; y < rect.height; y++) {
      for (int x = 0; x < rect.width; x++) {
        sourceImage.getRaster().setSample(x, y, 0, x * y);
        maskImage.getRaster().setSample(x, y, 0, x % 2 == 0 ? 1 : 0);
      }
    }

    RenderedImage maskedOpImage = new MaskedOpImage(sourceImage, maskImage, Float.NaN);
    // Compute and check result
    Raster data = maskedOpImage.getData(rect);
    for (int y = 0; y < rect.height; y++) {
      for (int x = 0; x < rect.width; x++) {
        double expected = x % 2 == 0 ? x * y : 0;
        double actual = data.getSampleDouble(x, y, 0);
        assertEquals(expected, actual, "Incorrect value at (" + x + "," + y + ")");
      }
    }
  }

  @Test
  public void testComputeRectAllMasked() {
    Rectangle rect = new Rectangle(0, 0, 5, 5);
    BufferedImage sourceImage = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_BYTE_GRAY);
    BufferedImage maskImage = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_BYTE_BINARY);

    // Create artificial raster
    for (int y = 0; y < 5; y++) {
      for (int x = 0; x < 5; x++) {
        sourceImage.getRaster().setSample(x, y, 0, x * y);
        maskImage.getRaster().setSample(x, y, 0, 1);
      }
    }

    // Compute and check result
    RenderedImage maskedOpImage = new MaskedOpImage(sourceImage, maskImage, Float.NaN);
    Raster data = maskedOpImage.getData(rect);
    for (int y = 0; y < 5; y++) {
      for (int x = 0; x < 5; x++) {
        double expected = x * y;
        double actual = data.getSampleDouble(x, y, 0);
        assertEquals(expected, actual, "Incorrect value at (" + x + "," + y + ")");
      }
    }

  }

}
