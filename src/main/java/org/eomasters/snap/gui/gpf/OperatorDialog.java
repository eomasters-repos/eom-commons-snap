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

import com.bc.ceres.swing.selection.SelectionChangeEvent;
import com.bc.ceres.swing.selection.SelectionChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import org.eomasters.gui.Dialogs;
import org.eomasters.gui.Highlighter;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.descriptor.OperatorDescriptor;
import org.esa.snap.core.gpf.ui.DefaultIOParametersPanel;
import org.esa.snap.core.gpf.ui.OperatorMenu;
import org.esa.snap.core.gpf.ui.OperatorParameterSupport;
import org.esa.snap.core.gpf.ui.SourceProductSelector;
import org.esa.snap.ui.AppContext;
import org.esa.snap.ui.ModelessDialog;

/**
 * A dialog for an operator.
 */
public class OperatorDialog extends ModelessDialog {

  private final AppContext appContext;
  private final OperatorDescriptor operatorDescriptor;
  private final JTabbedPane form;
  private boolean alwaysWriteTarget;
  private ParametersPanel<JPanel> parametersPanel;
  private DefaultIOParametersPanel ioPanel;
  private EnhancedTargetProductSelector targetProductSelector;
  private String productSuffix;
  private JScrollPane parametersScrollPanel;

  /**
   * Creates a new operator dialog.
   *
   * @param operatorDescriptor the operator descriptor
   * @param title              the dialog title
   * @param helpID             the help ID
   * @param appContext         the application context
   */
  public OperatorDialog(OperatorDescriptor operatorDescriptor, String title, String helpID, AppContext appContext) {
    super(appContext.getApplicationWindow(), title, ID_APPLY_CLOSE_HELP, helpID);
    this.operatorDescriptor = operatorDescriptor;
    this.appContext = appContext;
    productSuffix = "_" + (operatorDescriptor.getAlias() != null ? operatorDescriptor.getAlias() : "").toLowerCase();
    form = new JTabbedPane();
    renameApplyButtonToRun();
  }

  /**
   * Sets the suffix for the target product name.
   *
   * @param productSuffix the suffix
   */
  public void setProductSuffix(String productSuffix) {
    this.productSuffix = productSuffix;
  }

  /**
   * Sets whether the target should always be written to a file.
   * @param alwaysWriteTarget {@code true} if the target should always be written to a file
   */
  public void setAlwaysWriteTarget(boolean alwaysWriteTarget) {
    this.alwaysWriteTarget = alwaysWriteTarget;
  }

  /**
   * Sets the parameters panel.

   * @param parametersPanel the parameters panel
   */
  public void setParametersPanel(ParametersPanel<JPanel> parametersPanel) {
    this.parametersPanel = parametersPanel;
  }

  /**
   * Sets the icon of the dialog.
   * @param imageIcon the icon
   */
  public void setIcon(ImageIcon imageIcon) {
    getJDialog().setIconImage(imageIcon.getImage());
  }


  @Override
  public int show() {
    setContent(form);
    targetProductSelector = new EnhancedTargetProductSelector(appContext, alwaysWriteTarget);
    targetProductSelector.setTargetHandlingListener(new RunButtonUpdater(appContext));

    ioPanel = new DefaultIOParametersPanel(appContext, operatorDescriptor, targetProductSelector);
    ioPanel.initSourceProductSelectors();
    form.add("I/O Parameters", ioPanel);
    if (parametersPanel != null) {
      parametersScrollPanel = new JScrollPane(parametersPanel.getComponent());
      form.add("Processing Parameters", parametersScrollPanel);
      parametersPanel.onSourceProductSelectionChanged(ioPanel.createSourceProductsMap(), null);
      addSourceProductChangedListener(ioPanel);
    }
    ArrayList<SourceProductSelector> selectorList = ioPanel.getSourceProductSelectorList();
    if (!selectorList.isEmpty()) {
      updateTargetProductName(selectorList.get(0).getSelectedProduct());
    }
    if (getJDialog().getJMenuBar() == null) {
      final OperatorMenu operatorMenu = new OperatorMenu(getJDialog(),
          operatorDescriptor,
          new OperatorParameterSupport(operatorDescriptor),
          appContext,
          getHelpID());
      getJDialog().setJMenuBar(operatorMenu.createDefaultMenu());
    }
    return super.show();
  }

  @Override
  protected void onApply() {
    super.onApply();
    if (!canApply()) {
      return;
    }

    final OperatorExecutorDialog worker = new OperatorExecutorDialog(operatorDescriptor.getAlias(),
        targetProductSelector.getModel(), ioPanel.createSourceProductsMap(), parametersPanel.getParametersMap(),
        getJDialog(), appContext);
    worker.process();
  }

