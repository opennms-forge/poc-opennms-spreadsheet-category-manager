/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with OpenNMS(R). If not, see:
 * http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org> http://www.opennms.org/ http://www.opennms.com/
 * *****************************************************************************
 */
package org.opennms.forge.spreadsheetcategorymanager.utils;

import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * @author Markus@OpenNMS.org
 */
public class SpreadsheetLayouter {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(SpreadsheetLayouter.class);

    public static File layoutGeneratedOdsFile(File generatedOdsFile) {
        File odsOutFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "Layouted_" + generatedOdsFile.getName());

        try {
            OdfSpreadsheetDocument spreadsheet = OdfSpreadsheetDocument.loadDocument(generatedOdsFile);

            List<OdfTable> tableList = spreadsheet.getTableList();
            for (OdfTable table : tableList) {
                table = formatCategories(table);
                table = formatNodes(table);
            }
            spreadsheet.save(odsOutFile);
        } catch (Exception ex) {
            logger.error("Layouting of the OdsFile went wrong.", ex);
        }
        return odsOutFile;
    }

    private static OdfTable formatCategories(OdfTable table) {
        OdfTableRow categoryRow = table.getRowByIndex(0);
        logger.info("CellCount for Row 0 '{}'", categoryRow.getCellCount());
        return table;
    }

    private static OdfTable formatNodes(OdfTable table) {
        return table;
    }
}
