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
package org.openmrs.module.formentry.advice;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.formentry.FormEntryUtil;
import org.openmrs.module.formentry.FormEntryXsn;
import org.springframework.aop.AfterReturningAdvice;

/**
 * This class knows about the sync module and rebuilds the xsn after an xsn is
 * received into the SyncIngestService
 */
public class RebuildXsnAfterSyncAdvice implements AfterReturningAdvice {

	private Log log = LogFactory.getLog(this.getClass());

	@Override
	public void afterReturning(Object returnValue, Method m,
			Object[] args, Object target) throws Throwable {
		
		if (m.getName().equals("applyPreCommitRecordActions")) {
			
			Map<String, List<OpenmrsObject>> processedObjects = (Map<String, List<OpenmrsObject>>) args[0];
			
			if (processedObjects == null)
				return;
			
			// rebuild each received form
			List<OpenmrsObject> xsns = processedObjects.get("org.openmrs.module.formentry.FormEntryXsn");
			if (xsns != null) {
				for (OpenmrsObject o : xsns) {
					FormEntryXsn xsn = (FormEntryXsn) o;
	    			Form form = xsn.getForm();
	    			log.info("Rebuilding xsn for form: " + form);
	    			FormEntryUtil.rebuildXSN(form);
				}
			}
			
		}
	}

}