  private void updateTargetProductName(Product product) {
    if (product != null) {
      targetProductSelector.getModel().setProductName(product.getName() + "_" + productSuffix);
    }
  }

  private boolean canApply() {
    if (parametersPanel != null) {
      ValidationResult validationResult = parametersPanel.doValidation();
      if (!validationResult.isValid()) {
        form.setSelectedComponent(parametersScrollPanel);
        Highlighter.error(validationResult.getComponent(), validationResult.getValidationMessage());
        return false;
      }
    }
    if (!isIoInputValid()) {
      return false;
    }
    return true;
  }

  private boolean isIoInputValid() {
    ArrayList<SourceProductSelector> selectorList = ioPanel.getSourceProductSelectorList();
    for (SourceProductSelector sourceProductSelector : selectorList) {
      if (sourceProductSelector.getSelectedProduct() == null) {
        form.setSelectedComponent(ioPanel);
        Highlighter.error(sourceProductSelector.getProductNameComboBox(), "Please specify a source product.");
        return true;
      }
    }

    final String productName = targetProductSelector.getModel().getProductName();
    if (productName == null || productName.isEmpty()) {
      form.setSelectedComponent(ioPanel);
      Highlighter.error(targetProductSelector.getProductNameTextField(), "Please specify a target product name.");
      targetProductSelector.getProductNameTextField().requestFocus();
      return false;
    }

    String alreadyOpenedMessage = null;
    if (targetProductSelector.getModel().isOpenInAppSelected()) {
      if (appContext.getProductManager().getProduct(productName) != null) {
        alreadyOpenedMessage = String.format(
            "A product with the name ''%s'' is already opened in %s", productName, appContext.getApplicationName());
      }
    }
    String fileExistsMessage = null;
    if (targetProductSelector.getModel().isSaveToFileSelected()) {
      File productFile = targetProductSelector.getModel().getProductFile();
      if (productFile.exists()) {
        fileExistsMessage = String.format(
            "The specified output file already exists.<br>%s", productFile.getPath());
      }
    }

    if (alreadyOpenedMessage != null || fileExistsMessage != null) {
      StringBuilder sb = new StringBuilder("<html>There is a conflict with the target product.<br><ul>");
      if (alreadyOpenedMessage != null) {
        sb.append(String.format("<li>%s</li>", alreadyOpenedMessage));
      }
      if (fileExistsMessage != null) {
        sb.append(String.format("<li>%s</li>", fileExistsMessage));
      }
      sb.append("</ul><br>Do you want to continue?");
      if (!Dialogs.confirmation(getTitle(), sb.toString(), getJDialog())) {
        form.setSelectedComponent(ioPanel);
        Highlighter.error(targetProductSelector.getProductNameTextField(), "Please change the target product.");
        return false;
      }
    }

    return true;
  }


  private void renameApplyButtonToRun() {
    AbstractButton button = getButton(ID_APPLY);
    button.setText("Run");
    button.setMnemonic('R');
  }

  private void addSourceProductChangedListener(DefaultIOParametersPanel ioPanel) {
    ArrayList<SourceProductSelector> selectorList = ioPanel.getSourceProductSelectorList();
    for (SourceProductSelector sourceProductSelector : selectorList) {
      sourceProductSelector.addSelectionChangeListener(new ProductSelectionChangeDelegate());
    }
  }

  private class RunButtonUpdater implements TargetHandlingListener {

    private final AppContext appContext;

    public RunButtonUpdater(AppContext appContext) {
      this.appContext = appContext;
    }

    @Override
    public void onChange(boolean shallSave, boolean shallOpen) {
      AbstractButton button = getButton(ID_APPLY);
      String toolTipText = "";
      boolean enabled = true;
      if (shallSave && shallOpen) {
        toolTipText = "Save target product and open it in " + appContext.getApplicationName();
      } else if (shallSave) {
        toolTipText = "Save target product";
      } else if (shallOpen) {
        toolTipText = "Open target product in " + appContext.getApplicationName();
      } else {
        enabled = false;
      }
      button.setToolTipText(toolTipText);
      button.setEnabled(enabled);
    }

  }

  private class ProductSelectionChangeDelegate implements SelectionChangeListener {

    @Override
    public void selectionChanged(SelectionChangeEvent event) {
      Product changedValue = (Product) event.getSelection().getSelectedValue();
      updateTargetProductName(changedValue);

      if (parametersPanel != null) {
        HashMap<String, Product> productsMap = ioPanel.createSourceProductsMap();
        for (String key : productsMap.keySet()) {
          if (productsMap.get(key) == changedValue) {
            parametersPanel.onSourceProductSelectionChanged(productsMap, key);
            return;
          }
        }
      }
    }

    @Override
    public void selectionContextChanged(SelectionChangeEvent event) {

    }
  }

}
