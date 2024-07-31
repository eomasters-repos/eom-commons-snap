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

import org.junit.jupiter.api.Test;

class ProductUtilitiesTest {


  @Test
  void testCreateValidNodeName_withSpecialChars() {
    String input = "/some*input:";
    String expected = "_some_input_";

    String result = ProductUtilities.createValidNodeName(input);

    assertEquals(expected, result);
  }

  @Test
  void testCreateValidNodeName_withSpaces() {
    String input = "input with spaces";
    String expected = "input_with_spaces";

    String result = ProductUtilities.createValidNodeName(input);

    assertEquals(expected, result);
  }

  @Test
  void testCreateValidNodeName_leadingDot() {
    String input = ".inputWithSpaces";
    String expected = "_inputWithSpaces";

    String result = ProductUtilities.createValidNodeName(input);

    assertEquals(expected, result);
  }


  @Test
  void testCreateValidNodeName_withSpecialWords() {
    String input = "What and This or not";
    String expected = "What_This_";

    String result = ProductUtilities.createValidNodeName(input);

    assertEquals(expected, result);
  }

  @Test
  void testCreateValidNodeName_withSpecialWordsWithinOtherWords() {
    String input = "Wand more cannot";
    String expected = "Wand_more_cannot";

    String result = ProductUtilities.createValidNodeName(input);

    assertEquals(expected, result);
  }
}

