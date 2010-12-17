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
package org.openmrs.module.formentry.web;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.web.dwr.EnteredField;

/**
 *
 */
public class DWRFormEntryService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public void enterForm(Integer patientId, Integer formId, boolean useEncounter, List<String> fields) {
		FormService fs = Context.getFormService();
		EncounterService es = Context.getEncounterService();
		PatientService ps = Context.getPatientService();
		
		Patient patient = ps.getPatient(patientId);
		Form form = fs.getForm(formId);
		Encounter encounter = new Encounter();
		encounter.setPatient(patient);
		encounter.setForm(form);
		encounter.setEncounterType(form.getEncounterType());
		List<Obs> obs = new ArrayList<Obs>();
		for (String temp : fields) {
			EnteredField enteredField = new EnteredField(temp);
			if (enteredField.isEmpty())
				continue;
			Field field = fs.getField(enteredField.getFieldId());
			log.debug("field: " + field);
			if (field.getTableName() != null && field.getTableName().length() > 0) {
				if (field.getTableName().toLowerCase().equals("encounter")) {
					String attrName = field.getAttributeName().toLowerCase();
					if ("location_id".equals(attrName))
						encounter.setLocation(es.getLocation(Integer.valueOf(enteredField.getValue())));
					else if ("encounter_datetime".equals(attrName)) {
						try {
							encounter.setEncounterDatetime(dateHelper(enteredField.getValue()));
						} catch (ParseException ex) {
							throw new RuntimeException("Error in encounter datetime", ex);
						}
					} else if ("provider_id".equals(attrName)) {
						encounter.setProvider(Context.getUserService().getUser(Integer.valueOf(enteredField.getValue())));
					}
				}
			} else {
				Concept question = field.getConcept();
				log.debug("question: " + question);
				Obs o = new Obs();
				o.setPerson(patient);
				o.setConcept(question);
				try {
					log.debug("o.getConcept() == " + o.getConcept());
					o.setValueAsString(enteredField.getValue());
				} catch (Exception ex) {
					throw new RuntimeException("Can't handle value " + enteredField.getValue() + " for concept " + question, ex);
				}
				if (enteredField.getDateTime() != null) {
					try {
						o.setObsDatetime(dateHelper(enteredField.getDateTime()));	
					} catch (ParseException ex) {
						throw new RuntimeException("Error in obs datetime: " + enteredField.getDateTime(), ex);
					}
				}
				obs.add(o);
			}
		}
		if (useEncounter) {
			for (Obs o : obs) {
				if (o.getObsDatetime() == null)
					o.setObsDatetime(encounter.getEncounterDatetime());
				if (o.getLocation() == null)
					o.setLocation(encounter.getLocation());
				encounter.addObs(o);
			}
			Context.getEncounterService().createEncounter(encounter);
		} else {
			// TODO: need to specify obsDatetime and location in case those are null
			for (Obs o : obs)
				Context.getObsService().createObs(o);
		}
	}
	
	static DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); 
	private static Date dateHelper(String value) throws ParseException {
		if (value == null || value.length() == 0)
			return null;
		else
			return df.parse(value);
	}
}
