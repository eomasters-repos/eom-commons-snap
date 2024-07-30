package org.eomasters.snap.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.time.Duration;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

public class MaskedOpImageTest {

  @Test
  public void testComputeRect() {
    Rectangle rect = new Rectangle(0, 0, 5, 5);
    BufferedImage sourceImage = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_BYTE_GRAY);
    BufferedImage maskImage = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_BYTE_BINARY);

    // Create artificial raster
    for (int y = 0; y < rect.height; y++) {
      for (int x = 0; x < rect.width; x++) {
        sourceImage.getRaster().setSample(x, y, 0, x * y);
        maskImage.getRaster().setSample(x, y, 0, x % 2 == 0 ? 1 : 0);
      }
    }

    MaskedOpImage maskedOpImage = new MaskedOpImage(sourceImage, maskImage);
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
    MaskedOpImage maskedOpImage = new MaskedOpImage(sourceImage, maskImage);
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