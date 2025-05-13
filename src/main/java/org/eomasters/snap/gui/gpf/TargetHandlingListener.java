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

package org.eomasters.snap.gui.gpf;

/**
 * Interface which can be implemented to listen on changes of target handling changes notified by the
 * {@link EnhancedTargetProductSelector}.
 */
public interface TargetHandlingListener {

  /**
   * Called when the target handling has changed.
   *
   * @param shallSave whether the output should be saved to a file
   * @param shallOpen whether the output should be opened in the application
   */
  void onChange(boolean shallSave, boolean shallOpen);

}
