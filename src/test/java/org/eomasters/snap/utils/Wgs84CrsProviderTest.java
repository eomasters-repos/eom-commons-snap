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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.esa.snap.core.datamodel.Product;

public class Wgs84CrsProviderTest {

  @Test
  void testRetrieveWgs84CrsForProduct() {
    Product mockProduct = Mockito.mock(Product.class);
    Wgs84CrsProvider provider = new Wgs84CrsProvider(true);

    Assertions.assertEquals(DefaultGeographicCRS.WGS84, provider.getFeatureCrs(mockProduct));
  }

  @Test
  void testClipToProductBounds() {
    Assertions.assertTrue(new Wgs84CrsProvider(true).clipToProductBounds());
    Assertions.assertFalse(new Wgs84CrsProvider(false).clipToProductBounds());
  }

}
