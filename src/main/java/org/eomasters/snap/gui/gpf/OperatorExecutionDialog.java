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

import static org.openide.awt.NotificationDisplayer.Priority.NORMAL;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.SubProgressMonitor;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import java.awt.Component;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.prefs.Preferences;
import org.eomasters.audio.Audios;
import org.eomasters.gui.Dialogs;
import org.eomasters.icons.Icon;
import org.eomasters.icons.Icons;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.OperatorCancelException;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.common.WriteOp;
import org.esa.snap.core.gpf.internal.OperatorExecutor;
import org.esa.snap.core.gpf.internal.OperatorProductReader;
import org.esa.snap.core.gpf.ui.TargetProductSelectorModel;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.ui.AppContext;
import org.openide.awt.NotificationDisplayer;

/**
 * A dialog for executing an operator and showing the progress of the processing is the target product shall be written
 * to disk.
 */
public class OperatorExecutionDialog {

  private final InternalSwingWorker internalWorker;

  /**
   * Creates a new progress dialog.
   *
   * @param operatorName   the name of the operator
   * @param model          the model for the target product
   * @param sourceProducts the source products
   * @param parametersMap  the parameters map of the operator
   * @param parent         the parent component
   * @param appContext     the application context
   */
  public OperatorExecutionDialog(String operatorName, TargetProductSelectorModel model,
      Map<String, Product> sourceProducts,
      Map<String, Object> parametersMap, Component parent, AppContext appContext) {
    internalWorker = new InternalSwingWorker(operatorName, model, sourceProducts, parametersMap, parent, appContext);
  }


  /**
   * Starts the processing of the operator.
   */
  public void process() {
    internalWorker.executeWithBlocking();
  }


  private static class InternalSwingWorker extends ProgressMonitorSwingWorker<Product, Object> {

    private final AppContext appContext;
    private final String operatorName;
    private final Map<String, Product> sourceProducts;
    private final Map<String, Object> parametersMap;
    private final TargetProductSelectorModel model;
    private Duration duration;

    public InternalSwingWorker(String operatorName, TargetProductSelectorModel model,
        Map<String, Product> sourceProducts,
        Map<String, Object> parametersMap, Component parent, AppContext appContext) {
      super(parent, "Processing" + operatorName + "   Product");
      this.appContext = appContext;
      this.operatorName = operatorName;
      this.sourceProducts = sourceProducts;
      this.parametersMap = parametersMap;
      this.model = model;
    }


    @Override
    protected Product doInBackground(ProgressMonitor pm) {
      pm.beginTask("Processing...", 100);

      Instant startTime = Instant.now();
      Product targetProduct;
      try {
        targetProduct = GPF.createProduct(operatorName, parametersMap, sourceProducts);
        if (targetProduct == null) {
          throw new OperatorException("Target product could not be created.");
        }

        targetProduct.setName(model.getProductName());

        if (model.isSaveToFileSelected()) {
          Operator executionOperator = createExecutionOperator(targetProduct);
          final OperatorExecutor executor = OperatorExecutor.create(executionOperator);
          executor.execute(SubProgressMonitor.create(pm, 100));
        }

        duration = Duration.between(startTime, Instant.now());
      } finally {
        pm.done();
      }
      return targetProduct;
    }

    @Override
    protected void done() {
      try {
        final Product product = get();
        if (model.isSaveToFileSelected()) {
          maybePlaySound();
          if (model.isOpenInAppSelected()) {
            appContext.getProductManager().addProduct(product);
          }
          Path productFile = model.getProductFile().toPath();
          final String message = String.format(
              "<html>Processing completed in <b>%s</b>.<br>"
                  + "Product has been written to<br><b>%s</b>",
              formatDuration(duration), productFile);
          Dialogs.message(null, operatorName, message);
        } else if (model.isOpenInAppSelected()) {
          appContext.getProductManager().addProduct(product);
          NotificationDisplayer.getDefault().notify(operatorName,
              Icons.INFO.getImageIcon(Icon.SIZE_24),
              String.format(
                  "Product '%s' has been opened in %s. The actual processing is deferred until the data is requested.",
                  product.getName(), appContext.getApplicationName()), null, NORMAL);
        } else {
          throw new IllegalStateException("Neither save to file nor open in app is selected.");
        }
      } catch (InterruptedException e) {
        // ignore
      } catch (Throwable t) {
        if (t.getCause() instanceof OperatorCancelException) {
          Dialogs.message(null, operatorName, "Processing cancelled.");
        } else {
          Dialogs.error(operatorName, "Error during processing.", t.getCause());
        }
      }
    }

    private static void maybePlaySound() {
      final Preferences preferences = SnapApp.getDefault().getPreferences();
      if (preferences.getBoolean(GPF.BEEP_AFTER_PROCESSING_PROPERTY, false)) {
        Audios.NOTIFICATION.play();
      }
    }

    private String formatDuration(Duration duration) {
      return String.format("%02d:%02d:%02d (hh:mm:ss)",
          duration.toHours(),
          duration.toMinutesPart(),
          duration.toSecondsPart());
    }


    private Operator createExecutionOperator(Product targetProduct) {
      if (targetProduct.getProductReader() instanceof OperatorProductReader) {
        final OperatorProductReader opReader = (OperatorProductReader) targetProduct.getProductReader();
        Operator operator = opReader.getOperatorContext().getOperator();
        if (operator.getSpi().getOperatorDescriptor().isAutoWriteDisabled()) {
          return operator;
        }
      }
      WriteOp writeOp = new WriteOp(targetProduct, model.getProductFile(), model.getFormatName());
      writeOp.setDeleteOutputOnFailure(true);
      writeOp.setWriteEntireTileRows(true);
      writeOp.setClearCacheAfterRowWrite(false);
      return writeOp;
    }

  }
}
