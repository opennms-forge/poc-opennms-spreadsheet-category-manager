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
import org.opennms.forge.spreadsheetcategorymanager.utils.NodeToCategoryMapping;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>SpreadsheetReader class.</p>
 *
 * @author <a href="mailto:markus@opennms.org">Markus Neumann</a>
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

    public Collection<NodeToCategoryMapping> getNodeToCategoryMappingsFromFile() {
        Map<String, NodeToCategoryMapping> nodesToCategories = new HashMap<>();

        try {
            OdfSpreadsheetDocument spreadsheet = OdfSpreadsheetDocument.loadDocument(this.m_odsFile);

            for (OdfTable table : spreadsheet.getTableList()) {
                nodesToCategories = getNodeToCategoryMappingsFromTable(nodesToCategories, table);
            }

        } catch (Exception ex) {
            logger.error("Reading spreadsheet went wrong", ex);
        }

        return nodesToCategories.values();
    }

    private Map<String, NodeToCategoryMapping> getNodeToCategoryMappingsFromTable(Map<String, NodeToCategoryMapping> nodesToCategories, OdfTable table) {
        logger.info("Reading Nodes and Categories from '{}'", table.getTableName());
        OdfTableColumn nodeColumn = table.getColumnByIndex(0);
        OdfTableRow categoryRow = table.getRowByIndex(0);
        NodeToCategoryMapping nodeToCategoryMapping;

        //Build a list of all Categories
        List<String> categories = new LinkedList<>();
        int categoryIndex = 1;
        while (!categoryRow.getCellByIndex(categoryIndex).getDisplayText().equals("")) {
            categories.add(categoryRow.getCellByIndex(categoryIndex).getDisplayText().trim());
            categoryIndex++;
        }

        //Build a list of all Nodes with AddCategories and RemoveCategories
        int rowIndex = 1;
        while (!nodeColumn.getCellByIndex(rowIndex).getDisplayText().equals("")) {
            //Use already existing nodeToCategoryMapping objects if possible
            String nodeLabel = nodeColumn.getCellByIndex(rowIndex).getDisplayText().trim();
            if (nodesToCategories.containsKey(nodeLabel)) {
                nodeToCategoryMapping = nodesToCategories.get(nodeLabel);
            } else {
                nodeToCategoryMapping = new NodeToCategoryMapping(nodeLabel);
                nodesToCategories.put(nodeLabel, nodeToCategoryMapping);
            }

            for (int cellId = 1; cellId <= categories.size(); cellId++) {
                if (table.getRowByIndex(rowIndex).getCellByIndex(cellId).getDisplayText().equals("")) {
                    nodeToCategoryMapping.getRemoveCategories().add(new RequisitionCategory(categories.get(cellId - 1).trim()));
                    logger.debug("Node '{}' found removeCategory '{}'", nodeToCategoryMapping.getNodeLabel(), categories.get(cellId - 1));
                } else {
                    nodeToCategoryMapping.getAddCategories().add(new RequisitionCategory(categories.get(cellId - 1).trim()));
                    logger.debug("Node '{}' found addCategory    '{}'", nodeToCategoryMapping.getNodeLabel(), categories.get(cellId - 1));
                }
            }
            rowIndex++;
        }
        return nodesToCategories;
    }
}