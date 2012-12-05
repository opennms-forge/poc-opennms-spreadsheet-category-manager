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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.opennms.forge.restclient.utils.OnmsRestConnectionParameter;
import org.opennms.forge.restclient.utils.RestConnectionParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.DateFormatter;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This starter just provides the command line parameter handling.
 * 
 * @author Markus@OpenNMS.org
 */
public class Starter {

    private static Logger logger = LoggerFactory.getLogger(Starter.class);

    @Option(name = "--baseurl", aliases = {"-url"}, required = true, usage = "baseurl of the system to work with; http://demo.opennms.com/opennms/")
    private String m_baseUrl = "http://localhost:8980/opennms/";

    @Option(name = "--username", aliases = {"-u"}, required = true, usage = "username to work with the system")
    private String m_username = "admin";

    @Option(name = "--password", aliases = {"-p"}, required = true, usage = "m_password to work with the system")
    private String m_password = "admin";

    @Option(name = "--ods-file-source", aliases = {"-odssrc"}, required = true, usage = "path to the odsFile to read from")
    private String m_odsFileSource;

    @Option(name = "--foreign-source", aliases = {"-fs"}, required = true, usage = "name of the foreign source to work with")
    private String m_foreignSource;

    @Option(name = "--apply", aliases = {"-a"}, usage = "if this option is set, changes will be applied to the remote system.")
    private boolean m_apply = false;

    @Option(name = "--generateOds", aliases = {"-genods"}, usage = "if this option is set, just a ods file with the data from the remote system will be created in temp folder.")
    private boolean m_generateOds = false;

    @Option(name = "--all-foreign-source", aliases = {"-afs"}, usage = "runs the command for all foreign-sources")
    private boolean allForeignSources = false;

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

        RestConnectionParameter connParm = null;

        File workspace = setupWorkspace();
        setupLoggingFolder(workspace);

        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(TERMINAL_WIDTH);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            parser.printUsage(System.err);
            System.exit(1);
        }

        logger.info("OpenNMS Category Provisioning");

        try {
            connParm = new OnmsRestConnectionParameter(m_baseUrl, m_username, m_password);
        } catch (MalformedURLException ex) {
            logger.error("Invalid base URL '{}'. Error message: '{}'", m_baseUrl, ex.getMessage());

            // We don't have a valid URL, error exit
            System.exit(1);
        }

        RestCategoryProvisioner restCategoryProvisioner = new RestCategoryProvisioner(connParm, m_foreignSource, m_apply);
        if (m_generateOds) {
            if (allForeignSources) {
                restCategoryProvisioner.generateAllOdsFiles();
            } else {
                restCategoryProvisioner.generateOdsFile();
            }
        } else {
            restCategoryProvisioner.importCategoriesFromOds(m_odsFileSource);
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
            logger.error("Date String parsing went wrong", ex);
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