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

package org.eomasters.snap.gui;

import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.swing.binding.Binding;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.binding.ComponentAdapter;
import com.bc.ceres.swing.binding.PropertyEditor;
import com.bc.ceres.swing.binding.internal.TextComponentAdapter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * An editor for {@link Path}s using a file chooser dialog. It is registered as service in the file
 * <code>META-INF/services/com.bc.ceres.swing.binding.PropertyEditor</code>.
 */
public class PathEditor extends PropertyEditor {

  @Override
  public boolean isValidFor(PropertyDescriptor propertyDescriptor) {
    return Path.class.isAssignableFrom(propertyDescriptor.getType())
        && !Boolean.TRUE.equals(propertyDescriptor.getAttribute("directory"));
  }

  @Override
  public JComponent createEditorComponent(PropertyDescriptor propertyDescriptor, BindingContext bindingContext) {
    final JTextField textField = new JTextField();
    final ComponentAdapter adapter = new TextComponentAdapter(textField);
    final Binding binding = bindingContext.bind(propertyDescriptor.getName(), adapter);
    final JPanel editorPanel = new JPanel(new BorderLayout(2, 2));
    editorPanel.add(textField, BorderLayout.CENTER);
    final JButton etcButton = new JButton("...");
    final Dimension size = new Dimension(26, 16);
    etcButton.setPreferredSize(size);
    etcButton.setMinimumSize(size);
    etcButton.addActionListener(e -> {
      final JFileChooser fileChooser = new JFileChooser();
      Path currentFile = (Path) binding.getPropertyValue();
      if (currentFile != null) {
        fileChooser.setSelectedFile(currentFile.toFile());
      } else {
        Path selectedFile = (Path) propertyDescriptor.getDefaultValue();
        fileChooser.setSelectedFile(selectedFile.toFile());
      }
      int i = fileChooser.showDialog(editorPanel, "Select");
      if (i == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null) {
        binding.setPropertyValue(fileChooser.getSelectedFile());
      }
    });
    editorPanel.add(etcButton, BorderLayout.EAST);
    return editorPanel;
  }
}
