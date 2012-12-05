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
import org.opennms.forge.restclient.api.RestRequisitionProvider;
import org.opennms.forge.restclient.utils.RestConnectionParameter;
import org.opennms.forge.spreadsheetcategorymanager.utils.NodeToCategoryMapping;
import org.opennms.forge.spreadsheetcategorymanager.utils.SpreadsheetLayouter;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>RestCategoryProvisioner class.</p>
 * The main program to manage categories in provisioning via rest powered by spreadsheets.
 * 
 * @author <a href="mailto:markus@opennms.org">Markus Neumann</a>
 *
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
     * ODS file with node and category association
     */
    private File m_odsFile;
    /**
     * Default do not apply the new categories on the node, give a feedback for sanity check first
     */
    private boolean m_apply = false;
    /**
     * Contains the nodes from OpenNMS and provides access to the requisition node
     */
    private RequisitionManager m_requisitionManager;
    /**
     * Connection parameter for ReST HTTP client
     */
    private RestConnectionParameter m_restRestConnectionParameter;

    /**
     * Constructor to initialize the ReST category provisioner.
     *
     * @param restConnectionParameter Helper to bundle connection parameters for the rest client
     * @param foreignSource Name of the foreign source which has to be updated
     * @param apply Flag for preview or directly apply changes in OpenNMS and synchronize the OpenNMS database
     */
    public RestCategoryProvisioner(RestConnectionParameter restConnectionParameter, String foreignSource, Boolean apply) {
        this.m_apply = apply;
        this.m_restRestConnectionParameter = restConnectionParameter;
        this.m_requisitionManager = new RequisitionManager(restConnectionParameter, foreignSource);
    }

    /**
     * <p>readNodeToCategoryMappingsFromOdsFile</p>
     * <p/>
     * Get a collection of all Node to category mappings from the ODS file.
     *
     * @param odsFile the ODS File to read node to category mappings from
     * @return Collection of NodeToCategoryMappings from ODS File
     */
    public Collection<NodeToCategoryMapping> readNodeToCategoryMappingsFromOdsFile(File odsFile) {

        Collection<NodeToCategoryMapping> nodeToCategoryMappings = new ArrayList<NodeToCategoryMapping>();

        //read node to category mappings from spreadsheet
        SpreadsheetReader spreadsheetReader;

        try {
            spreadsheetReader = new SpreadsheetReader(odsFile);
            nodeToCategoryMappings = spreadsheetReader.getNodeToCategoryMappingsFromFile();

        } catch (IOException e) {
            logger.error("Error on reading spreadsheet with from '{}'.", this.m_odsFile.getAbsoluteFile(), e);
        }

        return nodeToCategoryMappings;
    }

    //TODO JavaDoc
    private void changeCategoryMappingsInManagedRequisition(Collection<NodeToCategoryMapping> nodeToCategoryMappings) {

        for (NodeToCategoryMapping nodeToCategoryMapping : nodeToCategoryMappings) {
            RequisitionNode requisitionNode = m_requisitionManager.getRequisitionNode(nodeToCategoryMapping.getNodeLabel());
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
                    m_requisitionManager.getRequisition().putNode(requisitionNode);
                }

            } else {
                logger.info("RequisitionNode '{}' is unknown on the system", nodeToCategoryMapping.getNodeLabel());
            }
        }
    }

    /**
     * <p>generateOdsFile</p>
     * <p/>
     * Generate an ODS file from an OpenNMS provisioning requisition identified by name.
     * <p/>
     * @return The generated OdsFile for the foreignSource of the RestCategoryProvider.
     */
    public File generateOdsFile() {
        Requisition requisition = m_requisitionManager.getRequisition();

        SpreadsheetWriter spreadsheetWriter = new SpreadsheetWriter();
        File generatedOdsFile = spreadsheetWriter.getSpreadsheetFromRequisition(requisition);

        //TODO tak: This one is marked as redundant
        File formattedOdsFile = SpreadsheetLayouter.layoutGeneratedOdsFile(generatedOdsFile);

        return formattedOdsFile;
    }

    public List<File> generateAllOdsFiles() {
        List<File> odsFiles = new ArrayList<File>();

        RestRequisitionProvider requisitionProvider = new RestRequisitionProvider(m_restRestConnectionParameter);
        RequisitionCollection allRequisitions = requisitionProvider.getAllRequisitions("");

        SpreadsheetWriter spreadsheetWriter = new SpreadsheetWriter();
        for (Requisition requisition : allRequisitions) {
            odsFiles.add(SpreadsheetLayouter.layoutGeneratedOdsFile(spreadsheetWriter.getSpreadsheetFromRequisition(requisition)));
        }
        return odsFiles;
    }

    /**
     * <p>importCategoriesFromOds</p>
     * <p/>
     * Import nodes with spreadsheet provisioned surveillance categories into OpenNMS.
     *
     * @param filename Spreadsheet in ODS format
     */
    public void importCategoriesFromOds(String filename) {
        File odsFile = new File(filename);

        // Check if the ODS file can be read
        if (!odsFile.canRead()) {
            logger.error("Cannot read ODS file for import in '{}'.", filename);
            System.exit(1);
        }
        
        logger.debug("ODS file '{}' for import is readable", odsFile.getAbsoluteFile());
        Collection<NodeToCategoryMapping> nodeToCategoryMappings = readNodeToCategoryMappingsFromOdsFile(odsFile);

        changeCategoryMappingsInManagedRequisition(nodeToCategoryMappings);

        m_requisitionManager.sendManagedRequisitionToOpenNMS();

        if (m_apply) {
            m_requisitionManager.synchronizeManagedRequisitionOnOpenNMS();
        }
    }
}