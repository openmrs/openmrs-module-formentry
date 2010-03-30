/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.formentry.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;
import org.openmrs.web.controller.PortletController;

/**
 * Controller that provides the addRelationship portlet with some context
 */
public class PatientRelationshipPortletController extends PortletController {
    protected final Log log = LogFactory.getLog(getClass());

	/**
	 * adds a relationshipMap variable to the model, to facilitate choosing a
	 * RelationshipType using the sides of the relationship as factors
	 */
	protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
		List<RelationshipType> rTypes = Context.getPersonService().getAllRelationshipTypes(); 
 
		Map<String, Map<String, RelationshipType>> rMap = new TreeMap();
		for (RelationshipType rType: rTypes) {
			String aToB = rType.getaIsToB();
			String bToA = rType.getbIsToA();

			if (!rMap.containsKey(aToB)) 
				rMap.put(aToB, new TreeMap());
			if (rMap.get(aToB).containsKey(bToA))
				log.warn("two relationships with similar As and Bs exist: relationship type id #" + rType.getRelationshipTypeId());
			rMap.get(aToB).put(bToA, rType);
			
			if (!aToB.equals(bToA)) {
				if (!rMap.containsKey(bToA)) 
					rMap.put(bToA, new TreeMap());
				if (rMap.get(bToA).containsKey(aToB))
					log.warn("two relationships with similar As and Bs exist: relationship type id #" + rType.getRelationshipTypeId());
				rMap.get(bToA).put(aToB, rType);
			}
		}
		
		model.put("relationshipMap", rMap);
	}
}
