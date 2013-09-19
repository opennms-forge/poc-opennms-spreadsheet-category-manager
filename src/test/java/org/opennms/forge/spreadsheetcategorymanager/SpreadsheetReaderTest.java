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
import org.junit.Test;
import org.opennms.forge.spreadsheetcategorymanager.utils.NodeToCategoryMapping;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * @author Markus@OpenNMS.org
 */
public class SpreadsheetReaderTest {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(SpreadsheetReaderTest.class);
    private SpreadsheetReader reader;

    @Before
    public void setup() {
        try {
            reader = new SpreadsheetReader(new File("src/test/resources/ImportTest.ods"));
        } catch (IOException e) {
            logger.error("Error during spreadsheed reading", e);
        }
    }

    @Test
    public void testGetNodeCategoryChangeFromFile() {
        Collection<NodeToCategoryMapping> nodeToCategoryMappings = reader.getNodeToCategoryMappingsFromFile();
        assertEquals("Amount of found NodeToCategory Entries", 8, nodeToCategoryMappings.size());
//        for (NodeToCategoryMapping nodeToCategoryMapping : nodeToCategoryMappings) {
//            logger.info("NodeToCategoryMapping for '{}' found addCategory size is '{}' found remove Category size is '{}'", nodeToCategoryMapping.getNodeLabel(), nodeToCategoryMapping.getAddCategories().size(), nodeToCategoryMapping.getRemoveCategories().size());
//        }
    }
}
