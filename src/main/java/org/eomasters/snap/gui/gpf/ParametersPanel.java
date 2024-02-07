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

import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import org.esa.snap.core.datamodel.Product;


/**
 * Interface for a parameters panel. A parameters panel is a panel to be used with the  {@link OperatorDialog} to
 * display the parameters.
 *
 * @param <J> the type of the component
 */
public interface ParametersPanel<J extends JComponent> {

  /**
   * Is called when the source product selection has changed. The <code>changedKey</code> is the key of the currently
   * changed source product. If <code>changedKey</code> is <code>null</code>, then it is unknown which product has
   * changed or all source products have changed.
   *
   * @param selectedProducts the currently selected source products
   * @param changedKey       the key of the currently changed source product or <code>null</code> if unknown
   */
  void onSourceProductSelectionChanged(HashMap<String, Product> selectedProducts, String changedKey);

  /**
   * Returns the component of this parameters panel. By default, this method returns <code>this</code> instance.
   */
  @SuppressWarnings("unchecked")
  default J getComponent() {
    return (J) this;
  }

  /**
   * Validates the parameters of this parameters panel. If the validation is not successful, a {@link ValidationResult}
   * with the validation message and the component that caused the validation error is returned. If the validation is
   * successful, a {@link ValidationResult} with <code>true</code> is returned.
   *
   * @return the validation result
   */
  ValidationResult doValidation();

  /**
   * Returns the parameters map of this parameters panel. The parameters map contains the parameters of this parameters
   * panel. The keys of the map are the parameter names and the values are the parameter values.
   *
   * @return the parameters map
   */
  Map<String, Object> getParametersMap();
}
