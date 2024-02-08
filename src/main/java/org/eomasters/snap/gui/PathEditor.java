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
