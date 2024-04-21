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

package org.eomasters.snap.gui.gpf;

import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import org.eomasters.icons.Icon;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.descriptor.OperatorDescriptor;
import org.esa.snap.rcp.actions.AbstractSnapAction;
import org.esa.snap.ui.AppContext;

/**
 * Abstract action for operators which simplifies which allows the dialog and the menu item to have an icon.
 */
public abstract class AbstractOpAction extends AbstractSnapAction {

  private final OperatorSpi spi;
  private final String dialogTitle;
  private final Icon icon;
  private final String helpId;
  private final String opName;

  /**
   * Creates a new action for an operator.
   *
   * @param actionName The name of the action
   * @param icon The icon
   * @param operatorName The name of the operator
   * @param dialogTitle The title of the dialog
   * @param helpId The help id
   */
  public AbstractOpAction(String actionName, Icon icon, String operatorName, String dialogTitle, String helpId) {
    this.icon = icon;
    this.opName = operatorName;
    this.dialogTitle = dialogTitle;
    this.helpId = helpId;
    putValue(NAME, actionName);
    putValue(SMALL_ICON, icon.getImageIcon(Icon.SIZE_16));
    putValue(LARGE_ICON_KEY, icon.getImageIcon(Icon.SIZE_32));

    spi = GPF.getDefaultInstance()
             .getOperatorSpiRegistry()
             .getOperatorSpi(opName);
  }

  protected abstract ParametersPanel<JPanel> getParametersPanel(OperatorDescriptor operatorDescriptor);

  @Override
  public void actionPerformed(ActionEvent e) {
    createDialog(getAppContext()).show();
  }

  protected OperatorDialog createDialog(AppContext appContext) {
    if (spi == null) {
      throw new IllegalArgumentException("No SPI found for operator name '" + opName + "'");
    }
    OperatorDescriptor opDescriptor = spi.getOperatorDescriptor();
    OperatorDialog dialog = new OperatorDialog(opDescriptor, dialogTitle, helpId, appContext);
    dialog.setIcon(icon.getImageIcon(Icon.SIZE_32));
    dialog.setParametersPanel(getParametersPanel(opDescriptor));
    return dialog;
  }
}
