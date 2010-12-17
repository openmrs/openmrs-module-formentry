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

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.module.formentry.FormEntryService;
import org.openmrs.module.formentry.FormEntryUtil;
import org.openmrs.module.formentry.FormEntryXsn;
import org.openmrs.module.formentry.PublishInfoPath;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

/**
 * Copies the xsn for a form when the "FormService.duplicateForm" method is called. 
 */
public class DuplicateFormAdvisor extends StaticMethodMatcherPointcutAdvisor
		implements Advisor {

	private static final long serialVersionUID = 38539204394323L;

	private Log log = LogFactory.getLog(this.getClass());

	public DuplicateFormAdvisor() {
		log.error("instantiated ... hash code " + this.hashCode());
	}
	
	public boolean matches(Method method, Class targetClass) {
		if (method.getName().equals("duplicateForm"))
			log.error("duplicateForm method matched on class " + targetClass.getCanonicalName() + ", hash code " + this.hashCode());
		return method.getName().equals("duplicateForm");
	}

	@Override
	public Advice getAdvice() {
		return new DuplicateFormAdvice();
	}

	private class DuplicateFormAdvice implements MethodInterceptor {

		public Object invoke(MethodInvocation invocation) throws Throwable {

			Form oldForm = (Form) invocation.getArguments()[0];
			Integer oldFormId = oldForm.getFormId();
			Form newForm = null;
			File tempDir = null;

			if (log.isDebugEnabled())
				log.debug("Method: " + invocation.getMethod().getName());

			try {
				newForm = (Form) invocation.proceed();

				oldForm = Context.getFormService().getForm(oldFormId);
				FormEntryService formEntryService = (FormEntryService) Context
						.getService(FormEntryService.class);
				FormEntryXsn oldXsn = formEntryService.getFormEntryXsn(oldForm);

				Object[] streamAndDir = FormEntryUtil.getCurrentXSN(oldForm,
						false);
				InputStream oldFormStream = (InputStream) streamAndDir[0];
				tempDir = (File) streamAndDir[1];
				if (log.isDebugEnabled())
					log.debug("oldXSN: " + oldXsn);

				// only try to copy the file if there is an old xsn
				if (oldXsn != null && oldFormStream != null) {
					// rebuild the XSN -- this is really just used to change the
					// form "id"
					// attribute in the xsd file
					PublishInfoPath.publishXSN(oldFormStream, newForm);
					log.debug("Done duplicating xsn");
					try {
						oldFormStream.close();
					} catch (IOException ioe) {
						// pass
					}
				}
			} finally {
				try {
					if (tempDir != null)
						OpenmrsUtil.deleteDirectory(tempDir);
				} catch (IOException ioe) {
					// pass
				}
			}

			return newForm;
		}
	}

}