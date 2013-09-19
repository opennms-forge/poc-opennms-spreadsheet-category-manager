/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.forge.spreadsheetcategorymanager;

import org.junit.Before;
import org.opennms.forge.restclient.utils.OnmsRestConnectionParameter;
import org.opennms.forge.restclient.utils.RestConnectionParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;

/**
 * <p>RestCategoryProvisionerTest class.</p>
 *
 * @author <a href="mailto:markus@opennms.org">Markus Neumann</a>
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 * @version 1.0-SNAPSHOT
 * @since 1.0-SNAPSHOT
 */
public class RestCategoryProvisionerTest {

    private static Logger logger = LoggerFactory.getLogger(RestCategoryProvisionerTest.class);

//    private String m_baseUrl = "http://localhost:8980/opennms/";
//
//    private String m_userName = "admin";
//
//    private String m_password = "admin";

    private String m_baseUrl = "http://demo.opennms.com/opennms/";

    private String m_userName = "demo";

    private String m_password = "demo";

    private String m_foreignSource = "RestProvisioningTest";

    private boolean m_apply = false;

    private RestCategoryProvisioner m_provider;

    @Before
    public void setUp() {
        try {
            RestConnectionParameter conParm = new OnmsRestConnectionParameter(this.m_baseUrl, this.m_userName, this.m_password);
        } catch (MalformedURLException e) {
            logger.error("Invalid base UrL '{}'. Error message: '{}'", this.m_baseUrl, e.getMessage());
        }
    }
}
