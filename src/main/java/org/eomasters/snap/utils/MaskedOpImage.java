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

import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import javax.media.jai.ImageLayout;
import javax.media.jai.PlanarImage;
import javax.media.jai.PointOpImage;

/**
 * An image which masks another image. Only the pixels in the mask image are used, pixels outside the mask are set to
 * NaN. This ensures that only the pixels in the mask are processed.
 */
public class MaskedOpImage extends PointOpImage {

  /**
   * Creates a new masked image.
   *
   * @param source    the source image
   * @param maskImage the mask image
   */
  public MaskedOpImage(RenderedImage source, RenderedImage maskImage) {
    super(source, maskImage, new ImageLayout(source), null, false);
  }

  @Override
  protected void computeRect(PlanarImage[] sources, WritableRaster dest, Rectangle destRect) {
    Raster sourceRaster = sources[0].getData();
    int sourceDataType = sourceRaster.getSampleModel().getDataType();
    Raster maskRaster = sources[1].getData();
    int[] maskLine = new int[destRect.width];
    for (int y = destRect.y; y < destRect.y + destRect.height; y++) {
      maskRaster.getSamples(destRect.x, y, destRect.width, 1, 0, maskLine);
      for (int x = destRect.x; x < destRect.x + destRect.width; x++) {
        if (maskLine[x - destRect.x] != 0) {
          if (sourceDataType == DataBuffer.TYPE_FLOAT) {
            dest.setSample(x, y, 0, sourceRaster.getSampleFloat(x, y, 0));
          } else if (sourceDataType == DataBuffer.TYPE_DOUBLE) {
            dest.setSample(x, y, 0, sourceRaster.getSampleDouble(x, y, 0));
          } else {
            dest.setSample(x, y, 0, sourceRaster.getSample(x, y, 0));
          }
        } else {
          dest.setSample(x, y, 0, Double.NaN);
        }
      }
    }
  }

}
