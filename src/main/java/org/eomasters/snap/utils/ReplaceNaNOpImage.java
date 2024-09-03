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
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import javax.media.jai.PointOpImage;
import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;
import org.esa.snap.core.gpf.internal.OperatorContext;

public final class ReplaceNaNOpImage extends PointOpImage {

  private final double replacementValue;

  public ReplaceNaNOpImage(RenderedImage source, double value) {
    super(source, null, null, true);
    this.replacementValue = value;
    OperatorContext.setTileCache(this);
  }

  @Override
  protected void computeRect(Raster[] sources, WritableRaster dest, Rectangle destRect) {
    RasterFormatTag[] formatTags = getFormatTags();
    RasterAccessor s = new RasterAccessor(sources[0], destRect, formatTags[0], getSourceImage(0).getColorModel());
    RasterAccessor d = new RasterAccessor(dest, destRect, formatTags[1], getColorModel());
    Data data = Data.create(s, d);
    int sLineStride = s.getScanlineStride();
    int sPixelStride = s.getPixelStride();
    int[] sBandOffsets = s.getBandOffsets();
    int dLineStride = d.getScanlineStride();
    int dPixelStride = d.getPixelStride();
    int[] dBandOffsets = d.getBandOffsets();
    int sLineOffset = sBandOffsets[0];
    int dLineOffset = dBandOffsets[0];
    for (int y = 0; y < d.getHeight(); y++) {
      int sPixelOffset = sLineOffset;
      int dPixelOffset = dLineOffset;
      sLineOffset += sLineStride;
      dLineOffset += dLineStride;
      for (int x = 0; x < d.getWidth(); x++) {
        data.replaceNan(sPixelOffset, dPixelOffset, replacementValue);
        sPixelOffset += sPixelStride;
        dPixelOffset += dPixelStride;
      }
    }
    d.copyDataToRaster();
  }


  private static abstract class Data {

    static Data create(RasterAccessor src, RasterAccessor dst) {
      switch (src.getDataType()) {
        case 4: // '\004'
          return new DataF(src, dst);
        case 5: // '\005'
          return new DataD(src, dst);
        default:
          throw new IllegalStateException("Unsupported source image data type. Must be either float or double");
      }

    }

    abstract void replaceNan(int sPixelOffset, int dPixelOffset, double replacement);


    private static class DataF extends Data {
      public final float[] source;
      public final float[] dest;

      public DataF(RasterAccessor src, RasterAccessor dst) {
        source = src.getFloatDataArray(0);
        dest = dst.getFloatDataArray(0);
      }

      @Override
      void replaceNan(int sPixelOffset, int dPixelOffset, double replacement) {
        float sourceValue = source[sPixelOffset];
        if (Float.isNaN(sourceValue)) {
          dest[dPixelOffset] = (float)replacement;
        } else {
          dest[dPixelOffset] = sourceValue;
        }
      }

    }

    private static class DataD extends Data {
      public final double[] source;
      public final double[] dest;

      public DataD(RasterAccessor src, RasterAccessor dst) {
        source = src.getDoubleDataArray(0);
        dest = dst.getDoubleDataArray(0);
      }

      @Override
      void replaceNan(int sPixelOffset, int dPixelOffset, double replacement) {
        double sourceValue = source[sPixelOffset];
        if (Double.isNaN(sourceValue)) {
          dest[dPixelOffset] = replacement;
        } else {
          dest[dPixelOffset] = sourceValue;
        }
      }

    }
  }
}
