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

import org.opennms.forge.restclient.api.RestRequisitionProvider;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.opennms.forge.restclient.utils.RestConnectionParameter;

/**
 * Provides static methods to read node and category data from a remote OpenNMS.
 * Uses Rest to get the data and provides the results as ODS spreadsheets.
 * It is a noninstantiable utility class.
 *
 * @author <a href="mailto:markus@opennms.org">Markus Neumann</a>
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 * @version 1.0-SNAPSHOT
 * @since 1.0-SNAPSHOT
 */

public final class RestCategoryReader {

    /**
     * Logging
     */
    private static Logger logger = LoggerFactory.getLogger(RestCategoryReader.class);
 
    /** Suppress default constructor for noninstantiability */
    private RestCategoryReader() {
    }

    /**
     * <p>generateOdsFile</p>
     * <p/>
     * Generate an ODS file from an OpenNMS provisioning requisition identified by foreignSource.
     * <p/>
     * @param foreignSource the name of the foreignSource to read into a ODS file.
     * @param connectionParameter object that keeps baseUrl, user, password and so on for the rest calls
     * @return The generated OdsFile for the foreignSource of the remote OpenNMS defined in the connectionParameter.
     */
    public static File generateOdsFile(String foreignSource, RestConnectionParameter connectionParameter, File templateOds) {
        Requisition requisition = new RestRequisitionProvider(connectionParameter).getRequisition(foreignSource, "");

        if (templateOds != null) {
            if (!(templateOds.exists() && templateOds.canRead())) {
                logger.error("Ods template file '{}' dose not exist, or is not readable.", templateOds);
                logger.error("Fallback to default ods template");
                templateOds = null;
            }
        }

        SpreadsheetWriter spreadsheetWriter = new SpreadsheetWriter();
        File generatedOdsFile = spreadsheetWriter.getSpreadsheetFromRequisition(requisition, templateOds);

        return generatedOdsFile;
    }

    /**
     *
     * @param connectionParameter object that keeps baseUrl, user, password and so on for the rest calls
     * @return A List of generated OdsFiles for all foreignSources of the remote opennms defined in the connectionParameter.
     */
    public static List<File> generateAllOdsFiles(RestConnectionParameter connectionParameter, File templateOds) {
        List<File> odsFiles = new ArrayList<>();

        RestRequisitionProvider requisitionProvider = new RestRequisitionProvider(connectionParameter);
        RequisitionCollection allRequisitions = requisitionProvider.getAllRequisitions("");

        if (templateOds != null) {
            if (!(templateOds.exists() && templateOds.canRead())) {
                logger.error("Ods template file '{}' dose not exist, or is not readable.", templateOds);
                logger.error("Fallback to default ods template");
                templateOds = null;
            }
        }

        SpreadsheetWriter spreadsheetWriter = new SpreadsheetWriter();
        for (Requisition requisition : allRequisitions) {
            odsFiles.add(spreadsheetWriter.getSpreadsheetFromRequisition(requisition, templateOds));
        }
        return odsFiles;
    }
}