/*-
 * ========================LICENSE_START=================================
 * EOM Commons SNAP - Library of common utilities for ESA SNAP
 * -> https://www.eomasters.org/
 * ======================================================================
 * Copyright (C) 2023 Marco Peters
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

import java.awt.image.RenderedImage;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.util.DummyProductBuilder;
import org.esa.snap.core.util.DummyProductBuilder.GC;
import org.esa.snap.core.util.DummyProductBuilder.Size;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

class ValidMaskImageBuilderTest {

  private static final int INVALID = 0;
  private static final int VALID = 255;
  private static Product product;

  @BeforeAll
  static void beforeAll() {
    DummyProductBuilder builder = new DummyProductBuilder();
    builder.size(Size.SMALL).gc(GC.MAP);
    product = builder.create();
  }

  @Test
  void testCreateValidExpressionMask() throws ValidMaskBuilderException {
    ValidMaskImageBuilder maskImageBuilder = new ValidMaskImageBuilder(product);
    maskImageBuilder.withExpression("X == 10.5");
    RenderedImage validMaskImage = maskImageBuilder.create();

    assertEquals(VALID, validMaskImage.getData().getSample(10, 0, 0));
    assertEquals(VALID, validMaskImage.getData().getSample(10, 60, 0));
    assertEquals(INVALID, validMaskImage.getData().getSample(9, 0, 0));
    assertEquals(INVALID, validMaskImage.getData().getSample(11, 0, 0));
    assertEquals(INVALID, validMaskImage.getData().getSample(9, 60, 0));
    assertEquals(INVALID, validMaskImage.getData().getSample(11, 60, 0));
  }

  @Test
  void testCreateMaskWithWktGeometry() throws ParseException, ValidMaskBuilderException {
    ValidMaskImageBuilder maskImageBuilder = new ValidMaskImageBuilder(product);

    // Rectangle from 40,9 to 74,46
    maskImageBuilder.withWkt(new WKTReader().read(
        "POLYGON ((3.3258594917787736 -0.7772795216741405, 6.225710014947682 -0.7772795216741405, \n"
            + "   6.225710014947682 -3.8863976083707024, 3.3258594917787736 -3.8863976083707024, \n"
            + "   3.3258594917787736 -0.7772795216741405))"));
    RenderedImage validMaskImage = maskImageBuilder.create();

    assertEquals(VALID, validMaskImage.getData().getSample(45, 9, 0));
    assertEquals(VALID, validMaskImage.getData().getSample(53, 30, 0));
    assertEquals(VALID, validMaskImage.getData().getSample(74, 46, 0));
    assertEquals(INVALID, validMaskImage.getData().getSample(10, 0, 0));
    assertEquals(INVALID, validMaskImage.getData().getSample(85, 60, 0));
  }

  @Test
  void testCreateMaskWithShapefile() throws ValidMaskBuilderException {
    ValidMaskImageBuilder maskImageBuilder = new ValidMaskImageBuilder(product);
    // Rectangle from 27,28 to 60,64
    maskImageBuilder.withShapeFile(getClass().getResource("geometry_Polygon.shp"));
    RenderedImage validMaskImage = maskImageBuilder.create();

    assertEquals(VALID, validMaskImage.getData().getSample(27, 28, 0));
    assertEquals(VALID, validMaskImage.getData().getSample(45, 46, 0));
    assertEquals(VALID, validMaskImage.getData().getSample(55, 63, 0));
    assertEquals(INVALID, validMaskImage.getData().getSample(10, 0, 0));
    assertEquals(INVALID, validMaskImage.getData().getSample(30, 80, 0));
  }

  @Test
  void testCreateMaskWithMaskImage() throws ValidMaskBuilderException {
    RenderedImage maskImage = new ValidMaskImageBuilder(product).withExpression("X ==100.5 || Y==100.5").create();

    RenderedImage validMaskImage = new ValidMaskImageBuilder(product).withMaskImage(maskImage).create();

    assertEquals(VALID, validMaskImage.getData().getSample(100, 100, 0));
    assertEquals(VALID, validMaskImage.getData().getSample(100, 46, 0));
    assertEquals(VALID, validMaskImage.getData().getSample(55, 100, 0));
    assertEquals(INVALID, validMaskImage.getData().getSample(10, 0, 0));
    assertEquals(INVALID, validMaskImage.getData().getSample(30, 80, 0));
  }

  @Test
  void testCreateMaskAllDefaultOrCombined() throws ParseException, ValidMaskBuilderException {
    ValidMaskImageBuilder maskImageBuilder = new ValidMaskImageBuilder(product);
    maskImageBuilder.withExpression("X == 10.5");
    // Rectangle from 40,9 to 74,46
    maskImageBuilder.withWkt(
        "POLYGON ((3.3258594917787736 -0.7772795216741405, 6.225710014947682 -0.7772795216741405, \n"
            + "   6.225710014947682 -3.8863976083707024, 3.3258594917787736 -3.8863976083707024, \n"
            + "   3.3258594917787736 -0.7772795216741405))");
    // Rectangle from 27,28 to 60,64
    maskImageBuilder.withShapeFile(getClass().getResource("geometry_Polygon.shp"));
    RenderedImage maskImage = new ValidMaskImageBuilder(product).withExpression("X ==100.5 || Y==100.5").create();
    maskImageBuilder.withMaskImage(maskImage);

    RenderedImage validMaskImage = maskImageBuilder.create();

    assertEquals(VALID, validMaskImage.getData().getSample(10, 0, 0));
    assertEquals(VALID, validMaskImage.getData().getSample(10, 60, 0));
    assertEquals(VALID, validMaskImage.getData().getSample(45, 9, 0));
    assertEquals(VALID, validMaskImage.getData().getSample(53, 30, 0));
    assertEquals(VALID, validMaskImage.getData().getSample(74, 46, 0));
    assertEquals(VALID, validMaskImage.getData().getSample(27, 28, 0));
    assertEquals(VALID, validMaskImage.getData().getSample(45, 46, 0));
    assertEquals(VALID, validMaskImage.getData().getSample(55, 63, 0));
    assertEquals(VALID, validMaskImage.getData().getSample(100, 100, 0));
    assertEquals(VALID, validMaskImage.getData().getSample(100, 46, 0));
    assertEquals(VALID, validMaskImage.getData().getSample(55, 100, 0));

    assertEquals(INVALID, validMaskImage.getData().getSample(9, 0, 0));
    assertEquals(INVALID, validMaskImage.getData().getSample(30, 80, 0));

  }


  @Test
  void testCreateMaskWithOr() throws ValidMaskBuilderException {
    ValidMaskImageBuilder maskImageBuilder = new ValidMaskImageBuilder(product);
    RenderedImage validMaskImage = maskImageBuilder
        .withExpression("X == 10.5")
        .or()
        .withExpression("Y==3.5")
        .create();

    assertEquals(VALID, validMaskImage.getData().getSample(10, 0, 0));
    assertEquals(VALID, validMaskImage.getData().getSample(0, 3, 0));
    assertEquals(VALID, validMaskImage.getData().getSample(10, 3, 0));

    assertEquals(INVALID, validMaskImage.getData().getSample(9, 0, 0));
    assertEquals(INVALID, validMaskImage.getData().getSample(11, 0, 0));
    assertEquals(INVALID, validMaskImage.getData().getSample(0, 2, 0));
    assertEquals(INVALID, validMaskImage.getData().getSample(0, 4, 0));

  }

  @Test
  void testCreateMaskWithAnd() throws ValidMaskBuilderException {
    ValidMaskImageBuilder maskImageBuilder = new ValidMaskImageBuilder(product);
    RenderedImage validMaskImage = maskImageBuilder
        .withExpression("X == 10.5")
        .and()
        .withExpression("Y==3.5")
        .create();

    assertEquals(VALID, validMaskImage.getData().getSample(10, 3, 0));
    assertEquals(INVALID, validMaskImage.getData().getSample(10, 0, 0));
    assertEquals(INVALID, validMaskImage.getData().getSample(0, 3, 0));

    assertEquals(INVALID, validMaskImage.getData().getSample(9, 0, 0));
    assertEquals(INVALID, validMaskImage.getData().getSample(11, 0, 0));
    assertEquals(INVALID, validMaskImage.getData().getSample(0, 2, 0));
    assertEquals(INVALID, validMaskImage.getData().getSample(0, 4, 0));

  }
}
