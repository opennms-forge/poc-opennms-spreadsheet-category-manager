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
 ******************************************************************************
 */
package org.opennms.forge.spreadsheetcategorymanager;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.opennms.forge.restclient.utils.OnmsRestConnectionParameter;
import org.opennms.forge.restclient.utils.RestConnectionParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import javax.swing.text.DateFormatter;

/**
 * @author Markus@OpenNMS.org
 */
public class Starter {

    private static Logger logger = LoggerFactory.getLogger(Starter.class);

    @Option(name = "-baseurl", required = true, usage = "baseurl of the system to work with; http://demo.opennms.com/opennms/")
    private String baseUrl = "http://localhost:8980/opennms/";

    @Option(name = "-username", required = true, usage = "username to work with the system")
    private String userName = "admin";

    @Option(name = "-password", required = true, usage = "password to work with the system")
    private String password = "admin";

    @Option(name = "-odsFile", required = true, usage = "path to the odsFile to read from")
    private String odsFilePath;

    @Option(name = "-foreign-source", required = true, usage = "name of the foreign source to work with")
    private String foreignSource;

    @Option(name = "-apply", usage = "if this option is set, changes will be applied to the remote system.")
    private boolean apply = false;

    @Option(name = "-generateOds", usage = "if this option is set, just a ods file with the data from the remote system will be created in temp folder.")
    private boolean generateOds = false;

    @Option(name = "-allFS", usage = "runs the command for all foreign-sources")

    private boolean allForeignSources = false;

    private RestConnectionParameter restConnectionParameter;

    /**
     * Set maximal terminal width for line breaks
     */
    private final int TERMINAL_WIDTH = 120;

    private final String WORKSPACE_NAME = "SSCM_Workspace";
    private final String DATE_FORMAT = "yyyy-MM-dd_HH:mm:ss";

    public static void main(String[] args) throws IOException {
        new Starter().doMain(args);
    }

    public void doMain(String[] args) {

        File workspace = setupWorkspace();
        setupLoggingFolder(workspace);

        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(TERMINAL_WIDTH);

        logger.info("OpenNMS Category Provisioning");
        try {
            restConnectionParameter = new OnmsRestConnectionParameter(baseUrl, userName, password);
//            ApacheHttpClient httpClient = RestHelper.createApacheHttpClient(restConnectionParameter);

        } catch (MalformedURLException e) {
            logger.error("Invalid base URL '{}'. Error message: '{}'", baseUrl, e.getMessage());
            System.exit(1);
        }
        try {
            parser.parseArgument(args);
            File odsFile = new File(odsFilePath);
            if (odsFile.exists() && odsFile.canRead()) {
                RestCategoryProvisioner restCategoryProvisioner = new RestCategoryProvisioner(restConnectionParameter, odsFile, foreignSource, apply);
                if (generateOds) {
                    if (allForeignSources) {
                        restCategoryProvisioner.generateAllOdsFiles();
                    } else {
                        restCategoryProvisioner.generateOdsFile();
                    }
                } else {
                    restCategoryProvisioner.getRequisitionNodesToUpdate();
                }
            } else {
                logger.info("The odsFile '{}' dose not exist or is not readable, sorry.", odsFilePath);
            }
        } catch (CmdLineException ex) {
            parser.printUsage(System.err);
        }

        logger.info("Thanks for computing with OpenNMS!");
    }

    private File setupWorkspace() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        DateFormatter dateFormatter = new DateFormatter(dateFormat);
        String dateString = "not_set_yet";
        File workspace = null;
        try {
            dateString = dateFormatter.valueToString(new Date());
            workspace = new File(WORKSPACE_NAME + File.separator + dateString);
            workspace.mkdirs();
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(Starter.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (workspace != null && !(workspace.exists() && workspace.canRead() && workspace.canWrite())) {
            logger.error("Problem with Workspace folder '{}'", WORKSPACE_NAME + File.separator + dateString);
            System.exit(1);
        }
        return workspace;
    }

    private void setupLoggingFolder(File workspace) {
        //TODO Tak
    }
}