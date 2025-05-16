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

import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.stream.IntStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.PlanarImage;
import javax.media.jai.PointOpImage;

/**
 * An image which masks another image. Only the pixels in the mask image are used, pixels outside the mask are set to
 * the provided fillValue.
 */
public class MaskedOpImage extends PointOpImage {

  private final Number fillValue;

  /**
   * Creates a new masked image.
   *
   * @param source    the source image
   * @param maskImage the mask image
   * @param fillValue the fill value used for areas outside the mask image
   */
  public MaskedOpImage(RenderedImage source, RenderedImage maskImage, Number fillValue) {
    super(source, maskImage, new ImageLayout(source), null, false);
    this.fillValue = fillValue;
  }

  @Override
  protected void computeRect(PlanarImage[] sources, WritableRaster dest, Rectangle destRect) {
    Raster maskRaster = sources[1].getData(destRect);
    int[] maskData = new int[destRect.width * destRect.height];
    maskRaster.getSamples(destRect.x, destRect.y, destRect.width, destRect.height, 0, maskData);

    Raster sourceRaster = sources[0].getData(destRect);
    int sourceDataType = sourceRaster.getSampleModel().getDataType();
    if (sourceDataType == DataBuffer.TYPE_DOUBLE) {
      processDouble(sourceRaster, dest, destRect, maskData);
    } else if (sourceDataType == DataBuffer.TYPE_FLOAT) {
      processFloat(sourceRaster, dest, destRect, maskData);
    } else {
      processInt(sourceRaster, dest, destRect, maskData);
    }
  }

  private void processInt(Raster sourceRaster, WritableRaster dest, Rectangle destRect, int[] maskData) {
    int[] destData = new int[destRect.width * destRect.height];
    sourceRaster.getSamples(destRect.x, destRect.y, destRect.width, destRect.height, 0, destData);
    IntStream.range(0, destData.length).forEach(i -> {
      if (maskData[i] == 0) {
        destData[i] = fillValue.intValue();
      }
    });
    dest.setSamples(destRect.x, destRect.y, destRect.width, destRect.height, 0, destData);
  }

  private void processFloat(Raster sourceRaster, WritableRaster dest, Rectangle destRect, int[] maskData) {
    float[] destData = new float[destRect.width * destRect.height];
    sourceRaster.getSamples(destRect.x, destRect.y, destRect.width, destRect.height, 0, destData);
    IntStream.range(0, destData.length).forEach(i -> {
      if (maskData[i] == 0) {
        destData[i] = fillValue.floatValue();
      }
    });
    dest.setSamples(destRect.x, destRect.y, destRect.width, destRect.height, 0, destData);
  }

  private void processDouble(Raster sourceRaster, WritableRaster dest, Rectangle destRect, int[] maskData) {
    double[] destData = new double[destRect.width * destRect.height];
    sourceRaster.getSamples(destRect.x, destRect.y, destRect.width, destRect.height, 0, destData);
    IntStream.range(0, destData.length).forEach(i -> {
      if (maskData[i] == 0) {
        destData[i] = fillValue.doubleValue();
      }
    });
    dest.setSamples(destRect.x, destRect.y, destRect.width, destRect.height, 0, destData);
  }

}
