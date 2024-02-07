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
import com.bc.ceres.swing.binding.internal.FileEditor;
import java.nio.file.Path;

/**
 * An editor for {@link Path}s using a file chooser dialog. It is registered as service in the file
 * <code>META-INF/services/com.bc.ceres.swing.binding.PropertyEditor</code>.
 */
public class PathEditor extends FileEditor {

    @Override
    public boolean isValidFor(PropertyDescriptor propertyDescriptor) {
        return Path.class.isAssignableFrom(propertyDescriptor.getType())
               && !Boolean.TRUE.equals(propertyDescriptor.getAttribute("directory"));
    }

}
