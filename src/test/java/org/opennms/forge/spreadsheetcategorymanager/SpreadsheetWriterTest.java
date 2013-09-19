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
import org.junit.Ignore;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus@OpenNMS.org
 */
public class SpreadsheetWriterTest {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(SpreadsheetWriterTest.class);

    private SpreadsheetWriter writer;

    @Before
    public void setup() {
        writer = new SpreadsheetWriter();
    }

    @Test
    @Ignore
    public void testGetSpreadsheetFromRequisition() {
        //this step is creating a TestRequisition.ods in the temp folder.
        writer.getSpreadsheetFromRequisition(generateTestRequisition(), null);
    }

    private Requisition generateTestRequisition() {
        Requisition requisition = new Requisition("TestRequisition");
        List<RequisitionNode> reqNodes = new ArrayList<>();

        List<RequisitionCategory> reqCatA = new ArrayList<>();
        reqCatA.add(new RequisitionCategory("Category-A"));

        RequisitionNode reqNodeA = new RequisitionNode();
        reqNodeA.setNodeLabel("Node-A");
        reqNodeA.setForeignId("ForeignId-A");
        reqNodeA.setCategories(reqCatA);
        reqNodes.add(reqNodeA);

        List<RequisitionCategory> reqCatB = new ArrayList<>();
        reqCatB.add(new RequisitionCategory("Category-B"));

        RequisitionNode reqNodeB = new RequisitionNode();
        reqNodeB.setNodeLabel("Node-B");
        reqNodeB.setForeignId("ForeignId-B");
        reqNodeB.setCategories(reqCatB);
        reqNodes.add(reqNodeB);

        requisition.setNodes(reqNodes);
        return requisition;
    }
}
