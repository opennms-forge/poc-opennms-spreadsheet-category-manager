/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

import org.opennms.forge.provisioningrestclient.api.RequisitionManager;
import org.opennms.forge.restclient.utils.RestConnectionParameter;
import org.opennms.forge.spreadsheetcategorymanager.utils.NodeToCategoryMapping;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * <p>RestCategoryProvisioner class.</p>
 * The main program to manage categories in provisioning via rest powered by spreadsheets.
 *
 * @author <a href="mailto:markus@opennms.org">Markus Neumann</a>
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 * @version 1.0-SNAPSHOT
 * @since 1.0-SNAPSHOT
 */
public class RestCategoryProvisioner {

    /**
     * Logging
     */
    private static Logger logger = LoggerFactory.getLogger(RestCategoryProvisioner.class);

    /**
     * <p>importCategoriesFromOds</p>
     * <p/>
     * Import nodes with spreadsheet provisioned surveillance categories into OpenNMS.
     *
     * @param restConnectionParameter helper that keeps base URL, username, password and so on for rest communication with OpenNMS
     * @param foreignSource           name of the foreign source to update note to category mappings
     * @param synchronize             changes will not just be send to the remote system, they will also be synchronized.
     * @param filename                Spreadsheet in ODS format
     */
    public static void importCategoriesFromOds(RestConnectionParameter restConnectionParameter, String foreignSource, Boolean synchronize, String filename) {
        File odsFile = new File(filename);

        // Check if the ODS file can be read
        if (!odsFile.canRead()) {
            logger.error("Cannot read ODS file for import in '{}'.", filename);
        }

        RequisitionManager requisitionManager = new RequisitionManager(restConnectionParameter, foreignSource);

        logger.debug("ODS file '{}' for import is readable", odsFile.getAbsoluteFile());
        Collection<NodeToCategoryMapping> nodeToCategoryMappings = readNodeToCategoryMappingsFromOdsFile(odsFile);

        changeNodeToCategoryMappingsInManagedRequisition(nodeToCategoryMappings, requisitionManager);
        requisitionManager.sendManagedRequisitionToOpenNMS();

        if (synchronize) {
            requisitionManager.synchronizeManagedRequisitionOnOpenNMS();
        }
    }

    /**
     * <p>readNodeToCategoryMappingsFromOdsFile</p>
     * <p/>
     * Get a collection of all Node to category mappings from the ODS file.
     *
     * @param odsFile the ODS File to read node to category mappings from
     * @return Collection of NodeToCategoryMappings from ODS File
     */
    private static Collection<NodeToCategoryMapping> readNodeToCategoryMappingsFromOdsFile(File odsFile) {

        Collection<NodeToCategoryMapping> nodeToCategoryMappings = new ArrayList<>();

        //read node to category mappings from spreadsheet
        SpreadsheetReader spreadsheetReader;

        try {
            spreadsheetReader = new SpreadsheetReader(odsFile);
            nodeToCategoryMappings = spreadsheetReader.getNodeToCategoryMappingsFromFile();

        } catch (IOException e) {
            logger.error("Error on reading spreadsheet with from '{}'.", odsFile.getAbsoluteFile(), e);
        }

        return nodeToCategoryMappings;
    }

    private static void changeNodeToCategoryMappingsInManagedRequisition(Collection<NodeToCategoryMapping> nodeToCategoryMappings, RequisitionManager requisitionManager) {

        for (NodeToCategoryMapping nodeToCategoryMapping : nodeToCategoryMappings) {
            RequisitionNode requisitionNode = requisitionManager.getRequisitionNode(nodeToCategoryMapping.getNodeLabel());
            if (requisitionNode != null) {

                //add all set categories
                Integer initialAmountOfCategories = requisitionNode.getCategories().size();
                for (RequisitionCategory addCategory : nodeToCategoryMapping.getAddCategories()) {
                    requisitionNode.putCategory(addCategory);
                }

                //remove all not set categories
                Integer afterAddingAmountOfCategories = requisitionNode.getCategories().size();
                for (RequisitionCategory removeCategory : nodeToCategoryMapping.getRemoveCategories()) {
                    requisitionNode.deleteCategory(removeCategory);
                }
                Integer afterRemoveAmountOfCategories = requisitionNode.getCategories().size();

                //compare amount of categories per step to identify changed requisition nodes
                if (initialAmountOfCategories.equals(afterAddingAmountOfCategories) && afterAddingAmountOfCategories.equals(afterRemoveAmountOfCategories)) {
                    logger.info("RequisitionNode '{}' has no updates", requisitionNode.getNodeLabel());
                } else {
                    logger.info("RequisitionNode '{}' has updates", requisitionNode.getNodeLabel());
                    requisitionManager.getRequisition().putNode(requisitionNode);
                }

            } else {
                logger.info("RequisitionNode '{}' is unknown on the system", nodeToCategoryMapping.getNodeLabel());
            }
        }
    }
}