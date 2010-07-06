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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;
import org.openmrs.module.formentry.FormEntryConstants;
import org.openmrs.web.controller.PortletController;

/**
 * Controller that provides the addRelationship portlet with some context
 */
public class PatientRelationshipPortletController extends PortletController {
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * adds a relationshipMap variable to the model, to facilitate choosing a
	 * RelationshipType using the sides of the relationship as driving factors
	 */
	protected void populateModel(HttpServletRequest request,
			Map<String, Object> model) {
		List<RelationshipType> rTypes = Context.getPersonService()
				.getAllRelationshipTypes();

		// yes, it's a map of maps ... but it all makes sense in the javascript,
		// and TreeMaps will let the map be automatically alphabetized
		Map<String, Map<String, RelationshipType>> rMap = new TreeMap<String, Map<String, RelationshipType>>(
				new OrderedRelationshipsComparator());
		for (RelationshipType rType : rTypes) {
			String aToB = rType.getaIsToB();
			String bToA = rType.getbIsToA();

			if (!rMap.containsKey(aToB))
				// add an element if aToB isn't in the top-level map's keys
				rMap.put(aToB, new TreeMap<String, RelationshipType>());
			if (rMap.get(aToB).containsKey(bToA))
				// this relationship was apparently already added
				log
						.warn("two relationships with similar As and Bs exist: relationship type id #"
								+ rType.getRelationshipTypeId());
			// add the relationship to the map entry's map
			rMap.get(aToB).put(bToA, rType);

			if (!aToB.equals(bToA)) {
				// aToB and bToA are not the same (i.e. Sibling <-> Sibling)
				if (!rMap.containsKey(bToA))
					// add an element if bToA isn't in the top-level map's keys
					rMap.put(bToA, new TreeMap<String, RelationshipType>());
				if (rMap.get(bToA).containsKey(aToB))
					// this relationship was apparently already added
					log
							.warn("two relationships with similar As and Bs exist: relationship type id #"
									+ rType.getRelationshipTypeId());
				// add the relationship to the map entry's map
				rMap.get(bToA).put(aToB, rType);
			}
		}

		// the end result should be along the lines of:
		// rMap.get('Doctor') = {'Patient': <RelationshipType>}
		// rMap.get('Patient') = {'Doctor': <RelationshipType>}
		// rMap.get('Child') = {'Parent': <RelationshipType>, 'Guardian':
		// <RelationshipType>}
		// and so on ...
		model.put("relationshipMap", rMap);
	}

	/**
	 * @author jkeiper
	 * 
	 *         Comparator for ordering relationship sides, specifically for the
	 *         patient relationship portlet for use with InfoPath
	 */
	class OrderedRelationshipsComparator implements Comparator<String> {
		List<String> order = new ArrayList<String>();

		/**
		 * build the order list so we can refer to it in comparisons
		 */
		public OrderedRelationshipsComparator() {
			// get the order GP and parse it into a real list
			String sortOrder = Context
					.getAdministrationService()
					.getGlobalProperty(
							FormEntryConstants.FORMENTRY_GP_RELATIONSHIP_SORT_ORDER,
							"");
			for (String item : sortOrder.split(","))
				if (!item.trim().isEmpty())
					order.add(item.trim());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(String arg0, String arg1) {
			// compare relationship side strings; if the string appears in the
			// official ordering list, it comes first ... if both exist in the
			// list, they are ordered according to when they appear in the list
			// and otherwise, the strings are alphabetized
			if (order.contains(arg0) || order.contains(arg1)) {
				if (!order.contains(arg0))
					return 1;
				if (!order.contains(arg1))
					return -1;
				return order.indexOf(arg0) - order.indexOf(arg1);
			}
			return arg0.compareTo(arg1);
		}

	}

}
