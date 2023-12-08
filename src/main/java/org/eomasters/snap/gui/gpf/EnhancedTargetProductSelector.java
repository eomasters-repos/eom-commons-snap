/*-
 * ========================LICENSE_START=================================
 * EOMTBX PRO - EOMasters Toolbox PRO for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
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

package org.eomasters.snap.gui.gpf;

import java.io.File;
import javax.swing.JPanel;
import org.esa.snap.core.gpf.ui.TargetProductSelector;
import org.esa.snap.core.gpf.ui.TargetProductSelectorModel;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.rcp.actions.file.SaveProductAsAction;
import org.esa.snap.ui.AppContext;


/**
 * An enhanced version of {@link TargetProductSelector} that allows to listen to changes of the target handling.
 */
class EnhancedTargetProductSelector extends TargetProductSelector {


  private TargetHandlingListener targetHandlingListener;


  /**
   * Creates a new instance.
   *
   * @param appContext      the application context
   * @param alwaysWriteOutput whether the output should always be written to a file
   */
  public EnhancedTargetProductSelector(AppContext appContext, boolean alwaysWriteOutput) {
    super(new TargetProductSelectorModel(), alwaysWriteOutput);
    String homeDirPath = SystemUtils.getUserHomeDir().getPath();
    String saveDir = appContext.getPreferences()
                               .getPropertyString(SaveProductAsAction.PREFERENCES_KEY_LAST_PRODUCT_DIR, homeDirPath);
    getModel().setProductDir(new File(saveDir));
    if (!alwaysWriteOutput) {
      getOpenInAppCheckBox().setText("Open in " + appContext.getApplicationName());
    }
    getModel().getValueContainer().addPropertyChangeListener(evt -> {
      if (evt.getPropertyName().equals("saveToFileSelected") ||
          evt.getPropertyName().equals("openInAppSelected")) {
        if (targetHandlingListener != null) {
          targetHandlingListener.onChange(getModel().isSaveToFileSelected(), getModel().isOpenInAppSelected());
        }
      }
    });

  }

  @Override
  public JPanel createDefaultPanel() {
    JPanel defaultPanel = super.createDefaultPanel();
    if(targetHandlingListener != null) {
      // inform the listener initially
      targetHandlingListener.onChange(getModel().isSaveToFileSelected(), getModel().isOpenInAppSelected());
    }
    return defaultPanel;
  }

  /**
   * Sets the listener for output handling changes.
   * @param listener the listener
   */
  void setTargetHandlingListener(TargetHandlingListener listener) {
    targetHandlingListener = listener;
  }
}
