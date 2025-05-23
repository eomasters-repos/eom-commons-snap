/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
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

package org.eomasters.snap.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SnapSystemReportTest {

  @Test
  void testReport() {
    String report;
    SnapSystemReport sr = new SnapSystemReport();
    report = sr.generate();
    assertTrue(report.startsWith("System Report"));

    sr.throwable(new Exception("Test Exception"));
    sr.logTail(10);
    report = sr.generate();
    assertTrue(report.startsWith("Error Report"));
    assertTrue(report.contains("Stacktrace:"));
    assertTrue(report.contains("System Log Tail (10 lines):"));

    System.out.println(report);
  }
}
