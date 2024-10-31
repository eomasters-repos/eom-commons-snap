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

import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.util.FeatureUtils;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Wgs84CrsProvider is a provider class that implements the FeatureUtils.FeatureCrsProvider interface.
 * It is responsible for providing the WGS84 Coordinate Reference System (CRS) for a given product.
 */
class Wgs84CrsProvider implements FeatureUtils.FeatureCrsProvider {

  private final boolean clipToBounds;

  /**
   * Constructs an instance of Wgs84CrsProvider.
   *
   * @param clipToBounds specifies whether the CRS should be clipped to product bounds
   */
  public Wgs84CrsProvider(boolean clipToBounds) {
    this.clipToBounds = clipToBounds;
  }

  /**
   * Returns the Coordinate Reference System (CRS) for a given product.
   * This implementation always returns the WGS84 geographic CRS.
   *
   * @param product the product for which the CRS is requested
   * @return the WGS84 Coordinate Reference System
   */
  @Override
  public CoordinateReferenceSystem getFeatureCrs(Product product) {
    return DefaultGeographicCRS.WGS84;
  }

  /**
   * Determines whether the CRS should be clipped to the product bounds.
   *
   * @return true if the CRS is clipped to product bounds, false otherwise
   */
  @Override
  public boolean clipToProductBounds() {
    return clipToBounds;
  }
}
