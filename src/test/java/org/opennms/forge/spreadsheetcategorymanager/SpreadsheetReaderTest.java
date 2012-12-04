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
import org.opennms.forge.spreadsheetcategorymanage.SpreadsheetReader;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * @author Markus@OpenNMS.org
 */
public class SpreadsheetReaderTest {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(SpreadsheetReaderTest.class);
    private SpreadsheetReader reader;

    @Before
    public void setup() {
        reader = new SpreadsheetReader();
    }

    @Test
    public void testGetNodeCategoryChangeFromFile() {
        List<NodeToCategoryMapping> nodeCategoryChanges = reader.getNodeToCategoryMappingsFromFile(new File("/home/tak/test.ods"), "Threshold");
        logger.info("Got '{}' NodeCategoryChanges.", nodeCategoryChanges.size());
        for (NodeToCategoryMapping nodeCategoryChange : nodeCategoryChanges) {
            logger.info("NodeCategoryChange for '{}' found addCategory size is '{}' found remove Category size is '{}'", nodeCategoryChange.getNodeLabel(), nodeCategoryChange.getAddCategories().size(), nodeCategoryChange.getRemoveCategories().size());
        }
    }
}
