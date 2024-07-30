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

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glevel.MultiLevelImage;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import org.esa.snap.core.datamodel.Mask;
import org.esa.snap.core.datamodel.PlainFeatureFactory;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.VectorDataNode;
import org.esa.snap.core.image.VirtualBandOpImage;
import org.esa.snap.core.jexp.Term;
import org.esa.snap.core.util.FeatureUtils;
import org.esa.snap.core.util.jai.JAIUtils;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * A builder for creating a valid mask image. If not otherwise defined, masks are combined by an AND operation.
 **/
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class ValidMaskImageBuilder {

  private static final int VALID = 255;
  private static final Dimension FALL_BACK_TILESIZE = new Dimension(128, 128);
  private final Product sourceProduct;
  private final ArrayList<MaskImage> maskImages = new ArrayList<>();
  private MaskOperation joinOperation;
  private Dimension tileSize;


  /**
   * Creates a new builder for the given source product.
   *
   * @param product the product to create the mask from
   */
  public ValidMaskImageBuilder(Product product) {
    this.sourceProduct = product;
    joinOperation = MaskOperation.AND;
  }

  /**
   * Creates the mask image.
   *
   * @return the mask image
   * @throws ValidMaskBuilderException if no mask image was defined
   */
  public RenderedImage create() throws ValidMaskBuilderException {
    if (maskImages.isEmpty()) {
      return createConstantMask(VALID);
    }
    RenderedImage mask = maskImages.get(0).create(sourceProduct, getEffectiveTileSize());
    for (int i = 1; i < maskImages.size(); i++) {
      MaskImage element = maskImages.get(i);
      RenderedImage maskImage = element.create(sourceProduct, getEffectiveTileSize());
      mask = JAI.create(element.getOperationName(), mask, maskImage);
    }

    return mask;
  }

  /**
   * Switches the operation for combining the following masks to <code>OR</code>.
   *
   * @return the current builder instance
   */
  public ValidMaskImageBuilder or() {
    joinOperation = MaskOperation.OR;
    return this;
  }

  /**
   * Switches the operation for combining the following masks to <code>AND</code>.
   *
   * @return the current builder instance
   */
  public ValidMaskImageBuilder and() {
    joinOperation = MaskOperation.AND;
    return this;
  }


  /**
   * Adds a mask based on an image.
   *
   * @param maskImage the mask
   * @return the current builder instance
   */
  public ValidMaskImageBuilder withMaskImage(RenderedImage maskImage) {
    if (maskImage != null) {
      maskImages.add(new WrappedImage(joinOperation, maskImage));
    }
    return this;
  }

  /**
   * Adds a mask using a valid expression.
   *
   * @param validExpression the expression to be used as a mask
   * @return the current builder instance
   */
  public ValidMaskImageBuilder withExpression(String validExpression) {
    if (validExpression != null && !validExpression.isEmpty()) {
      maskImages.add(new ValidExprImage(joinOperation, validExpression));
    }
    return this;
  }

  /**
   * Adds a mask using a geometry.
   *
   * @param area the geometry
   * @return the current builder instance
   */
  public ValidMaskImageBuilder withGeometryArea(Geometry area) {
    if (area != null) {
      maskImages.add(new WktRoiImage(joinOperation, area));
    }
    return this;
  }

  /**
   * Adds a mask created from a WKT string.
   *
   * @param wktString the WKT string
   * @return the current builder instance
   * @throws ParseException if the WKT string is not valid
   */
  public ValidMaskImageBuilder withWktArea(String wktString) throws ParseException {
    if (wktString != null && !wktString.isEmpty()) {
      maskImages.add(new WktRoiImage(joinOperation, new WKTReader().read(wktString)));
    }
    return this;
  }

  /**
   * Adds a mask read from a shape file.
   *
   * @param shapeFile the path to the shape file
   * @return the current builder instance
   */
  public ValidMaskImageBuilder withShapeFile(Path shapeFile) {
    if (shapeFile != null) {
      maskImages.add(new ShapefileImage(joinOperation, shapeFile));
    }
    return this;
  }

  /**
   * Sets the preferred tile size for creating the mask image. If not provided the preferred tile size of the source
   * product will be used, if also not available a default tile size of [128,128] will be used.
   *
   * @param preferredTileSize the preferred tile size
   * @return the updated ValidMaskImageBuilder instance
   */
  public ValidMaskImageBuilder withTileSize(Dimension preferredTileSize) {
    this.tileSize = preferredTileSize;
    return this;
  }

  /**
   * Adds a mask read from a shape file.
   *
   * @param shapeUrl the url of the shape file
   * @return the current builder instance
   */
  public ValidMaskImageBuilder withShapeFile(URL shapeUrl) {
    if (shapeUrl != null) {
      maskImages.add(new ShapefileImage(joinOperation, shapeUrl));
    }
    return this;
  }

  @SuppressWarnings("SameParameterValue")
  private RenderedImage createConstantMask(int value) {
    ParameterBlock pb = new ParameterBlock();
    Dimension dimension = sourceProduct.getSceneRasterSize();
    pb.add(Float.valueOf(dimension.width));
    pb.add(Float.valueOf(dimension.height));
    pb.add(new Byte[]{(byte) value});

    Dimension tileSize = getEffectiveTileSize();
    ImageLayout tileLayout = new ImageLayout();
    tileLayout.setTileWidth(tileSize.width);
    tileLayout.setTileHeight(tileSize.height);

    return JAI.create("Constant", pb, new RenderingHints(JAI.KEY_IMAGE_LAYOUT, tileLayout));
  }

  private Dimension getEffectiveTileSize() {
    if (tileSize != null) {
      return tileSize;
    } else if (sourceProduct.getPreferredTileSize() != null) {
      return sourceProduct.getPreferredTileSize();
    } else {
      return FALL_BACK_TILESIZE;
    }
  }

  private enum MaskOperation {
    OR, AND
  }

  private abstract static class MaskImage {

    private final MaskOperation operation;

    public MaskImage(MaskOperation operation) {
      this.operation = operation;
    }

    public abstract RenderedImage create(Product product, Dimension tileSize) throws ValidMaskBuilderException;

    String getOperationName() {
      return operation.name();
    }
  }

  private static class WrappedImage extends MaskImage {

    private final RenderedImage image;

    public WrappedImage(MaskOperation operation, RenderedImage image) {
      super(operation);
      this.image = image;
    }

    @Override
    public RenderedImage create(Product product, Dimension tileSize) {
      return JAIUtils.createTileFormatOp(image, tileSize.width, tileSize.height);
    }
  }

  private static class ValidExprImage extends MaskImage {

    private final String validExpression;

    public ValidExprImage(MaskOperation operation, String validExpression) {
      super(operation);
      this.validExpression = validExpression;
    }

    @Override
    public RenderedImage create(Product product, Dimension tileSize) throws ValidMaskBuilderException {
      if (validExpression == null || validExpression.isEmpty()) {
        throw new ValidMaskBuilderException("Expression must not be null or empty.");
      }
      Term term = VirtualBandOpImage.parseExpression(validExpression, product);
      VirtualBandOpImage.Builder builder = VirtualBandOpImage.builder(term).
                                                             sourceSize(product.getSceneRasterSize())
                                                             .tileSize(tileSize)
                                                             .mask(true);
      return builder.create();
    }
  }

  private class WktRoiImage extends MaskImage {

    private final Geometry geometry;

    public WktRoiImage(MaskOperation operation, Geometry geometry) {
      super(operation);
      this.geometry = geometry;
    }

    @Override
    public RenderedImage create(Product product, Dimension tileSize) throws ValidMaskBuilderException {
      if (geometry == null) {
        throw new ValidMaskBuilderException("Geometry must not be null.");
      }

      SimpleFeatureType wktFeatureType = PlainFeatureFactory.createDefaultFeatureType(DefaultGeographicCRS.WGS84);
      ListFeatureCollection newCollection = new ListFeatureCollection(wktFeatureType);
      SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(wktFeatureType);
      SimpleFeature wktFeature = featureBuilder.buildFeature("ID" + Long.toHexString(System.currentTimeMillis()));
      wktFeature.setDefaultGeometry(geometry);
      newCollection.add(wktFeature);
      FeatureCollection<SimpleFeatureType, SimpleFeature> wktFeatures =
          FeatureUtils.clipFeatureCollectionToProductBounds(newCollection, sourceProduct,
              null, ProgressMonitor.NULL);

      VectorDataNode roiNode = new VectorDataNode("WktRoiImage", wktFeatures);
      roiNode.setOwner(sourceProduct);

      Dimension dimension = product.getSceneRasterSize();
      Mask wktRoiMask = new Mask("m", dimension.width, dimension.height, Mask.VectorDataType.INSTANCE);
      Mask.VectorDataType.setVectorData(wktRoiMask, roiNode);
      wktRoiMask.setOwner(sourceProduct);
      MultiLevelImage sourceImage = wktRoiMask.getSourceImage();
      return JAIUtils.createTileFormatOp(sourceImage.getImage(0), tileSize.width, tileSize.height);
    }
  }

  private class ShapefileImage extends MaskImage {

    private final File shapeFile;

    public ShapefileImage(MaskOperation operation, Path shapePath) {
      this(operation, shapePath.toFile());
    }

    public ShapefileImage(MaskOperation operation, URL shapeUrl) {
      this(operation, new File(shapeUrl.getFile()));
    }

    public ShapefileImage(MaskOperation operation, File shapeFile) {
      super(operation);
      this.shapeFile = shapeFile;
    }


    @Override
    public RenderedImage create(Product product, Dimension tileSize) throws ValidMaskBuilderException {
      if (shapeFile == null) {
        throw new ValidMaskBuilderException("Shapefile must not be null.");
      }

      Dimension dimension = product.getSceneRasterSize();
      final FeatureUtils.FeatureCrsProvider crsProvider = new Wgs84CrsProvider();
      DefaultFeatureCollection simpleFeatures;
      try {
        simpleFeatures = FeatureUtils.loadShapefileForProduct(shapeFile, sourceProduct,
            crsProvider, ProgressMonitor.NULL);
      } catch (IOException e) {
        throw new ValidMaskBuilderException("Cannot load shapefile.", e);
      }
      VectorDataNode shapefileRoiNode = new VectorDataNode("shapefileRoiImage", simpleFeatures);
      shapefileRoiNode.setOwner(sourceProduct);

      Mask shapefileMaks = new Mask("m", dimension.width, dimension.height, Mask.VectorDataType.INSTANCE);
      shapefileMaks.setOwner(sourceProduct);
      Mask.VectorDataType.setVectorData(shapefileMaks, shapefileRoiNode);
      return JAIUtils.createTileFormatOp(shapefileMaks.getSourceImage().getImage(0), tileSize.width, tileSize.height);
    }

    private class Wgs84CrsProvider implements FeatureUtils.FeatureCrsProvider {

      @Override
      public CoordinateReferenceSystem getFeatureCrs(Product product) {
        return DefaultGeographicCRS.WGS84;
      }

      @Override
      public boolean clipToProductBounds() {
        return true;
      }
    }

  }

}
