/*-
 * ========================LICENSE_START=================================
 * EOMTBX PRO - EOMasters Toolbox PRO for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
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

package org.eomasters.snap.gui.gpf;

import javax.swing.JComponent;

/**
 * Result of a {@link ParametersPanel} validation. If the validation is not successful, the validation message and the
 * component that caused the validation error are provided. If the validation is successful, the {@link #isValid()}
 * method returns <code>true</code>. In this case, the validation message and the component are not set.
 */
public class ValidationResult {

  private final String validationMessage;
  private final JComponent component;
  private final boolean valid;

  /**
   * Creates an invalid validation result.
   *
   * @param validationMessage the validation message
   * @param component         the component that caused the validation error
   */
  public static ValidationResult createInvalidResult(String validationMessage, JComponent component) {
    return new ValidationResult(false, validationMessage, component);
  }

  /**
   * Creates a valid validation result.
   */
  public static ValidationResult createValidResult() {
    return new ValidationResult(true, null, null);
  }

  private ValidationResult(boolean valid, String validationMessage, JComponent component) {
    this.validationMessage = validationMessage;
    this.component = component;
    this.valid = valid;
  }

  /**
   * Checks if the validation is successful.
   *
   * @return <code>true</code> if the validation is successful, <code>false</code> otherwise
   */
  public boolean isValid() {
    return valid;
  }

  /**
   * Gets the validation message.
   *
   * @return the validation message
   */
  public String getValidationMessage() {
    return validationMessage;
  }

  /**
   * Gets the component that caused the validation error.
   *
   * @return the component that caused the validation error
   */
  public JComponent getComponent() {
    return component;
  }
}
