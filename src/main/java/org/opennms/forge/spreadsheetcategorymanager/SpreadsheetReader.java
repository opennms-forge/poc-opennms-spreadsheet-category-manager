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
package org.opennms.forge.spreadsheetcategorymanager;

import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableColumn;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;
import org.odftoolkit.odfdom.type.Color;
import org.opennms.forge.spreadsheetcategorymanager.utils.NodeToCategoryMapping;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>SpreadsheetReader class.</p>
 *
 * @author <a href="mailto:markus@opennms.org">Markus Neumann</a>*
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 * @version 1.0-SNAPSHOT
 * @since 1.0-SNAPSHOT
 */
public class SpreadsheetReader {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(SpreadsheetReader.class);

    /**
     * Spreadsheet with nodes and category mapping
     */
    private final File m_odsFile;

    public SpreadsheetReader(File odsFile) throws IOException {
        this.m_odsFile = odsFile;

        if (!(this.m_odsFile.exists() && this.m_odsFile.canRead())) {
            // The file does not exist and is not readable
            logger.error("The file '{}' doesn't exist or is not readable.", this.m_odsFile.getName());
            throw new IOException("File " + this.m_odsFile.getName() + " isn't readable or doesn't exist");
        }
    }

    public List<NodeToCategoryMapping> getNodeToCategoryMappingsFromFile(String tableName) {
        List<NodeToCategoryMapping> nodes = new LinkedList<NodeToCategoryMapping>();
        List<String> categories = new LinkedList<String>();
        try {
            OdfSpreadsheetDocument spreadsheet = OdfSpreadsheetDocument.loadDocument(this.m_odsFile);
            OdfTable table = spreadsheet.getTableByName(tableName);
            OdfTableColumn nodeColumn = table.getColumnByIndex(0);
            OdfTableRow categoryRow = table.getRowByIndex(0);

            //Build a list of all Categories
            int categoryIndex = 1;
            while (!categoryRow.getCellByIndex(categoryIndex).getDisplayText().equals("")) {
                categories.add(categoryRow.getCellByIndex(categoryIndex).getDisplayText());
                categoryIndex++;
            }

            //Build a list of all Nodes with AddCategories and RemoveCategories
            int rowIndex = 1;
            while (!nodeColumn.getCellByIndex(rowIndex).getDisplayText().equals("")) {
                NodeToCategoryMapping node = new NodeToCategoryMapping(nodeColumn.getCellByIndex(rowIndex).getDisplayText());

                for (int cellId = 1; cellId <= categories.size(); cellId++) {
                    if (table.getRowByIndex(rowIndex).getCellByIndex(cellId).getDisplayText().equals("")) {
                        table.getRowByIndex(rowIndex).getCellByIndex(cellId).setCellBackgroundColor(Color.RED);
                        node.getRemoveCategories().add(new RequisitionCategory(categories.get(cellId - 1)));
                        logger.debug("Node '{}' found removeCategory '{}'", node.getNodeLabel(), categories.get(cellId - 1));
                    } else {
                        table.getRowByIndex(rowIndex).getCellByIndex(cellId).setCellBackgroundColor(Color.GREEN);
                        node.getAddCategories().add(new RequisitionCategory(categories.get(cellId - 1)));
                        logger.debug("Node '{}' found addCategory    '{}'", node.getNodeLabel(), categories.get(cellId - 1));
                    }
                }
                nodes.add(node);
                rowIndex++;
            }
            spreadsheet.save(new File(System.getProperty("java.io.tmpdir") + File.separator + "newFile.ods"));
        } catch (Exception ex) {
            logger.error("Reading odsFile went wrong", ex);
        }
        return nodes;
    }
}