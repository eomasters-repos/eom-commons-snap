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

/**
 * Exception thrown by {@link ValidMaskImageBuilder} if there is an error when building the valid mask.
 */
public class ValidMaskBuilderException extends Exception {

  /**
   * Creates a new instance.
   *
   * @param message the error message
   */
  public ValidMaskBuilderException(String message) {
    super(message);
  }

  /**
   * Creates a new instance.
   *
   * @param message the error message
   * @param cause   the cause
   */
  public ValidMaskBuilderException(String message, Throwable cause) {
    super(message, cause);
  }
}
