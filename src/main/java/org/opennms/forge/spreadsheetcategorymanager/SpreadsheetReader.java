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
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.*;

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

    public List<NodeToCategoryMapping> getNodeToCategoryMappingsFromFile(File odsFile, String tableName) {
        List<NodeToCategoryMapping> nodes = new LinkedList<NodeToCategoryMapping>();
        List<String> categories = new LinkedList<String>();
        if (odsFile.exists() && odsFile.canRead()) {
            try {
                OdfSpreadsheetDocument spreadsheet = OdfSpreadsheetDocument.loadDocument(odsFile);
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

        } else {
            logger.error("OdsFile '{}' dose not exist or is not readable.", odsFile);
        }

        return nodes;
    }

    public File getSpeadsheetFromRequisition(Requisition requisition) {
        if (requisition == null) {
            logger.error("Requisition was null");
            return null;
        }
        File odsOutFile = new File(System.getProperty("java.io.tmpdir") + File.separator + requisition.getForeignSource() + ".ods");
        try {
            InputStream template = this.getClass().getClassLoader().getResourceAsStream("template.ods");
            OdfSpreadsheetDocument spreadsheet = OdfSpreadsheetDocument.loadDocument(template);
            OdfTable thresholdTable = spreadsheet.getTableList().get(0);
            thresholdTable.setTableName(requisition.getForeignSource() + " " + "TH");

            OdfTable categoryTable = spreadsheet.getTableList().get(1);
            categoryTable.setTableName(requisition.getForeignSource() + " " + "CATEGORIES");

            Map<String, RequisitionNode> reqNodes = new TreeMap<String, RequisitionNode>();
            Set<String> categories = new TreeSet<String>();
            Set<String> thresholdCategories = new TreeSet<String>();

            for (RequisitionNode reqNode : requisition.getNodes()) {
                reqNodes.put(reqNode.getNodeLabel(), reqNode);
                for (RequisitionCategory reqCategory : reqNode.getCategories()) {
                    if (reqCategory.getName().startsWith("TH-")) {
                        thresholdCategories.add(reqCategory.getName());
                    } else {
                        categories.add(reqCategory.getName());
                    }
                }
            }

            writeCategoriesIntoSheet(thresholdTable.getRowByIndex(0), thresholdCategories, requisition.getForeignSource());
            writeNodesIntoSheet(thresholdTable, reqNodes, thresholdCategories);

            writeCategoriesIntoSheet(categoryTable.getRowByIndex(0), categories, requisition.getForeignSource());
            writeNodesIntoSheet(categoryTable, reqNodes, categories);

            spreadsheet.save(odsOutFile);
            logger.info("saved '{}'", odsOutFile);

        } catch (Exception ex) {
            logger.error("Building Spreadsheet went wrong", ex);
        }
        return odsOutFile;
    }

    private void writeCategoriesIntoSheet(OdfTableRow categoryRow, Set<String> categories, String foreignSource) {
        categoryRow.getCellByIndex(0).setDisplayText(foreignSource);
        int categoryCellIndex = 1;
        for (String category : categories) {
            categoryRow.getCellByIndex(categoryCellIndex).setDisplayText(category);
            categoryCellIndex++;
        }
    }

    private void writeNodesIntoSheet(OdfTable table, Map<String, RequisitionNode> reqNodes, Set<String> categories) {
        OdfTableColumn nodeColumn = table.getColumnByIndex(0);
        OdfTableRow categoryRow = table.getRowByIndex(0);
        int nodeCellIndex = 1;
        for (RequisitionNode reqNode : reqNodes.values()) {
            nodeColumn.getCellByIndex(nodeCellIndex).setDisplayText(reqNode.getNodeLabel());
            if (categories.size() > 0) {
                for (int categoryIndex = 1; categoryIndex <= categories.size(); categoryIndex++) {
                    if (reqNode.getCategories().contains(new RequisitionCategory(categoryRow.getCellByIndex(categoryIndex).getDisplayText()))) {
                        table.getRowByIndex(nodeCellIndex).getCellByIndex(categoryIndex).setDisplayText("X");
                    } else {
                        table.getRowByIndex(nodeCellIndex).getCellByIndex(categoryIndex).setDisplayText("");
                    }
                }
            }
            nodeCellIndex++;
        }
    }
}