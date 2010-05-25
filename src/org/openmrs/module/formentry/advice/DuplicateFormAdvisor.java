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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.module.formentry.FormEntryService;
import org.openmrs.module.formentry.FormEntryUtil;
import org.openmrs.module.formentry.FormEntryXsn;
import org.openmrs.module.formentry.PublishInfoPath;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.aop.AfterReturningAdvice;

/**
 * Copies the xsn for a form when the "FormService.duplicateForm" method is called. 
 */
public class DuplicateFormAdvisor implements AfterReturningAdvice {

	private static final long serialVersionUID = 38539204394323L;

	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void afterReturning(Object returnValue, Method method,
			Object[] args, Object target) throws Throwable {
		
		if (method.getName().equals("duplicateForm")) {
			
			FormEntryService formEntryService = (FormEntryService)Context.getService(FormEntryService.class);
			
			if (log.isDebugEnabled())
				log.debug("Method: " + method.getName());
			
			Form oldForm = (Form)args[0];
			
			FormEntryXsn oldXsn = formEntryService.getFormEntryXsn(oldForm.getFormId());
			
			// only try to copy the file if there is an old xsn
			if (oldXsn != null) {
				Object[] streamAndDir = FormEntryUtil.getCurrentXSN(oldForm, false);
				InputStream oldFormStream = (InputStream) streamAndDir[0];
				File tempDir = (File) streamAndDir[1];
				if (log.isDebugEnabled())
					log.debug("oldXSN: " + oldXsn);
				
				Form newForm = null;
				try {
					
					// only try to copy the file if there is an old xsn
					if (oldFormStream != null) {
						// rebuild the XSN -- this is really just used to change the form "id"
						// attribute in the xsd file
						PublishInfoPath.publishXSN(oldFormStream, newForm);
						log.debug("Done duplicating xsn");
						try {
							oldFormStream.close();
						} 
						catch (IOException ioe) {
							// pass
						}
					}
				}
				finally {
					try {
						if (tempDir != null)
							OpenmrsUtil.deleteDirectory(tempDir);
					} 
					catch (IOException ioe) {
						// pass
					}
				}
			}
		}
		
	}
	
}